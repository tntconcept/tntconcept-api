package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.ApprovalState


data class ActivityDateDTO(
    val billable: Boolean,
    val description: String,
    val hasEvidences: Boolean?,
    val id: Long,
    val projectRoleId: Long,
    val interval : IntervalResponseDTO,
    val userId: Long,
    val approvalState: ApprovalState,

    )
