package com.autentia.tnt.binnacle.entities.dto

data class SearchResponseDTO(

    val organizations: List<OrganizationDescriptionDTO>,
    val projects: List<ProjectDescriptionDTO>,
    val projectRoles: List<ProjectRoleDescriptionDTO>

)

data class OrganizationDescriptionDTO(
    val id: Long,
    val name: String
)

data class ProjectDescriptionDTO(
    val id: Long,
    val name: String,
    val open: Boolean,
    val billable: Boolean,
    val organizationId: Long
)

class ProjectRoleDescriptionDTO(
    val id: Long,
    val name: String,
    val projectId: Long
)
