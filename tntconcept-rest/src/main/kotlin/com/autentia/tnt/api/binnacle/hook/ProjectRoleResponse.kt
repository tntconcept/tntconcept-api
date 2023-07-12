package com.autentia.tnt.api.binnacle.hook

import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTOOld

data class ProjectRoleResponse(
    val id: Long,
    val name: String,
    val requireEvidence: Boolean,
) {
    companion object {
        fun from(projectRoleResponseDTOOld: ProjectRoleResponseDTOOld) =
            ProjectRoleResponse(
                projectRoleResponseDTOOld.id,
                projectRoleResponseDTOOld.name,
                projectRoleResponseDTOOld.requireEvidence,
            )
    }
}