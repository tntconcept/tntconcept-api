package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO
import jakarta.inject.Singleton
import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser

@Singleton
class SearchConverter(
    private val projectResponseConverter: ProjectResponseConverter,
    private val organizationResponseConverter: OrganizationResponseConverter,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter
) {

    fun toResponseDTO(roles: List<ProjectRole>, projectRoleUsers: List<ProjectRoleUser>): SearchResponseDTO {
        val projectRoleDescription = projectRoleUsers.map { projectRoleResponseConverter.toProjectRoleUserDTO(it) }
        val projectDescription =
            roles.map { projectResponseConverter.toProjectResponseDTO(it.project) }.distinctBy { it.id }
                .distinctBy { it.id }
        val organizationDescription =
            roles.map { organizationResponseConverter.toOrganizationResponseDTO(it.project.organization) }
                .distinctBy { it.id }

        return SearchResponseDTO(
            organizationDescription,
            projectDescription,
            projectRoleDescription
        )
    }
}