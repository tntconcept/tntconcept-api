package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDate

@Deprecated("Used in the deprecated Activities Controller")
data class ActivityDate(
    val date: LocalDate,
    val workedMinutes: Int,
    val activities: List<ActivitiesResponse>
)