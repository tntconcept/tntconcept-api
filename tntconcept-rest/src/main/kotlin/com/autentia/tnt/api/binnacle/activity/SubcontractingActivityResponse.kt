package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractingActivityResponseDTO

data class SubcontractingActivityResponse (
        val billable: Boolean,
        val duration: Int,
        val description: String,
        val hasEvidences: Boolean?,
        val id: Long,
        val projectRoleId: Long,
        val interval: IntervalResponse,
        val userId: Long,
        val approval: Approval
) {
    companion object {
        fun from(subcontractingActivityResponseDTO: SubcontractingActivityResponseDTO) =
                SubcontractingActivityResponse(
                        subcontractingActivityResponseDTO.billable,
                        subcontractingActivityResponseDTO.duration,
                        subcontractingActivityResponseDTO.description,
                        subcontractingActivityResponseDTO.hasEvidences,
                        subcontractingActivityResponseDTO.id,
                        subcontractingActivityResponseDTO.projectRoleId,
                        IntervalResponse.from(subcontractingActivityResponseDTO.interval),
                        subcontractingActivityResponseDTO.userId,
                        Approval(
                                subcontractingActivityResponseDTO.approval.state,
                                subcontractingActivityResponseDTO.approval.canBeApproved,
                                subcontractingActivityResponseDTO.approval.approvedByUserId,
                                subcontractingActivityResponseDTO.approval.approvalDate
                        )
                )
    }
}