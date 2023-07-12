package com.autentia.tnt.api.binnacle.hook

import com.autentia.tnt.api.binnacle.organization.OrganizationResponse
import com.autentia.tnt.api.binnacle.project.ProjectResponse
import com.autentia.tnt.binnacle.entities.dto.ActivitiesResponseDTO
import java.time.LocalDateTime

data class ActivityResponse(
    val id: Long,
    val startDate: LocalDateTime,
    val duration: Int,
    val description: String,
    val projectRole: ProjectRoleResponse,
    val userId: Long,
    val billable: Boolean,
    val organization: OrganizationResponse,
    val project: ProjectResponse,
    val hasImage: Boolean?,
) {
    companion object {
        fun from(activitiesResponseDTO: ActivitiesResponseDTO) =
            ActivityResponse(
                activitiesResponseDTO.id,
                activitiesResponseDTO.startDate,
                activitiesResponseDTO.duration,
                activitiesResponseDTO.description,
                ProjectRoleResponse.from(activitiesResponseDTO.projectRole),
                activitiesResponseDTO.userId,
                activitiesResponseDTO.billable,
                OrganizationResponse.from(activitiesResponseDTO.organization),
                ProjectResponse.from(activitiesResponseDTO.project),
                activitiesResponseDTO.hasImage,
            )
    }
}