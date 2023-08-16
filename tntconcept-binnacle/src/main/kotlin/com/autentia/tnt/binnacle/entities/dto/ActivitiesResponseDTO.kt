package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDateTime
@Deprecated("Use ActivityResponseDTO instead")
data class ActivitiesResponseDTO(
    val id: Long,
    val startDate: LocalDateTime,
    val duration: Int,
    val description: String,
    val projectRole: ProjectRoleResponseDTOOld,
    val userId: Long,
    val billable: Boolean,
    val organization: OrganizationResponseDTO,
    val project: ProjectResponseDTO,
)