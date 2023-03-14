package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDate

data class ActivitySummaryDTO(
    val date: LocalDate,
    val worked: Double
)
