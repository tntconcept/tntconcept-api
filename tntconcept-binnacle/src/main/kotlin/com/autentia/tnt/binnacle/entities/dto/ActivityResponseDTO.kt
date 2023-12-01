package com.autentia.tnt.binnacle.entities.dto

data class ActivityResponseDTO(
        val billable: Boolean,
        val description: String,
        val evidences: List<String>,
        val id: Long,
        val projectRoleId: Long,
        val interval: IntervalResponseDTO,
        val userId: Long,
        val approval: ApprovalDTO
)
