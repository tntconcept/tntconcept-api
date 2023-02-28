package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.OrganizationDescriptionDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectDescriptionDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDescriptionDTO
import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO
import jakarta.inject.Singleton

@Singleton
class SearchConverter {

    fun toResponseDTO(roles: List<ProjectRole>): SearchResponseDTO {
        val projectRoleDescription = roles
            .map { ProjectRoleDescriptionDTO(it.id, it.name, it.project.id) }
        val projectDescription = roles
            .map { ProjectDescriptionDTO(it.project.id, it.project.name, it.project.organization.id) }
            .distinctBy { it.id }
        val organizationDescription = roles
            .map { OrganizationDescriptionDTO(it.project.organization.id, it.project.organization.name) }
            .distinctBy { it.id }

        return SearchResponseDTO(
            organizationDescription,
            projectDescription,
            projectRoleDescription
        )
    }

}
