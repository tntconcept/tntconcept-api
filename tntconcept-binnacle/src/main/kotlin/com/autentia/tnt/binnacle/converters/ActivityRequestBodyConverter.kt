package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date

@Singleton
class ActivityRequestBodyConverter() {

    fun mapActivityRequestBodyDTOToActivityRequestBody(
        activityRequestBodyDTO: ActivityRequestBodyDTO, projectRole: ProjectRole, duration: Int
    ) =
        ActivityRequestBody(
            activityRequestBodyDTO.id,
            getDateAtTimeIfNecessary(activityRequestBodyDTO.interval.start, projectRole, LocalTime.MIN),
            getDateAtTimeIfNecessary(activityRequestBodyDTO.interval.end, projectRole, LocalTime.of(23, 59, 59)),
            duration,
            activityRequestBodyDTO.description,
            activityRequestBodyDTO.billable,
            activityRequestBodyDTO.projectRoleId,
            activityRequestBodyDTO.hasEvidences,
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

    private fun getDateAtTimeIfNecessary(
        date: LocalDateTime, projectRole: ProjectRole, localTime: LocalTime
    ): LocalDateTime = if (projectRole.timeUnit === TimeUnit.DAYS) date.toLocalDate().atTime(localTime) else date

    private fun getApprovalState(projectRole: ProjectRole) =
        if (projectRole.isApprovalRequired) ApprovalState.PENDING else ApprovalState.NA
}