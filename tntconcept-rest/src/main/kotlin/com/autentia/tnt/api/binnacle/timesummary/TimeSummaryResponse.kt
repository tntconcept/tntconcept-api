package com.autentia.tnt.api.binnacle.timesummary

import com.autentia.tnt.binnacle.entities.dto.*
import java.math.BigDecimal

data class TimeSummaryResponse(
    val year: YearAnnualBalanceResponse,
    val months: List<MonthlyBalanceResponse>,
) {
    companion object {
        fun from(timeSummaryDTO: TimeSummaryDTO) =
            TimeSummaryResponse(
                YearAnnualBalanceResponse.from(timeSummaryDTO.year),
                timeSummaryDTO.months.map { MonthlyBalanceResponse.from(it) },
            )
    }
}

data class YearAnnualBalanceResponse(
    val previous: PreviousAnnualBalanceResponse,
    val current: AnnualBalanceResponse
) {
    companion object {
        fun from(yearAnnualBalanceDTO: YearAnnualBalanceDTO) =
            YearAnnualBalanceResponse(
                PreviousAnnualBalanceResponse.from(yearAnnualBalanceDTO.previous),
                AnnualBalanceResponse.from(yearAnnualBalanceDTO.current),
            )
    }
}

data class AnnualBalanceResponse(
    val worked: BigDecimal,
    val target: BigDecimal,
    val notRequestedVacations: BigDecimal,
    val balance: BigDecimal,
) {
    companion object {
        fun from(annualBalanceDTO: AnnualBalanceDTO) =
            AnnualBalanceResponse(
                annualBalanceDTO.worked,
                annualBalanceDTO.target,
                annualBalanceDTO.notRequestedVacations,
                annualBalanceDTO.balance,
            )
    }
}

data class PreviousAnnualBalanceResponse(
    val worked: BigDecimal,
    val target: BigDecimal,
    val balance: BigDecimal,
) {
    companion object {
        fun from(previousAnnualBalanceDTO: PreviousAnnualBalanceDTO) =
            PreviousAnnualBalanceResponse(
                previousAnnualBalanceDTO.worked,
                previousAnnualBalanceDTO.target,
                previousAnnualBalanceDTO.balance,
            )
    }
}

data class MonthlyBalanceResponse(
    val workable: BigDecimal,
    val worked: BigDecimal,
    val recommended: BigDecimal,
    val balance: BigDecimal,
    val roles: List<MonthlyRolesResponse>,
    val vacations: VacationsResponse,
) {
    companion object {
        fun from(monthlyBalanceDTO: MonthlyBalanceDTO): MonthlyBalanceResponse =
            MonthlyBalanceResponse(
                monthlyBalanceDTO.workable,
                monthlyBalanceDTO.worked,
                monthlyBalanceDTO.recommended,
                monthlyBalanceDTO.balance,
                monthlyBalanceDTO.roles.map { MonthlyRolesResponse.from(it) },
                VacationsResponse.from(monthlyBalanceDTO.vacations),
            )
    }
}
data class MonthlyRolesResponse(
    val id: Long,
    val hours: BigDecimal
) {
    companion object {
        fun from(monthlyRolesDTO: MonthlyRolesDTO) =
            MonthlyRolesResponse(
                monthlyRolesDTO.id,
                monthlyRolesDTO.hours,
            )
    }
}
data class VacationsResponse(
    val charged: BigDecimal,
    val enjoyed: BigDecimal
) {
    companion object {
        fun from(vacationsDTO: VacationsDTO) =
            VacationsResponse(
                vacationsDTO.charged,
                vacationsDTO.enjoyed,
            )
    }
}
