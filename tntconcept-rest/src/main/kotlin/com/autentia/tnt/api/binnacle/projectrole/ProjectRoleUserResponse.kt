package com.autentia.tnt.api.binnacle.projectrole

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO

data class ProjectRoleUserResponse(
    val id: Long,
    val name: String,
    val organizationId: Long,
    val projectId: Long,
    val maxAllowed: Int,
    val remaining: Int,
    val timeUnit: TimeUnit,
    val requireEvidence: RequireEvidence,
    val requireApproval: Boolean,
    val userId: Long,
) {
    companion object {
        fun from(projectRoleUserDTO: ProjectRoleUserDTO) =
            ProjectRoleUserResponse(
                projectRoleUserDTO.id,
                projectRoleUserDTO.name,
                projectRoleUserDTO.organizationId,
                projectRoleUserDTO.projectId,
                projectRoleUserDTO.timeInfo.maxTimeAllowed.byYear,
                projectRoleUserDTO.timeInfo.userRemainingTime,
                projectRoleUserDTO.timeInfo.timeUnit,
                projectRoleUserDTO.requireEvidence,
                projectRoleUserDTO.requireApproval,
                projectRoleUserDTO.userId,
            )
    }
}