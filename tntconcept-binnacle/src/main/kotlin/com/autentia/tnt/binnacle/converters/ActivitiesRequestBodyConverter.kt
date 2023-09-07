package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivitiesRequestBody
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.ActivitiesRequestBodyDTO
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.*

@Deprecated("Use ActivityRequestBodyConverter instead")
@Singleton
class ActivitiesRequestBodyConverter() {


    fun mapActivityRequestBodyToActivity(
        activityRequestBody: ActivitiesRequestBody,
        projectRole: ProjectRole,
        user: User,
        insertDate: Date? = null
    ) =
        Activity(
            activityRequestBody.id,
            activityRequestBody.startDate,
            setEndTime(activityRequestBody.startDate, activityRequestBody.duration),
            activityRequestBody.duration,
            activityRequestBody.description,
            projectRole,
            user.id,
            activityRequestBody.billable,
            user.departmentId,
            insertDate,
            activityRequestBody.hasImage,
            ApprovalState.NA
        )

    private fun setEndTime(startDate: LocalDateTime, duration: Int): LocalDateTime{
        return startDate.plusMinutes(duration.toLong())
    }
}