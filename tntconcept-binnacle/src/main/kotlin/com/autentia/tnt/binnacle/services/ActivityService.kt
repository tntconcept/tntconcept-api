package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.InvalidActivityApprovalStateException
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
        return activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
    }

    @Transactional
    @ReadOnly
    fun getActivitiesBetweenDates(dateInterval: DateInterval): List<Activity> {
        val startDateMinHour = dateInterval.start.atTime(LocalTime.MIN)
        val endDateMaxHour = dateInterval.end.atTime(LocalTime.MAX)
        return activityRepository.find(startDateMinHour, endDateMaxHour)
    }

    @Transactional
    @ReadOnly
    fun getUserActivitiesBetweenDates(dateInterval: DateInterval, userId: Long): List<Activity> {
        val startDateMinHour = dateInterval.start.atTime(LocalTime.MIN)
        val endDateMaxHour = dateInterval.end.atTime(LocalTime.MAX)
        return activityRepository.findWithoutSecurity(startDateMinHour, endDateMaxHour, userId)
    }

    @Transactional
    @ReadOnly
    fun getActivitiesApprovalState(approvalState: ApprovalState): List<Activity> {
        return activityRepository.find(approvalState)
    }

    @Transactional
    @ReadOnly
    fun getActivitiesForAGivenProjectRoleAndUser(projectRoleId: Long, userId: Long): List<Activity> =
        activityRepository.find(projectRoleId)

    @Transactional(rollbackOn = [Exception::class])
    fun createActivity(activityRequest: ActivityRequestBody, user: User): Activity {
        val projectRole = projectRoleRepository
            .findById(activityRequest.projectRoleId)
            ?: error { "Cannot find projectRole with id = ${activityRequest.projectRoleId}" }

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
            ?: error { "Cannot find projectRole with id = ${activityRequest.projectRoleId}" }

        val oldActivity = activityRepository
            .findById(activityRequest.id!!) ?: throw ActivityNotFoundException(activityRequest.id)


        // Update stored image
        if (activityRequest.hasEvidences) {
            activityImageService.storeActivityImage(
                activityRequest.id,
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
        val activityToApprove = activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
        if (activityToApprove.approvalState == ApprovalState.ACCEPTED || activityToApprove.approvalState == ApprovalState.NA) {
            throw InvalidActivityApprovalStateException()
        }
        activityToApprove.approvalState = ApprovalState.ACCEPTED
        return activityRepository.update(
            activityToApprove
        )
    }

    @Transactional
    fun deleteActivityById(id: Long) {
        val activityToDelete =
            activityRepository
                .findById(id) ?: throw ActivityNotFoundException(id)
        if (activityToDelete.hasEvidences) {
            activityImageService.deleteActivityImage(id, activityToDelete.insertDate!!)
        }
        activityRepository.deleteById(id)
    }

}