package com.autentia.tnt.api.binnacle.request.activity

import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.TimeIntervalRequestDTO
import jakarta.inject.Singleton

@Singleton
class ActivityRequestConverter {

    fun convertTo(activityRequest: ActivityRequest): ActivityRequestBodyDTO = ActivityRequestBodyDTO(
        id = activityRequest.id,
        interval = TimeIntervalRequestDTO(activityRequest.interval.start, activityRequest.interval.end),
        description = activityRequest.description,
        billable = activityRequest.billable,
        projectRoleId = activityRequest.projectRoleId,
        hasEvidences = activityRequest.hasEvidences,
        //evidence = if (activityRequest.evidence != null) EvidenceDTO.from(activityRequest.evidence) else null
        imageFile = activityRequest.evidence
    )
}