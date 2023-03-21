package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date

@Singleton
class ActivityRequestBodyConverter() {

    fun mapActivityRequestBodyDTOToActivityRequestBody(activityRequestBodyDTO: ActivityRequestBodyDTO, duration: Int) =
        ActivityRequestBody(
            activityRequestBodyDTO.id,
            activityRequestBodyDTO.interval.start,
            activityRequestBodyDTO.interval.end,
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
            setActivityStart(activityRequestBody.start, projectRole),
            setActivityEnd(activityRequestBody.end, projectRole),
            activityRequestBody.duration,
            activityRequestBody.description,
            projectRole,
            user.id,
            activityRequestBody.billable,
            user.departmentId,
            insertDate,
            activityRequestBody.hasEvidences,
            setApprovalState(projectRole)
        )

    private fun setApprovalState(projectRole: ProjectRole): ApprovalState {
        var approvalState = ApprovalState.NA
        if(projectRole.isApprovalRequired) approvalState = ApprovalState.PENDING
        return approvalState
    }

    private fun isDailyProjectRole(projectRole: ProjectRole): Boolean {
        return projectRole.timeUnit == TimeUnit.DAYS
    }
    private fun setActivityStart(start: LocalDateTime, projectRole: ProjectRole): LocalDateTime {
        var startDateTime = start
        if (isDailyProjectRole(projectRole)) startDateTime = start.toLocalDate().atTime(LocalTime.MIN)
        return startDateTime
    }

    private fun setActivityEnd(end: LocalDateTime, projectRole: ProjectRole): LocalDateTime {
        var endDateTime = end
        if (isDailyProjectRole(projectRole)) endDateTime = end.toLocalDate().atTime(LocalTime.MAX)
        return endDateTime
    }
}
