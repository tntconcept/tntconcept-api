package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import java.math.BigDecimal
import java.time.LocalDate

data class ActivitySummaryResponse(
    val date: LocalDate,
    val worked: BigDecimal
) {
    companion object {
        fun from(activitySummaryDTO: ActivitySummaryDTO) =
            ActivitySummaryResponse(activitySummaryDTO.date, activitySummaryDTO.worked)
    }
}