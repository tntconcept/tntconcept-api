package com.autentia.tnt.binnacle.core.domain

import kotlin.time.Duration

data class AnnualWorkSummary(
    val year: Int,
    val workedTime: Duration = Duration.ZERO,
    val targetWorkingTime: Duration = Duration.ZERO,
    val earnedVacations: Int = 0,
    val consumedVacations: Int = 0,
    val alerts: List<AnnualWorkSummaryAlert> = emptyList(),
)
