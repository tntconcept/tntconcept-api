package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.entities.dto.RemainingTimeInfoDTO
import jakarta.inject.Singleton


@Singleton
class ProjectRoleResponseConverter {

    fun toProjectRoleDTO(projectRole: ProjectRole): ProjectRoleDTO = ProjectRoleDTO(
        id = projectRole.id,
        name = projectRole.name,
        organizationId = projectRole.project.organization.id,
        projectId = projectRole.project.id,
        maxAllowed = projectRole.maxTimeAllowedByYear,
        isWorkingTime = projectRole.isWorkingTime,
        timeUnit = projectRole.timeUnit,
        requireEvidence = projectRole.requireEvidence,
        requireApproval = projectRole.isApprovalRequired
    )

    fun toProjectRoleDTO(projectRole: com.autentia.tnt.binnacle.core.domain.ProjectRole): ProjectRoleDTO = ProjectRoleDTO(
        id = projectRole.id,
        name = projectRole.name,
        organizationId = projectRole.project.organization.id,
        projectId = projectRole.project.id,
        maxAllowed = projectRole.getMaxTimeAllowedByYear(),
        isWorkingTime = projectRole.isWorkingTime,
        timeUnit = projectRole.getTimeUnit(),
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
        RemainingTimeInfoDTO.from(projectRole.timeInfo)
    )
}