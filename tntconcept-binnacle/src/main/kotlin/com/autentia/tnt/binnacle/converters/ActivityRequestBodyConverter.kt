package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyHookDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityRequestDTO
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.*

@Singleton
class ActivityRequestBodyConverter() {
    fun toActivity(
        activityRequestBody: ActivityRequestDTO,
        duration: Int,
        insertDate: LocalDateTime?,
        projectRole: com.autentia.tnt.binnacle.core.domain.ProjectRole,
        user: com.autentia.tnt.binnacle.core.domain.User,
    ) =
        com.autentia.tnt.binnacle.core.domain.Activity.of(
            activityRequestBody.id,
            activityRequestBody.interval.toDomain(),
            duration,
            activityRequestBody.description,
            projectRole,
            user.id,
            activityRequestBody.billable,
            user.departmentId,
            insertDate,
            activityRequestBody.hasEvidences,
            projectRole.getApprovalState(),
            activityRequestBody.evidence?.toDomain()
        )

    fun toActivity(
            subcontractingActivityRequestBody: SubcontractedActivityRequestDTO,
            insertDate: LocalDateTime?,
            projectRole: com.autentia.tnt.binnacle.core.domain.ProjectRole,
            user: com.autentia.tnt.binnacle.core.domain.User,
    ) =
            com.autentia.tnt.binnacle.core.domain.Activity.of(
                    subcontractingActivityRequestBody.id,
                    subcontractingActivityRequestBody.interval.toDomain(),
                    subcontractingActivityRequestBody.duration,
                    subcontractingActivityRequestBody.description,
                    projectRole,
                    user.id,
                    subcontractingActivityRequestBody.billable,
                    user.departmentId,
                    insertDate,
                    subcontractingActivityRequestBody.hasEvidences,
                    projectRole.getApprovalState(),
                    subcontractingActivityRequestBody.evidence?.toDomain()
            )

    fun mapActivityRequestBodyDTOToActivityRequestBody(activityRequestBodyDTO: ActivityRequestBodyHookDTO) =
        ActivityRequestBody(
            activityRequestBodyDTO.id,
            activityRequestBodyDTO.startDate,
            activityRequestBodyDTO.startDate.plusMinutes(activityRequestBodyDTO.duration.toLong()),
            activityRequestBodyDTO.duration,
            activityRequestBodyDTO.description,
            activityRequestBodyDTO.billable,
            activityRequestBodyDTO.projectRoleId,
            activityRequestBodyDTO.hasImage,
            activityRequestBodyDTO.imageFile
        )

    fun mapActivityRequestBodyToActivity(
        activityRequestBody: ActivityRequestBody,
        projectRole: ProjectRole,
        user: User,
        insertDate: Date? = null,
    ) =
        Activity(
            activityRequestBody.id,
            activityRequestBody.start,
            activityRequestBody.end,
            activityRequestBody.duration,
            activityRequestBody.description,
            projectRole,
            user.id,
            activityRequestBody.billable,
            user.departmentId,
            insertDate,
            activityRequestBody.hasEvidences,
            getApprovalState(projectRole)
        )


    private fun getApprovalState(projectRole: ProjectRole) =
        if (projectRole.isApprovalRequired) ApprovalState.PENDING else ApprovalState.NA
}