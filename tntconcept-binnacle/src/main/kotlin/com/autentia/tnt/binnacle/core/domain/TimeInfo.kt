package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit

data class TimeInfo(
    val maxTimeAllowed: MaxTimeAllowed,
    val timeUnit: TimeUnit,
) {
    fun getMaxTimeAllowedByYear() = maxTimeAllowed.byYear
    fun getMaxTimeAllowedByActivity() = maxTimeAllowed.byActivity
}

data class MaxTimeAllowed(
    val byYear: Int,
    val byActivity: Int,
)