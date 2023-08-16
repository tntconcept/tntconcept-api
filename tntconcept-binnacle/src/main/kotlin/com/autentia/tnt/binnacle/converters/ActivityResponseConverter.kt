package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.core.domain.Approval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ApprovalDTO
import jakarta.inject.Singleton

@Singleton
class ActivityResponseConverter(
        private val activityIntervalResponseConverter: ActivityIntervalResponseConverter,
        private val activityEvidenceResponseConverter: ActivityEvidenceResponseConverter
) {

    fun toActivityResponseDTO(activity: com.autentia.tnt.binnacle.core.domain.Activity) = ActivityResponseDTO(
            billable = activity.billable,
            description = activity.description,
            evidences = activityEvidenceResponseConverter.getEvidencesUrls(activity),
            id = activity.id!!,
            projectRoleId = activity.projectRole.id,
            interval = activityIntervalResponseConverter.toIntervalResponseDTO(activity),
            userId = activity.userId,
            approval = ApprovalDTO(
                    state = activity.approvalState,
                    canBeApproved = activity.canBeApproved(),
                    approvedByUserId = activity.approvedByUserId,
                    approvalDate = activity.approvalDate
            )
    )

    fun mapActivityToActivityResponse(activity: Activity) = ActivityResponse(
            id = activity.id!!,
            start = activity.start,
            end = activity.end,
            billable = activity.billable,
            userId = activity.userId,
            description = activity.description,
            organization = activity.projectRole.project.organization,
            project = activity.projectRole.project,
            projectRole = activity.projectRole,
            duration = activity.duration,
            evidences =  activityEvidenceResponseConverter.getEvidencesUrls(activity),
            approval = Approval(
                    activity.approvalState,
                    activity.approvedByUserId,
                    activity.approvalDate
            )
    )

    fun toActivityResponseDTO(activityResponse: ActivityResponse) =
            ActivityResponseDTO(
                    activityResponse.billable,
                    activityResponse.description,
                    activityResponse.evidences,
                    activityResponse.id,
                    activityResponse.projectRole.id,
                    activityIntervalResponseConverter.mapActivityResponseToIntervalResponseDTO(activityResponse),
                    activityResponse.userId,
                    ApprovalDTO(
                            state = activityResponse.approval.approvalState,
                            approvedByUserId = activityResponse.approval.approvedByUserId,
                            approvalDate = activityResponse.approval.approvalDate
                    )
            )
}