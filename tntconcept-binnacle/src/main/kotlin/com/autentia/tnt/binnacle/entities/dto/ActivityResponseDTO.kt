package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.ApprovalState
import java.time.LocalDateTime

data class ActivityResponseDTO(
        val id: Long,
        val startDate: LocalDateTime,
        val duration: Int,
        val description: String,
        val projectRole: ProjectRoleResponseDTO,
        val userId: Long,
        val billable: Boolean,
        val organization: OrganizationResponseDTO,
        val project: ProjectResponseDTO,
        val hasEvidences: Boolean?,
        val approvalState: ApprovalState
) {}
