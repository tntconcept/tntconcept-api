package com.autentia.tnt.binnacle.core.domain

import java.time.Month
import kotlin.time.Duration

data class TimeSummary(
    val year: YearAnnualBalance,
    val months: Map<Month, MonthlyBalance>,
)

data class YearAnnualBalance(
    val previous: PreviousAnnualBalance,
    val current: AnnualBalance,
)

data class AnnualBalance(
    val worked: Duration,
    val target: Duration,
    val notRequestedVacations: Duration,
    val balance: Duration,
)

data class PreviousAnnualBalance(
    val worked: Duration,
    val target: Duration,
    val balance: Duration,
)

data class MonthlyBalance(
    val workable: Duration,
    val worked: Duration,
    val recommended: Duration,
    val balance: Duration,
    val roles: List<MonthlyRoles>,
    val chargedVacations: Duration,
    val enjoyedVacations: Duration
)

data class MonthlyRoles(
    val id: Long,
    val hours: Duration
)
