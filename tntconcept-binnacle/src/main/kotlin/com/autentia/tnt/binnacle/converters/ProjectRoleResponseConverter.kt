package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.MaxTimeAllowedDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.entities.dto.TimeInfoDTO
import jakarta.inject.Singleton


@Singleton
class ProjectRoleResponseConverter {

    fun toProjectRoleDTO(projectRole: ProjectRole): ProjectRoleDTO = ProjectRoleDTO(
        id = projectRole.id,
        name = projectRole.name,
        organizationId = projectRole.project.organization.id,
        projectId = projectRole.project.id,
        timeInfo = TimeInfoDTO(
            MaxTimeAllowedDTO(projectRole.maxTimeAllowedByYear, projectRole.maxTimeAllowedByActivity),
                    projectRole.timeUnit),
        isWorkingTime = projectRole.isWorkingTime,
        requireEvidence = projectRole.requireEvidence,
        requireApproval = projectRole.isApprovalRequired
    )

    fun toProjectRoleDTO(projectRole: com.autentia.tnt.binnacle.core.domain.ProjectRole): ProjectRoleDTO = ProjectRoleDTO(
        id = projectRole.id,
        name = projectRole.name,
        organizationId = projectRole.project.organization.id,
        projectId = projectRole.project.id,
        timeInfo = TimeInfoDTO(
            MaxTimeAllowedDTO(projectRole.getMaxTimeAllowedByYear(), projectRole.getMaxTimeAllowedByActivity()),
            projectRole.getTimeUnit()),
        isWorkingTime = projectRole.isWorkingTime,
        requireEvidence = projectRole.requireEvidence,
        requireApproval = projectRole.isApprovalRequired
    )

    fun toProjectRoleUserDTO(projectRole: ProjectRoleUser): ProjectRoleUserDTO = ProjectRoleUserDTO(
        projectRole.id,
        projectRole.name,
        projectRole.organizationId,
        projectRole.projectId,
        projectRole.requireEvidence,
        projectRole.requireApproval,
        projectRole.userId,
        TimeInfoDTO.from(projectRole.timeInfo)
    )
}