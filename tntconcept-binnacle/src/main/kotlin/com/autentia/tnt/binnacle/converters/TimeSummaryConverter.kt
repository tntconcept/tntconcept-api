package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.*
import com.autentia.tnt.binnacle.core.utils.toBigDecimalHours
import com.autentia.tnt.binnacle.entities.dto.AnnualBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.MonthlyBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.MonthlyRolesDTO
import com.autentia.tnt.binnacle.entities.dto.PreviousAnnualBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.TimeSummaryDTO
import com.autentia.tnt.binnacle.entities.dto.YearAnnualBalanceDTO
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.Month
import kotlin.time.Duration

@Singleton
class TimeSummaryConverter {
    fun toTimeSummaryDTO(timeSummary: TimeSummary): TimeSummaryDTO {
        val annualBalanceDTO = AnnualBalanceDTO(
            timeSummary.year.current.worked.toBigDecimalHours(),
            timeSummary.year.current.target.toBigDecimalHours(),
            timeSummary.year.current.notRequestedVacations.toBigDecimalHours(),
            timeSummary.year.current.balance.toBigDecimalHours()
        )

        val previousAnnualBalanceDTO = PreviousAnnualBalanceDTO(
            timeSummary.year.previous.worked.toBigDecimalHours(),
            timeSummary.year.previous.target.toBigDecimalHours(),
            timeSummary.year.previous.balance.toBigDecimalHours()
        )

        val monthlyBalancesDTO: List<MonthlyBalanceDTO> =
            timeSummary.months.map {
                MonthlyBalanceDTO(
                    it.value.workable.toBigDecimalHours(),
                    it.value.worked.toBigDecimalHours(),
                    it.value.recommended.toBigDecimalHours(),
                    it.value.balance.toBigDecimalHours(),
                    it.value.roles.map {entry ->  MonthlyRolesDTO(entry.id, entry.hours.toBigDecimalHours()) },
                    it.value.chargedVacations.toBigDecimalHours(),
                    it.value.consumedVacations.toBigDecimalHours(),
                )
        }
        val yearAnnualBalanceDTO = YearAnnualBalanceDTO(previousAnnualBalanceDTO, annualBalanceDTO)

        return TimeSummaryDTO(yearAnnualBalanceDTO, monthlyBalancesDTO)
    }

    fun toTimeSummary(
        workedTimeByMonth: Map<Month, Duration>,
        annualTargetWork: Duration,
        suggestWorkingTimeByMonth: Map<Month, Duration>,
        notConsumedVacations: Duration,
        workableMonthHours: List<Duration>,
        roles: Map<Month, List<MonthlyRoles>>,
        previousAnnualTargetWork : Duration,
        previousWorkedTimeByMonth : Map<Month, Duration>,
        chargedVacations: List<Vacation>,
        consumedVacations: List<LocalDate>,
    ): TimeSummary {
        val accumulatedWorkedTimeByMonth = workedTimeByMonth.values.fold(Duration.ZERO, Duration::plus)
        val previousAccumulatedWorkedTimeByMonth = previousWorkedTimeByMonth.values.fold(Duration.ZERO, Duration::plus)

        val annualBalance =
            AnnualBalance(
                accumulatedWorkedTimeByMonth,
                annualTargetWork,
                notConsumedVacations,
                accumulatedWorkedTimeByMonth - (annualTargetWork + notConsumedVacations)
            )

        val previousAnnualBalance =
            PreviousAnnualBalance(
                previousAccumulatedWorkedTimeByMonth,
                previousAnnualTargetWork,
                previousAccumulatedWorkedTimeByMonth - previousAnnualTargetWork
            )

        val monthlyBalances = mutableMapOf<Month, MonthlyBalance>()
        Month.values().forEach {
            val chargedVacationsByMonth = chargedVacations.flatMap { ite -> ite.days }.count { ite -> ite.month.value == it.value }
            val consumedVacationsByMonth = consumedVacations.count { ite -> ite.month.value == it.value }
            monthlyBalances[it] = MonthlyBalance(
                    workableMonthHours[it.value - 1],
                    workedTimeByMonth.getOrDefault(it, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(it, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(it, Duration.ZERO) - suggestWorkingTimeByMonth.getOrDefault(it, Duration.ZERO),
                        roles.getOrDefault(it, emptyList()),
                    Duration.parse((chargedVacationsByMonth*8).toString() + "h"),
                    Duration.parse((consumedVacationsByMonth*8).toString() + "h")
                )
        }

        val yearAnnualBalance = YearAnnualBalance(previousAnnualBalance, annualBalance)

        return TimeSummary(yearAnnualBalance, monthlyBalances)
    }

}
