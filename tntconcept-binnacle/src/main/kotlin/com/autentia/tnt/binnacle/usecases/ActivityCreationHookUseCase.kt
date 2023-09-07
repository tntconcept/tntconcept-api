package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyHookDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.UserService
import io.micronaut.validation.Validated
import jakarta.inject.Named
import jakarta.inject.Singleton
import jakarta.validation.Valid

@Singleton
@Validated
class ActivityCreationHookUseCase internal constructor(
    @param:Named("Internal") private val activityRepository: ActivityRepository,
    private val projectRoleRepository: ProjectRoleRepository,
    private val userService: UserService,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter,
    private val activityResponseConverter: ActivityResponseConverter,
) {

    fun createActivity(@Valid activityRequestBody: ActivityRequestBodyHookDTO): ActivityResponseDTO {
        val user = userService.getByUserName(activityRequestBody.userName)
        val activityRequest = activityRequestBodyConverter
            .mapActivityRequestBodyDTOToActivityRequestBody(activityRequestBody)
        val projectRole = getProjectRole(activityRequest.projectRoleId)

        val activityResponse = if (activityRequestBody.id == null) {
            createActivity(activityRequest, projectRole, user)
        } else {
            updateActivity(activityRequest, projectRole, user)
        }

        return activityResponseConverter.toActivityResponseDTO(activityResponse)
    }

    private fun createActivity(
        activityRequest: ActivityRequestBody,
        projectRole: ProjectRole,
        user: User
    ): ActivityResponse = activityResponseConverter.mapActivityToActivityResponse(
        activityRepository.save(
            activityRequestBodyConverter.mapActivityRequestBodyToActivity(
                activityRequest, projectRole, user
            )
        )
    )

    private fun updateActivity(
        activityRequest: ActivityRequestBody,
        projectRole: ProjectRole,
        user: User
    ): ActivityResponse {
        val oldActivity = activityRepository
            .findById(activityRequest.id!!) ?: throw ActivityNotFoundException(activityRequest.id)
        return activityResponseConverter.mapActivityToActivityResponse(
            activityRepository.update(
                activityRequestBodyConverter.mapActivityRequestBodyToActivity(
                    activityRequest, projectRole, user, oldActivity.insertDate
                )
            )
        )
    }

    private fun getProjectRole(projectRoleId: Long) =
        projectRoleRepository
            .findById(projectRoleId)
            ?: error { "Cannot find projectRole with id = $projectRoleId" }

}

