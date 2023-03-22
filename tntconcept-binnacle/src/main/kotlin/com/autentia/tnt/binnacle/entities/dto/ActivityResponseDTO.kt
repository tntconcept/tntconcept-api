package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.ApprovalState
import java.time.LocalDateTime

data class ActivityResponseDTO(
        val billable: Boolean,
        val description: String,
        val hasEvidences: Boolean?,
        val id: Long,
        val projectRoleId: Long,
        val interval: IntervalResponseDTO,
        val userId: Long,
        val approvalState: ApprovalState
) {}
