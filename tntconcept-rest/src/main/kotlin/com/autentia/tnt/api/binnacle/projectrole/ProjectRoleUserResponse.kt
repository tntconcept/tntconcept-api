package com.autentia.tnt.api.binnacle.projectrole

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO

data class ProjectRoleUserResponse(
    val id: Long,
    val name: String,
    val organizationId: Long,
    val projectId: Long,
    val timeInfo: TimeInfoResponse,
    val requireEvidence: RequireEvidence,
    val requireApproval: Boolean,
    val userId: Long
) {
    companion object {
        fun from(projectRoleUserDTO: ProjectRoleUserDTO) =
            ProjectRoleUserResponse(
                projectRoleUserDTO.id,
                projectRoleUserDTO.name,
                projectRoleUserDTO.organizationId,
                projectRoleUserDTO.projectId,
                TimeInfoResponse.from(projectRoleUserDTO.timeInfo),
                projectRoleUserDTO.requireEvidence,
                projectRoleUserDTO.requireApproval,
                projectRoleUserDTO.userId,
            )
    }
}