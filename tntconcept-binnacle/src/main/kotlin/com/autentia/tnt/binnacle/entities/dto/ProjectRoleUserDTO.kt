package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.RequireEvidence

data class ProjectRoleUserDTO(
    val id: Long,
    val name: String,
    val organizationId: Long,
    val projectId: Long,
    val requireEvidence: RequireEvidence,
    val requireApproval: Boolean,
    val userId: Long,
    val timeInfo: RemainingTimeInfoDTO,
)


