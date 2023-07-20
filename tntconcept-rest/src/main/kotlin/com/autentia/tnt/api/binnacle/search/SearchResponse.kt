package com.autentia.tnt.api.binnacle.search

import com.autentia.tnt.api.binnacle.organization.OrganizationResponse
import com.autentia.tnt.api.binnacle.project.ProjectResponse
import com.autentia.tnt.api.binnacle.projectrole.ProjectRoleUserResponse
import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO

data class SearchResponse(
    val organizations: List<OrganizationResponse>,
    val projects: List<ProjectResponse>,
    val projectRoles: List<ProjectRoleUserResponse>
) {
    companion object {
        fun from(searchResponseDTO: SearchResponseDTO) =
            SearchResponse(
                searchResponseDTO.organizations.map { OrganizationResponse.from(it) },
                searchResponseDTO.projects.map { ProjectResponse.from(it) },
                searchResponseDTO.projectRoles.map { ProjectRoleUserResponse.from(it) }
            )
    }
}