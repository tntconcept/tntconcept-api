package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDateTime

data class ActivityResponseDTO(
        val id: Long,
        val start: LocalDateTime,
        val end: LocalDateTime,
        val duration: Int,
        val description: String,
        val projectRole: ProjectRoleResponseDTO,
        val userId: Long,
        val billable: Boolean,
        val organization: OrganizationResponseDTO,
        val project: ProjectResponseDTO,
        val hasImage: Boolean?
) {}
