package com.autentia.tnt.binnacle.entities.dto

data class SubcontractingActivityResponseDTO (
    val billable: Boolean,
    val duration: Int,
    val description: String,
    val hasEvidences: Boolean?,
    val id: Long,
    val projectRoleId: Long,
    val interval: IntervalResponseDTO,
    val userId: Long,
    val approval: ApprovalDTO
)