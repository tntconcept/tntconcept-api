package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit

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

data class ProjectRoleDescriptionDTO(
    val id: Long,
    val name: String,
    val requireEvidence: RequireEvidence,
    val projectId: Long,
    val maxAllowed: Int,
    val isWorkingTime: Boolean,
    val isApprovalRequired: Boolean,
    val timeUnit: TimeUnit
)
