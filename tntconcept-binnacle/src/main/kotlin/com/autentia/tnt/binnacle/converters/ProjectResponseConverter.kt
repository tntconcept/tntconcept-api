package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import jakarta.inject.Singleton

@Singleton
class ProjectResponseConverter {

    fun toProjectResponseDTO(project: Project): ProjectResponseDTO = ProjectResponseDTO(
        id = project.id,
        name = project.name,
        open = project.open,
        billable = project.billable,
        organizationId = project.organization.id
    )

    fun toProjectResponseDTO(project: com.autentia.tnt.binnacle.core.domain.Project): ProjectResponseDTO =
        ProjectResponseDTO(project.id, project.name, project.open, project.billable, project.organization.id)
}
