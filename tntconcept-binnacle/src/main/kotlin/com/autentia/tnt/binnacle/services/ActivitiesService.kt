package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.converters.ActivitiesRequestBodyConverter
import com.autentia.tnt.binnacle.core.domain.ActivitiesRequestBody
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Deprecated("Use ActivityService instead")
@Singleton
internal class ActivitiesService(
    private val activityRepository: ActivityRepository,
    private val projectRoleRepository: ProjectRoleRepository,
    private val activityEvidenceService: ActivityEvidenceService,
    private val activityRequestBodyConverter: ActivitiesRequestBodyConverter
) {

    @Transactional
    @ReadOnly
    fun getActivityById(id: Long): Activity {
        return activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
    }

    @Transactional(rollbackOn = [Exception::class])
    fun createActivity(activityRequest: ActivitiesRequestBody, user: User): Activity {
        val projectRole = projectRoleRepository
            .findById(activityRequest.projectRoleId)
            ?: error { "Cannot find projectRole with id = ${activityRequest.projectRoleId}" }

        val savedActivity = activityRepository.save(
            activityRequestBodyConverter.mapActivityRequestBodyToActivity(
                activityRequest,
                projectRole,
                user
            )
        )

        if (activityRequest.hasImage) {
            activityEvidenceService.storeActivityEvidence(
                savedActivity.id!!,
                EvidenceDTO.from(activityRequest.imageFile!!),
                savedActivity.insertDate!!
            )
        }

        return savedActivity
    }

    @Transactional(rollbackOn = [Exception::class])
    fun updateActivity(activityRequest: ActivitiesRequestBody, user: User): Activity {
        val projectRole = projectRoleRepository
            .findById(activityRequest.projectRoleId)
            ?: error { "Cannot find projectRole with id = ${activityRequest.projectRoleId}" }

        val oldActivity = activityRepository
            .findById(activityRequest.id!!) ?: throw ActivityNotFoundException(activityRequest.id!!)

        // Update stored image
        if (activityRequest.hasImage) {
            activityEvidenceService.storeActivityEvidence(
                activityRequest.id!!,
                EvidenceDTO.from(activityRequest.imageFile!!),
                oldActivity.insertDate!!
            )
        }

        // Delete stored image
        if (!activityRequest.hasImage && oldActivity.hasEvidences) {
            activityEvidenceService.deleteActivityEvidence(activityRequest.id!!, oldActivity.insertDate!!)
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

    @Transactional
    fun deleteActivityById(id: Long) {
        val activityToDelete = activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
        if (activityToDelete.hasEvidences) {
            activityEvidenceService.deleteActivityEvidence(id, activityToDelete.insertDate!!)
        }
        activityRepository.deleteById(id)
    }

}