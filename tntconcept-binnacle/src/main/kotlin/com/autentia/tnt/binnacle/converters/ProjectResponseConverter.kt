package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectBillingTypes
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import jakarta.inject.Singleton

@Singleton
class ProjectResponseConverter {

    fun toProjectResponseDTO(project: Project): ProjectResponseDTO = ProjectResponseDTO(
        project.id,
        project.name,
        project.open,
        ProjectBillingTypes().getProjectBillingType(project.billingType),
        project.organization.id,
        project.startDate,
        project.blockDate,
        project.blockedByUser,
    )

    fun toProjectResponseDTO(project: com.autentia.tnt.binnacle.core.domain.Project): ProjectResponseDTO =
        ProjectResponseDTO(
            project.id,
            project.name,
            project.open,
            project.projectBillingType,
            project.organization.id,
            project.startDate,
            project.blockDate,
            project.blockedByUser,
        )
}
