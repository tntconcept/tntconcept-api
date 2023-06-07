package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.InvalidActivityApprovalStateException
import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.time.LocalTime
import javax.transaction.Transactional

@Singleton
internal class ActivityService(
    private val activityRepository: ActivityRepository,
    @param:Named("Internal") private val internalActivityRepository: ActivityRepository,
    private val projectRoleRepository: ProjectRoleRepository,
    private val activityEvidenceService: ActivityEvidenceService
) {

    @Transactional
    @ReadOnly
    fun getActivityById(id: Long): com.autentia.tnt.binnacle.core.domain.Activity {
        return activityRepository.findById(id)?.toDomain() ?: throw ActivityNotFoundException(id)
    }

    @Transactional
    @ReadOnly
    fun getActivitiesBetweenDates(dateInterval: DateInterval, userId: Long): List<Activity> {
        val startDateMinHour = dateInterval.start.atTime(LocalTime.MIN)
        val endDateMaxHour = dateInterval.end.atTime(LocalTime.MAX)
        return activityRepository.findByUserId(startDateMinHour, endDateMaxHour, userId)
    }

    @Transactional
    @ReadOnly
    fun getActivities(activitySpecification: Specification<Activity>): List<com.autentia.tnt.binnacle.core.domain.Activity> {
        return activityRepository.findAll(activitySpecification).map { it.toDomain() }
    }


    @Transactional
    @ReadOnly
    fun getUserActivitiesBetweenDates(dateInterval: DateInterval, userId: Long): List<Activity> {
        val startDateMinHour = dateInterval.start.atTime(LocalTime.MIN)
        val endDateMaxHour = dateInterval.end.atTime(LocalTime.MAX)
        return internalActivityRepository.findByUserId(startDateMinHour, endDateMaxHour, userId)
    }

    @Transactional
    @ReadOnly
    fun getActivities(timeInterval: TimeInterval, userIds: List<Long>): List<Activity> =
        activityRepository.find(timeInterval.start, timeInterval.end, userIds)

    @Transactional
    @ReadOnly
    fun getActivitiesByProjectId(timeInterval: TimeInterval, projectId: Long, userId: Long): List<Activity> =
        activityRepository.findByProjectId(timeInterval.start, timeInterval.end, projectId, userId)

    @Transactional
    @ReadOnly
    fun getActivitiesByProjectRoleIds(timeInterval: TimeInterval, projectRoleIds: List<Long>, userId: Long) =
        activityRepository.findByProjectRoleIds(timeInterval.start, timeInterval.end, projectRoleIds, userId)
            .map(Activity::toDomain)

    @Transactional
    @ReadOnly
    fun getActivitiesOfLatestProjects(timeInterval: TimeInterval, userId: Long) =
        activityRepository.findOfLatestProjects(timeInterval.start, timeInterval.end, userId)

    @Transactional(rollbackOn = [Exception::class])
    fun createActivity(
        activityToCreate: com.autentia.tnt.binnacle.core.domain.Activity, evidence: EvidenceDTO?
    ): com.autentia.tnt.binnacle.core.domain.Activity {
        val projectRole = projectRoleRepository.findById(activityToCreate.projectRole.id)
            ?: error { "Cannot find projectRole with id = ${activityToCreate.projectRole.id}" }

        val savedActivity = activityRepository.save(Activity.of(activityToCreate, projectRole))

        if (activityToCreate.hasEvidences) {
            checkAttachedEvidence(activityToCreate, evidence)
            activityEvidenceService.storeActivityEvidence(
                savedActivity.id!!, evidence!!, savedActivity.insertDate!!
            )
        }


        return savedActivity.toDomain()
    }

    fun filterActivitiesByTimeInterval(
        filterTimeInterval: TimeInterval, activities: List<Activity>
    ) = activities.map(Activity::toDomain).filter { it.isInTheTimeInterval(filterTimeInterval) }.toList()

    @Transactional(rollbackOn = [Exception::class])
    fun updateActivity(
        activityToUpdate: com.autentia.tnt.binnacle.core.domain.Activity, evidence: EvidenceDTO?
    ): com.autentia.tnt.binnacle.core.domain.Activity {
        val projectRole = projectRoleRepository.findById(activityToUpdate.projectRole.id)
            ?: error { "Cannot find projectRole with id = ${activityToUpdate.projectRole.id}" }

        val oldActivity =
            activityRepository.findById(activityToUpdate.id!!) ?: throw ActivityNotFoundException(activityToUpdate.id)

        // Update stored image
        if (activityToUpdate.hasEvidences) {
            checkAttachedEvidence(activityToUpdate, evidence)
            activityEvidenceService.storeActivityEvidence(
                activityToUpdate.id, evidence!!, oldActivity.insertDate!!
            )
        }

        // Delete stored image
        if (!activityToUpdate.hasEvidences && oldActivity.hasEvidences) {
            activityEvidenceService.deleteActivityEvidence(activityToUpdate.id, oldActivity.insertDate!!)
        }

        return activityRepository.update(Activity.of(activityToUpdate, projectRole)).toDomain()
    }

    @Transactional(rollbackOn = [Exception::class])
    fun approveActivityById(id: Long): Activity {
        val activityToApprove = activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
        if (activityToApprove.approvalState == ApprovalState.ACCEPTED || activityToApprove.approvalState == ApprovalState.NA) {
            throw InvalidActivityApprovalStateException()
        }
        if (!activityToApprove.hasEvidences){
            throw NoEvidenceInActivityException(id)
        }
        activityToApprove.approvalState = ApprovalState.ACCEPTED
        return activityRepository.update(
            activityToApprove
        )
    }

    @Transactional
    fun deleteActivityById(id: Long) {
        val activityToDelete = activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
        if (activityToDelete.hasEvidences) {
            activityEvidenceService.deleteActivityEvidence(id, activityToDelete.insertDate!!)
        }
        activityRepository.deleteById(id)
    }

    fun getProjectRoleActivities(projectRoleId: Long, userId: Long): List<Activity> =
        activityRepository.findByProjectRoleIdAndUserId(projectRoleId, userId)

    private fun checkAttachedEvidence(
        activity: com.autentia.tnt.binnacle.core.domain.Activity, evidence: EvidenceDTO?
    ) {
        if (activity.hasEvidences && evidence == null) {
            throw NoEvidenceInActivityException(
                activity.id ?: 0, "Activity sets hasEvidence to true but no evidence was found"
            )
        }
    }
}