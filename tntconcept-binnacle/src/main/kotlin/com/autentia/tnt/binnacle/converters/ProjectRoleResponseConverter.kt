package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import jakarta.inject.Singleton

@Singleton
class ProjectRoleResponseConverter {

    fun toProjectRoleResponseDTO(projectRole: ProjectRole): ProjectRoleResponseDTO = ProjectRoleResponseDTO(
        id = projectRole.id,
        name = projectRole.name,
        requireEvidence = projectRole.requireEvidence
    )

}
