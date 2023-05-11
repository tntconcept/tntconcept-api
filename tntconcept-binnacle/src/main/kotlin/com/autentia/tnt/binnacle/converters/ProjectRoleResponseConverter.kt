package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import jakarta.inject.Singleton


@Singleton
class ProjectRoleResponseConverter {

    fun toProjectRoleDTO(projectRole: ProjectRole): ProjectRoleDTO = ProjectRoleDTO(
        id = projectRole.id,
        name = projectRole.name,
        organizationId = projectRole.project.organization.id,
        projectId = projectRole.project.id,
        maxAllowed = projectRole.maxAllowed,
        isWorkingTime = projectRole.isWorkingTime,
        timeUnit = projectRole.timeUnit,
        requireEvidence = projectRole.requireEvidence,
        requireApproval = projectRole.isApprovalRequired
    )

    fun toProjectRoleUserDTO(projectRole: ProjectRoleUser): ProjectRoleUserDTO = ProjectRoleUserDTO(
        id = projectRole.id,
        name = projectRole.name,
        organizationId = projectRole.organizationId,
        projectId = projectRole.projectId,
        maxAllowed = projectRole.maxAllowed,
        remaining = projectRole.remaining,
        timeUnit = projectRole.timeUnit,
        requireEvidence = projectRole.requireEvidence,
        requireApproval = projectRole.requireApproval,
        userId = projectRole.userId
    )
}