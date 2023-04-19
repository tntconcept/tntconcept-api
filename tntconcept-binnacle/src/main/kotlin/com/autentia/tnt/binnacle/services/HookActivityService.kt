package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
internal class HookActivityService(
    private val activityRepository: ActivityRepository,
    private val projectRoleRepository: ProjectRoleRepository,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter
) {

    @Transactional(rollbackOn = [Exception::class])
    fun createActivity(activityRequest: ActivityRequestBody, user: User): Activity {
        val projectRole = projectRoleRepository
            .findById(activityRequest.projectRoleId)
            ?: error { "Cannot find projectRole with id = ${activityRequest.projectRoleId}" }

        return activityRepository.saveWithoutSecurity(
            activityRequestBodyConverter.mapActivityRequestBodyToActivity(
                activityRequest, projectRole, user
            )
        )
    }

    @Transactional(rollbackOn = [Exception::class])
    fun updateActivity(activityRequest: ActivityRequestBody, user: User): Activity {
        val projectRole = projectRoleRepository
            .findById(activityRequest.projectRoleId)
            ?: error { "Cannot find projectRole with id = ${activityRequest.projectRoleId}" }

        val oldActivity = activityRepository
            .findByIdWithoutSecurity(activityRequest.id!!) ?: throw ActivityNotFoundException(activityRequest.id)

        return activityRepository.updateWithoutSecurity(
            activityRequestBodyConverter.mapActivityRequestBodyToActivity(
                activityRequest, projectRole, user, oldActivity.insertDate
            )
        )
    }

}