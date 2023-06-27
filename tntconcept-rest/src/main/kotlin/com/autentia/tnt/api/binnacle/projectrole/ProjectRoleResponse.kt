package com.autentia.tnt.api.binnacle.projectrole

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO

data class ProjectRoleResponse(
    val id: Long,
    val name: String,
    val organizationId: Long,
    val projectId: Long,
    val maxAllowed: Int,
    val isWorkingTime: Boolean,
    val timeUnit: TimeUnit,
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
                projectRoleDTO.maxAllowed,
                projectRoleDTO.isWorkingTime,
                projectRoleDTO.timeUnit,
                projectRoleDTO.requireEvidence,
                projectRoleDTO.requireApproval,
            )
    }
}