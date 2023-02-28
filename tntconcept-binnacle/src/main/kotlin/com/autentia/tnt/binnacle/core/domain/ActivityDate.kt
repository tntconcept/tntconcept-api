package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDate

data class ActivityDate(
    val date: LocalDate,
    val workedMinutes: Int,
    val activities: List<ActivityResponse>
)
