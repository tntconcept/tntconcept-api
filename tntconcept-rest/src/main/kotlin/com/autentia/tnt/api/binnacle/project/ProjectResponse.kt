package com.autentia.tnt.api.binnacle.project

import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import java.time.LocalDate

data class ProjectResponse(
    val id: Long,
    val name: String,
    val open: Boolean,
    val billable: Boolean,
    val organizationId: Long,
    val startDate: LocalDate,
    val blockDate: LocalDate? = null,
    val blockedByUser: Long? = null,
) {
    companion object {
        fun from(projectResponseDTO: ProjectResponseDTO) =
            ProjectResponse(
                projectResponseDTO.id,
                projectResponseDTO.name,
                projectResponseDTO.open,
                projectResponseDTO.billable,
                projectResponseDTO.organizationId,
                projectResponseDTO.startDate,
                projectResponseDTO.blockDate,
                projectResponseDTO.blockedByUser,
            )
    }
}