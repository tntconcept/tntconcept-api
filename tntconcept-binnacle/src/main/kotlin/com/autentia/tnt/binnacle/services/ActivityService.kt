package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.exception.ActivityAlreadyApprovedException
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime
import javax.transaction.Transactional

@Singleton
internal class ActivityService(
    private val activityRepository: ActivityRepository,
    private val projectRoleRepository: ProjectRoleRepository,
    private val activityImageService: ActivityImageService,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter,
    private val activityResponseConverter: ActivityResponseConverter
) {

    @Transactional
    @ReadOnly
    fun getActivityById(id: Long): Activity {
        return activityRepository.findById(id).orElseThrow { ActivityNotFoundException(id) }
    }

    @Transactional
    @ReadOnly
    fun getActivitiesBetweenDates(startDate: LocalDate, endDate: LocalDate, userId: Long): List<ActivityResponse> {
        val startDateMinHour = startDate.atTime(LocalTime.MIN)
        val endDateMaxHour = endDate.atTime(23, 59, 59)
        return activityRepository
            .getActivitiesBetweenDate(startDateMinHour, endDateMaxHour, userId)
            .map { activityResponseConverter.mapActivityToActivityResponse(it) }
    }

    @Transactional(rollbackOn = [Exception::class])
    fun createActivity(activityRequest: ActivityRequestBody, user: User): Activity {
        val projectRole = projectRoleRepository
            .findById(activityRequest.projectRoleId)
            .orElse(null) ?: error { "Cannot find projectRole with id = ${activityRequest.projectRoleId}" }

        val savedActivity = activityRepository.save(
            activityRequestBodyConverter.mapActivityRequestBodyToActivity(
                activityRequest,
                projectRole,
                user
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
            .findById(activityRequest.id!!)
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
                activityRequest,
                projectRole,
                user,
                oldActivity.insertDate
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
