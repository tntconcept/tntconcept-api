package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO

data class ActivityResponse(
    val billable: Boolean,
    val description: String,
    val evidences: List<String>,
    val id: Long,
    val projectRoleId: Long,
    val interval: IntervalResponse,
    val userId: Long,
    val approval: Approval
) {
    companion object {
        fun from(activityResponseDTO: ActivityResponseDTO) =
            ActivityResponse(
                activityResponseDTO.billable,
                activityResponseDTO.description,
                activityResponseDTO.evidences,
                activityResponseDTO.id,
                activityResponseDTO.projectRoleId,
                IntervalResponse.from(activityResponseDTO.interval),
                activityResponseDTO.userId,
                Approval(
                    activityResponseDTO.approval.state,
                    activityResponseDTO.approval.canBeApproved,
                    activityResponseDTO.approval.approvedByUserId,
                    activityResponseDTO.approval.approvalDate
                )
            )
    }
}