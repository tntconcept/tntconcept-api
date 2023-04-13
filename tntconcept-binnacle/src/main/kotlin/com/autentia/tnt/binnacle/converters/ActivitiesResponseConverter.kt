package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivitiesResponse
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.dto.ActivitiesResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTOOld
import jakarta.inject.Singleton
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Deprecated("Use ActivityResponseConverter instead")
@Singleton
class ActivitiesResponseConverter(
    private val organizationResponseConverter: OrganizationResponseConverter,
    private val projectResponseConverter: ProjectResponseConverter,
) {

    fun mapActivityToActivityResponseDTO(activity: Activity) = ActivitiesResponseDTO(
        id = activity.id!!,
        startDate = activity.start,
        billable = activity.billable,
        userId = activity.userId,
        description = activity.description,
        organization = organizationResponseConverter.toOrganizationResponseDTO(activity.projectRole.project.organization),
        project = projectResponseConverter.toProjectResponseDTO(activity.projectRole.project),
        projectRole = toProjectRoleDTOOld(activity.projectRole),
        duration = activity.duration,
        hasImage = activity.hasEvidences
    )

    fun mapActivityToActivityResponse(activity: Activity) = ActivitiesResponse(
        id = activity.id!!,
        startDate = activity.start,
        billable = activity.billable,
        userId = activity.userId,
        description = activity.description,
        organization = activity.projectRole.project.organization,
        project = activity.projectRole.project,
        projectRole = activity.projectRole,
        duration = activity.duration,
        hasImage = activity.hasEvidences
    )

    fun toActivityResponseDTO(activityResponse: ActivitiesResponse) =
        ActivitiesResponseDTO(
            activityResponse.id,
            activityResponse.startDate,
            activityResponse.duration,
            activityResponse.description,
            toProjectRoleDTOOld(activityResponse.projectRole),
            activityResponse.userId,
            activityResponse.billable,
            organizationResponseConverter.toOrganizationResponseDTO(activityResponse.organization),
            projectResponseConverter.toProjectResponseDTO(activityResponse.project),
            activityResponse.hasImage,

            )

    fun toProjectRoleDTOOld(projectRole: ProjectRole): ProjectRoleResponseDTOOld {
        val requireEvidence = projectRole.requireEvidence != RequireEvidence.NO
        return ProjectRoleResponseDTOOld(projectRole.id, projectRole.name, requireEvidence)
    }
}