package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import jakarta.inject.Singleton
import java.util.Date

@Singleton
class ActivityRequestBodyConverter() {

    fun mapActivityRequestBodyDTOToActivityRequestBody(activityRequestBodyDTO: ActivityRequestBodyDTO) =
        ActivityRequestBody(
            activityRequestBodyDTO.id,
            activityRequestBodyDTO.startDate,
            activityRequestBodyDTO.duration,
            activityRequestBodyDTO.description,
            activityRequestBodyDTO.billable,
            activityRequestBodyDTO.projectRoleId,
            activityRequestBodyDTO.hasEvidences,
            activityRequestBodyDTO.imageFile,
            activityRequestBodyDTO.approvalState
        )


    fun mapActivityRequestBodyToActivity(
        activityRequestBody: ActivityRequestBody,
        projectRole: ProjectRole,
        user: User,
        insertDate: Date? = null
    ) =
        Activity(
            activityRequestBody.id,
            activityRequestBody.startDate,
            activityRequestBody.duration,
            activityRequestBody.description,
            projectRole,
            user.id,
            activityRequestBody.billable,
            user.departmentId,
            insertDate,
            activityRequestBody.hasImage,
            activityRequestBody.approvalState
        )

    fun mapActivityRequestBodyToActivityRequestDTO(activityRequestBody: ActivityRequestBody) =
        ActivityRequestBodyDTO(
            activityRequestBody.id,
            activityRequestBody.startDate,
            activityRequestBody.duration,
            activityRequestBody.description,
            activityRequestBody.billable,
            activityRequestBody.projectRoleId,
            activityRequestBody.hasImage,
            activityRequestBody.imageFile,
            activityRequestBody.approvalState
        )

}
