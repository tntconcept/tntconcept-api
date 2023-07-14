package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.core.domain.MaxTimeAllowed
import com.autentia.tnt.binnacle.core.domain.RemainingTimeInfo
import com.autentia.tnt.binnacle.entities.TimeUnit

data class RemainingTimeInfoDTO(
    val maxTimeAllowed: MaxTimeAllowedDTO,
    val timeUnit: TimeUnit,
    val userRemainingTime: Int,
){
    companion object {
        fun from(remainingTimeInfo: RemainingTimeInfo) =
            RemainingTimeInfoDTO(
                MaxTimeAllowedDTO.from(remainingTimeInfo.maxTimeAllowed),
                remainingTimeInfo.timeUnit,
                remainingTimeInfo.userRemainingTime,
            )
    }
}

data class MaxTimeAllowedDTO(
    val byYear: Int,
    val byActivity: Int,
) {
    companion object {
        fun from(maxTimeAllowed: MaxTimeAllowed) =
            MaxTimeAllowedDTO(maxTimeAllowed.byYear, maxTimeAllowed.byActivity)
    }
}