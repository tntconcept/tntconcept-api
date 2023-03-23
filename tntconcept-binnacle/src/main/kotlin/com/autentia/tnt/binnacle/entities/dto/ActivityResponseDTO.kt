package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.ApprovalState
import java.time.LocalDateTime

data class ActivityResponseDTO(
    val id: Long,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val duration: Int,
    val description: String,
    val projectRole: ProjectRoleDTO,
    val userId: Long,
    val billable: Boolean,
    val organization: OrganizationResponseDTO,
    val project: ProjectResponseDTO,
    val hasEvidences: Boolean?,
    val approvalState: ApprovalState
) {}
