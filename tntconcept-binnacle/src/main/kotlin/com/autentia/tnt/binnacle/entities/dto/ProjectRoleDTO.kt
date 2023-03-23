package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit

data class ProjectRoleDTO (
    val id: Long,
    val name: String,
    val requireEvidence: RequireEvidence
)

data class ProjectRoleResponseDTO (
    val id: Long,
    val name: String,
    val organizationId: Long,
    val projectId: Long,
    val maxAllowed: Int,
    val remaining: Int,
    val timeUnit: TimeUnit,
    val requireEvidence: RequireEvidence,
    val requireApproval: Boolean,
    val userId: Long
)
