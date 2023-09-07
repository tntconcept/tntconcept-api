package com.autentia.tnt.api.binnacle.projectrole

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO

data class ProjectRoleResponse(
    val id: Long,
    val name: String,
    val organizationId: Long,
    val projectId: Long,
    val timeInfo: TimeInfoResponse,
    val isWorkingTime: Boolean,
    val requireEvidence: RequireEvidence,
    val requireApproval: Boolean,
) {
    companion object{
        fun from(projectRoleDTO: ProjectRoleDTO) =
            ProjectRoleResponse(
                projectRoleDTO.id,
                projectRoleDTO.name,
                projectRoleDTO.organizationId,
                projectRoleDTO.projectId,
                TimeInfoResponse.from(projectRoleDTO.timeInfo),
                projectRoleDTO.isWorkingTime,
                projectRoleDTO.requireEvidence,
                projectRoleDTO.requireApproval,
            )
    }
}