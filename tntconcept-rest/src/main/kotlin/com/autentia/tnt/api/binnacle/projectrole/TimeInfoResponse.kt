package com.autentia.tnt.api.binnacle.projectrole

import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.MaxTimeAllowedDTO
import com.autentia.tnt.binnacle.entities.dto.TimeInfoDTO

data class TimeInfoResponse (
    val maxTimeAllowed: MaxTimeAllowedResponse,
    val timeUnit: TimeUnit,
    val userRemainingTime: Int,
    ) {
    companion object {
        fun from(timeInfoDTO: TimeInfoDTO) =
            TimeInfoResponse(
                MaxTimeAllowedResponse.from(timeInfoDTO.maxTimeAllowed),
                timeInfoDTO.timeUnit,
                timeInfoDTO.userRemainingTime,
            )
    }
}

data class MaxTimeAllowedResponse(
    val byYear: Int,
    val byActivity: Int,
) {
    companion object {
        fun from(maxTimeAllowedDTO: MaxTimeAllowedDTO) =
            MaxTimeAllowedResponse(maxTimeAllowedDTO.byYear, maxTimeAllowedDTO.byActivity)
    }
}