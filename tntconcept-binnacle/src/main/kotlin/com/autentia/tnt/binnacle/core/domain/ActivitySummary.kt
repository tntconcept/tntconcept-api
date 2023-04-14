package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDate

data class ActivitySummary(
    val date: LocalDate,
    val workedHours: Double,
)
