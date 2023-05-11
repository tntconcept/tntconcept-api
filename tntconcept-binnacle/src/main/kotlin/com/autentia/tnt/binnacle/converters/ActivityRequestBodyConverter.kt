package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyHookDTO
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.Date

@Singleton
class ActivityRequestBodyConverter() {
    fun toActivity(
        activityRequestBody: ActivityRequestBodyDTO,
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
        insertDate: Date? = null
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