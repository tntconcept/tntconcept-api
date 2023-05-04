package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import jakarta.inject.Singleton

@Singleton
class ProjectResponseConverter {

    fun toProjectResponseDTO(project: Project): ProjectResponseDTO = ProjectResponseDTO(
        id = project.id,
        billable = project.billable,
        name = project.name,
        open = project.open,
        organizationId = project.organization.id
    )
}