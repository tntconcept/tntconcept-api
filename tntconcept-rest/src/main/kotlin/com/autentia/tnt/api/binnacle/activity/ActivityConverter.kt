package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.dto.ActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.entities.dto.TimeIntervalRequestDTO
import jakarta.inject.Singleton

@Singleton
class ActivityConverter {

    fun convertTo(activityRequest: ActivityRequest): ActivityRequestDTO = ActivityRequestDTO(
        id = activityRequest.id,
        interval = TimeIntervalRequestDTO(activityRequest.interval.start, activityRequest.interval.end),
        description = activityRequest.description,
        billable = activityRequest.billable,
        projectRoleId = activityRequest.projectRoleId,
        hasEvidences = activityRequest.hasEvidences,
        evidence = if (activityRequest.evidence != null) EvidenceDTO.from(activityRequest.evidence) else null
    )
}