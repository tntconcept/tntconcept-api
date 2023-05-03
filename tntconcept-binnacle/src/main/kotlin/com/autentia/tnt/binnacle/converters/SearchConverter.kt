package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.OrganizationDescriptionDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectDescriptionDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDescriptionDTO
import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO
import jakarta.inject.Singleton

@Singleton
class SearchConverter {

    fun toResponseDTO(roles: List<ProjectRole>): SearchResponseDTO {
        val projectRoleDescription = roles.map { toResponseDTO(it) }
        val projectDescription = roles.map { toResponseDTO(it.project) }.distinctBy { it.id }
        val organizationDescription = roles.map { toResponseDTO(it.project.organization) }.distinctBy { it.id }

        return SearchResponseDTO(
            organizationDescription,
            projectDescription,
            projectRoleDescription
        )
    }

    fun toResponseDTO(project: Project) =
        ProjectDescriptionDTO(
            project.id,
            project.name,
            project.open,
            project.billable,
            project.organization.id
        )

    fun toResponseDTO(projectRole: ProjectRole) = ProjectRoleDescriptionDTO(
        projectRole.id,
        projectRole.name,
        projectRole.requireEvidence,
        projectRole.project.id,
        projectRole.maxAllowed,
        projectRole.isWorkingTime,
        projectRole.isApprovalRequired,
        projectRole.timeUnit
    )

    fun toResponseDTO(organization: Organization) = OrganizationDescriptionDTO(organization.id, organization.name)
}