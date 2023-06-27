package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO

data class ActivityResponse(
    val billable: Boolean,
    val description: String,
    val hasEvidences: Boolean?,
    val id: Long,
    val projectRoleId: Long,
    val interval: IntervalResponse,
    val userId: Long,
    val approvalState: ApprovalState
) {
    companion object {
        fun from(activityResponseDTO: ActivityResponseDTO) =
            ActivityResponse(
                activityResponseDTO.billable,
                activityResponseDTO.description,
                activityResponseDTO.hasEvidences,
                activityResponseDTO.id,
                activityResponseDTO.projectRoleId,
                IntervalResponse.from(activityResponseDTO.interval),
                activityResponseDTO.userId,
                activityResponseDTO.approvalState
            )
    }
}