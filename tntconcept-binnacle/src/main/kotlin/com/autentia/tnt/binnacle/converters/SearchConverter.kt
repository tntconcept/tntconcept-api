package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO
import jakarta.inject.Singleton

@Singleton
class SearchConverter(
    private val projectResponseConverter: ProjectResponseConverter,
    private val organizationResponseConverter: OrganizationResponseConverter,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter
) {

    fun toResponseDTO(roles: List<ProjectRole>): SearchResponseDTO {
        val projectRoleDescription = roles.map { projectRoleResponseConverter.toProjectRoleDTO(it) }
        val projectDescription =
            roles.map { projectResponseConverter.toProjectResponseDTO(it.project) }.distinctBy { it.id }
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