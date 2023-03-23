package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.DateInterval
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.exception.ActivityAlreadyApprovedException
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalTime
import javax.transaction.Transactional

@Singleton
internal class ActivityService(
    private val activityRepository: ActivityRepository,
    private val projectRoleRepository: ProjectRoleRepository,
    private val activityImageService: ActivityImageService,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter
) {

    @Transactional
    @ReadOnly
    fun getActivityById(id: Long): Activity {
        return activityRepository.findById(id).orElseThrow { ActivityNotFoundException(id) }
    }

    @Transactional
    @ReadOnly
    fun getActivitiesBetweenDates(dateInterval: DateInterval, userId: Long): List<Activity> {
        val startDateMinHour = dateInterval.start.atTime(LocalTime.MIN)
        val endDateMaxHour = dateInterval.end.atTime(LocalTime.MAX)
        return activityRepository.getActivitiesBetweenDate(startDateMinHour, endDateMaxHour, userId)
    }

    @Transactional
    @ReadOnly
    fun getActivitiesApprovalState(approvalState: ApprovalState, userId: Long): List<Activity> {
        return activityRepository.getActivitiesApprovalState(approvalState, userId)
    }
    @Transactional
    @ReadOnly
    fun getActivitiesForAGivenProjectRoleAndUser(projectRoleId: Long, userId: Long): List<Activity> =
        activityRepository.getActivitiesForAGivenProjectRoleAndUser(projectRoleId, userId)

    @Transactional(rollbackOn = [Exception::class])
    fun createActivity(activityRequest: ActivityRequestBody, user: User): Activity {
        val projectRole = projectRoleRepository
            .findById(activityRequest.projectRoleId)
            .orElse(null) ?: error { "Cannot find projectRole with id = ${activityRequest.projectRoleId}" }

        val savedActivity = activityRepository.save(
            activityRequestBodyConverter.mapActivityRequestBodyToActivity(
                activityRequest, projectRole, user
            )
        )

        if (activityRequest.hasEvidences) {
            activityImageService.storeActivityImage(
                savedActivity.id!!,
                activityRequest.imageFile,
                savedActivity.insertDate!!
            )
        }

        return savedActivity
    }

    @Transactional(rollbackOn = [Exception::class])
    fun updateActivity(activityRequest: ActivityRequestBody, user: User): Activity {
        val projectRole = projectRoleRepository
            .findById(activityRequest.projectRoleId)
            .orElse(null) ?: error { "Cannot find projectRole with id = ${activityRequest.projectRoleId}" }

        val oldActivity = activityRepository
            .findById(activityRequest.id as Long)
            .orElseThrow { ActivityNotFoundException(activityRequest.id!!) }

        // Update stored image
        if (activityRequest.hasEvidences) {
            activityImageService.storeActivityImage(
                activityRequest.id!!,
                activityRequest.imageFile,
                oldActivity.insertDate!!
            )
        }

        // Delete stored image
        if (!activityRequest.hasEvidences && oldActivity.hasEvidences) {
            activityImageService.deleteActivityImage(activityRequest.id!!, oldActivity.insertDate!!)
        }

        return activityRepository.update(
            activityRequestBodyConverter.mapActivityRequestBodyToActivity(
                activityRequest, projectRole, user, oldActivity.insertDate
            )
        )
    }

    @Transactional(rollbackOn = [Exception::class])
    fun approveActivityById(id: Long): Activity {
        val activityToApprove = activityRepository.findById(id).orElseThrow { ActivityNotFoundException(id) }
        if (activityToApprove.approvalState == ApprovalState.ACCEPTED || activityToApprove.approvalState == ApprovalState.NA){
            throw ActivityAlreadyApprovedException()
        }
        activityToApprove.approvalState = ApprovalState.ACCEPTED
        return activityRepository.update(
            activityToApprove
        )
    }

    @Transactional
    fun deleteActivityById(id: Long) {
        val activityToDelete = activityRepository.findById(id).orElseThrow() // TODO handle exception
        if (activityToDelete.hasEvidences) {
            activityImageService.deleteActivityImage(id, activityToDelete.insertDate!!)
        }
        activityRepository.deleteById(id)
    }
}