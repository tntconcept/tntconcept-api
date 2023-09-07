package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.RequireEvidence

data class ProjectRoleUser(
    val id: Long,
    val name: String,
    val organizationId: Long,
    val projectId: Long,
    val requireEvidence: RequireEvidence,
    val requireApproval: Boolean,
    val userId: Long,
    val timeInfo: RemainingTimeInfo,
)