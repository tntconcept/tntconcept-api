package com.autentia.tnt.binnacle.entities.dto

import java.math.BigDecimal

data class TimeSummaryDTO(
    val year: YearAnnualBalanceDTO,
    val months: List<MonthlyBalanceDTO>,
)

data class YearAnnualBalanceDTO(
    val previous: PreviousAnnualBalanceDTO,
    val current: AnnualBalanceDTO
)
data class AnnualBalanceDTO(
    val worked: BigDecimal,
    val target: BigDecimal,
    val notRequestedVacations: BigDecimal,
    val balance: BigDecimal,
)

data class PreviousAnnualBalanceDTO(
    val worked: BigDecimal,
    val target: BigDecimal,
    val balance: BigDecimal,
)

data class MonthlyBalanceDTO(
    val workable: BigDecimal,
    val worked: BigDecimal,
    val recommended: BigDecimal,
    val balance: BigDecimal,
    val roles: List<MonthlyRolesDTO>,
    val vacations: VacationsDTO,
)
data class MonthlyRolesDTO(
    val id: Long,
    val hours: BigDecimal
)
data class VacationsDTO(
    val charged: BigDecimal,
    val enjoyed: BigDecimal
)
