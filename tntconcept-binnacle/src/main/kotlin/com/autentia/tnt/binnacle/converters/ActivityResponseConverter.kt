package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import jakarta.inject.Singleton

import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Singleton
class ActivityResponseConverter(
    private val organizationResponseConverter: OrganizationResponseConverter,
    private val projectResponseConverter: ProjectResponseConverter,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter
) {

    fun mapActivityToActivityResponseDTO(activity: Activity) = ActivityResponseDTO(
        id = activity.id!!,
        start = activity.interval.start,
        end = activity.interval.end,
        billable = activity.billable,
        userId = activity.userId,
        description = activity.description,
        organization = organizationResponseConverter.toOrganizationResponseDTO(activity.projectRole.project.organization),
        project = projectResponseConverter.toProjectResponseDTO(activity.projectRole.project),
        projectRole = projectRoleResponseConverter.toProjectRoleResponseDTO(activity.projectRole),
        duration = activity.duration(),
        hasEvidences = activity.hasEvidences,
        approvalState = activity.approvalState
    )

    fun mapActivityToActivityResponse(activity: Activity) = ActivityResponse(
        id = activity.id!!,
        start = activity.interval.start,
        end = activity.interval.end,
        billable = activity.billable,
        userId = activity.userId,
        description = activity.description,
        organization = activity.projectRole.project.organization,
        project = activity.projectRole.project,
        projectRole = activity.projectRole,
        duration = activity.duration(),
        hasEvidences = activity.hasEvidences,
        approvalState = activity.approvalState
    )

    fun toActivityResponseDTO(activityResponse: ActivityResponse) =
        ActivityResponseDTO(
            activityResponse.id,
            activityResponse.start,
            activityResponse.end,
            activityResponse.duration,
            activityResponse.description,
            projectRoleResponseConverter.toProjectRoleResponseDTO(activityResponse.projectRole),
            activityResponse.userId,
            activityResponse.billable,
            organizationResponseConverter.toOrganizationResponseDTO(activityResponse.organization),
            projectResponseConverter.toProjectResponseDTO(activityResponse.project),
            activityResponse.hasEvidences,
            activityResponse.approvalState
        )

    fun toActivity(activityResponse: ActivityResponse) =
        com.autentia.tnt.binnacle.core.domain.Activity(
            activityResponse.duration.toDuration(DurationUnit.MINUTES),
            activityResponse.start,
            ProjectRoleId(activityResponse.projectRole.id)
        )

}
