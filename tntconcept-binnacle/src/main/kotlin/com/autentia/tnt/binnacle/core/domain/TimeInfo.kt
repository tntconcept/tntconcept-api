package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit

data class TimeInfo(
    val maxTimeAllowedByYear: Int,
    val maxTimeAllowedByActivity: Int,
    val timeUnit: TimeUnit,
)