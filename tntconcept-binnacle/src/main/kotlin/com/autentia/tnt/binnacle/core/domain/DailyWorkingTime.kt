package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDate

data class DailyWorkingTime(
    val date: LocalDate,
    val workedHours: Float,
)
