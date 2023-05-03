package com.autentia.tnt.binnacle.entities.dto

data class SearchResponseDTO(

    val organizations: List<OrganizationResponseDTO>,
    val projects: List<ProjectResponseDTO>,
    val projectRoles: List<ProjectRoleUserDTO>
)