package com.autentia.tnt.binnacle.converters


import com.autentia.tnt.binnacle.core.domain.AnnualBalance
import com.autentia.tnt.binnacle.core.domain.MonthlyBalance
import com.autentia.tnt.binnacle.core.domain.MonthlyRoles
import com.autentia.tnt.binnacle.core.domain.PreviousAnnualBalance
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.domain.TimeSummary
import com.autentia.tnt.binnacle.core.domain.Vacation
import com.autentia.tnt.binnacle.core.domain.YearAnnualBalance
import com.autentia.tnt.binnacle.entities.VacationState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month
import java.time.Month.APRIL
import java.time.Month.AUGUST
import java.time.Month.DECEMBER
import java.time.Month.FEBRUARY
import java.time.Month.JANUARY
import java.time.Month.JULY
import java.time.Month.JUNE
import java.time.Month.MARCH
import java.time.Month.MAY
import java.time.Month.NOVEMBER
import java.time.Month.OCTOBER
import java.time.Month.SEPTEMBER
import java.time.Month.of
import kotlin.time.Duration

internal class TimeSummaryConverterTest {

    private var timeSummaryConverter = TimeSummaryConverter()

    @Test
    fun `given working time should return DTO with converted values`() {

        val timeSummaryDTO = timeSummaryConverter.toTimeSummaryDTO(timeSummary)

        assertEquals(getBigDecimalHours(timeSummary.year.current.worked), timeSummaryDTO.year.current.worked)
        assertEquals(getBigDecimalHours(timeSummary.year.current.target), timeSummaryDTO.year.current.target)
        assertEquals(getBigDecimalHours(timeSummary.year.current.notRequestedVacations), timeSummaryDTO.year.current.notRequestedVacations)
        assertEquals(getBigDecimalHours(timeSummary.year.current.balance), timeSummaryDTO.year.current.balance)
        assertEquals(timeSummary.months.size, timeSummaryDTO.months.size)

        timeSummaryDTO.months.mapIndexed { index, monthlyBalanceDTO ->
            val indexStartingInOne = index + 1
            assertEquals(
                getBigDecimalHours(timeSummary.months[of((indexStartingInOne).mod(12))]!!.workable),
                monthlyBalanceDTO.workable
            )
            assertEquals(
                getBigDecimalHours(timeSummary.months[of((indexStartingInOne).mod(12))]!!.worked),
                monthlyBalanceDTO.worked
            )
            assertEquals(
                getBigDecimalHours(timeSummary.months[of((indexStartingInOne).mod(12))]!!.recommended),
                monthlyBalanceDTO.recommended
            )
            assertEquals(
                getBigDecimalHours(timeSummary.months[of((indexStartingInOne).mod(12))]!!.balance),
                monthlyBalanceDTO.balance
            )
            assertEquals(
                getBigDecimalHours(timeSummary.months[of((indexStartingInOne).mod(12))]!!.enjoyedVacations),
                monthlyBalanceDTO.vacations.enjoyed
            )
        }
    }

    @Test
    fun `given work, target and suggest time should return domain WorkingTime with converted values`() {
        val workedTimeByMonth = mapOf(JANUARY to workedJanuary, FEBRUARY to workedFebruary)
        val previousWorkedTimeByMonth = mapOf(JANUARY to workedJanuary, FEBRUARY to workedFebruary)
        val suggestWorkingTimeByMonth = mapOf(JANUARY to Duration.parse("120h"), FEBRUARY to Duration.parse("140h"))

        val timeSummaryBalance = timeSummaryConverter.toTimeSummary(
            workedTimeByMonth,
            annualTargetWork,
            suggestWorkingTimeByMonth,
            notRequestedVacations,
            workableMonthlyHoursList,
            ROLES,
            previousAnnualTargetWork,
            previousWorkedTimeByMonth,
            requestedVacationsList,
            consumedVacationList
        )

        val expectedBalance = getExpectedBalance(
            workedTimeByMonth,
            annualTargetWork,
            suggestWorkingTimeByMonth,
            workableMonthlyHoursList
        )

        assertEquals(expectedBalance, timeSummaryBalance)
    }


    private companion object{

        val worked = Duration.parse("50h 30m")
        val annualTargetWork = Duration.parse("1765h")
        val previousAnnualTargetWork = Duration.parse("1765h")
        val notRequestedVacations = Duration.parse("8h")
        val chargedVacations = Duration.parse("0h")
        val enjoyedVacations = Duration.parse("0h")
        val balance = worked - (annualTargetWork + notRequestedVacations)

        val workable = Duration.parse("150h")
        val workedJanuary = Duration.parse("20h 30m")
        val workedFebruary = Duration.parse("40h 30m")
        val recommended = Duration.parse("150h 30m")
        val balanceJanuary = workedJanuary - recommended
        val balanceFebruary = workedFebruary - recommended
        val chargedVacationHoursJanuary = Duration.parse("8h")
        val chargedVacationHoursFebruary = Duration.parse("8h")
        val enjoyedVacationHoursJanuary = Duration.parse("8h")
        val enjoyedVacationHoursFebruary = Duration.parse("8h")

        val DEV_ROLE = ProjectRoleId(10L)
        val QA_ROLE = ProjectRoleId(3L)
        val DEV_OPS_ROLE = ProjectRoleId(5L)

        val rolesJanuary =
            listOf(MonthlyRoles(DEV_ROLE.id, Duration.parse("5h")), MonthlyRoles(QA_ROLE.id, Duration.parse("6h")))
        val rolesFebruary = listOf(MonthlyRoles(DEV_OPS_ROLE.id, Duration.parse("12h")))

        val ROLES = mapOf(JANUARY to rolesJanuary, FEBRUARY to rolesFebruary)

        val timeSummary = TimeSummary(
            YearAnnualBalance(
                PreviousAnnualBalance(worked, annualTargetWork, balance),
                AnnualBalance(worked, annualTargetWork, notRequestedVacations, balance)
            ),
            mapOf(
                JANUARY to MonthlyBalance(
                    workable,
                    workedJanuary,
                    recommended,
                    balanceJanuary,
                    rolesJanuary,
                    chargedVacationHoursJanuary,
                    enjoyedVacationHoursJanuary
                ),
                FEBRUARY to MonthlyBalance(
                    workable,
                    workedFebruary,
                    recommended,
                    balanceFebruary,
                    rolesFebruary,
                    chargedVacationHoursFebruary,
                    enjoyedVacationHoursFebruary
                ),
            )
        )

        val requestedVacationsList = listOf(
            Vacation(
                1L,
                "",
                "",
                VacationState.PENDING,
                LocalDate.of(2022, 1, 3),
                LocalDate.of(2022, 1, 3),
                listOf(LocalDate.of(2022, 1, 3)),
                LocalDate.of(2022, 1, 3)
            ),
            Vacation(
                1L,
                "",
                "",
                VacationState.PENDING,
                LocalDate.of(2022, 2, 3),
                LocalDate.of(2022, 2, 3),
                listOf(LocalDate.of(2022, 2, 3)),
                LocalDate.of(2022, 1, 3)
            )
        )

        val consumedVacationList = listOf(
            LocalDate.of(2022, 1, 3),
            LocalDate.of(2022, 2, 3)
        )

        val workableMonthlyHoursList: List<Duration> = listOf(
            Duration.parse("168h"),
            Duration.parse("160h"),
            Duration.parse("184h"),
            Duration.parse("168h"),
            Duration.parse("176h"),
            Duration.parse("168h"),
            Duration.parse("176h"),
            Duration.parse("168h"),
            Duration.parse("176h"),
            Duration.parse("168h"),
            Duration.parse("176h"),
            Duration.parse("176h")
        )

    }

    private fun getExpectedBalance(
        workedTimeByMonth: Map<Month, Duration>,
        annualTargetWork: Duration,
        suggestWorkingTimeByMonth: Map<Month, Duration>,
        workableMonthlyHoursList: List<Duration>
    ): TimeSummary {
        val worked = workedTimeByMonth.values.fold(Duration.ZERO, Duration::plus)
        val notRequestedVacations = Duration.parse("8h")
        val balance = worked - (annualTargetWork + notRequestedVacations)
        val previousBalance = worked - annualTargetWork

        val emptyRoles = emptyList<MonthlyRoles>()

        return TimeSummary(
            YearAnnualBalance(
                PreviousAnnualBalance(worked, annualTargetWork, previousBalance),
                AnnualBalance(worked, annualTargetWork, notRequestedVacations, balance)),
            mapOf(
                JANUARY to MonthlyBalance(
                    workableMonthlyHoursList[JANUARY.value - 1],
                    workedTimeByMonth.getOrDefault(JANUARY, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(JANUARY, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(
                        JANUARY,
                        Duration.ZERO
                    ) - suggestWorkingTimeByMonth.getOrDefault(JANUARY, Duration.ZERO),
                        rolesJanuary,
                    chargedVacationHoursJanuary,
                    enjoyedVacationHoursJanuary
                ),
                FEBRUARY to MonthlyBalance(
                    workableMonthlyHoursList[FEBRUARY.value - 1],
                    workedTimeByMonth.getOrDefault(FEBRUARY, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(FEBRUARY, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(
                        FEBRUARY,
                        Duration.ZERO
                    ) - suggestWorkingTimeByMonth.getOrDefault(FEBRUARY, Duration.ZERO),
                        rolesFebruary,
                    chargedVacationHoursFebruary,
                    enjoyedVacationHoursFebruary
                ),
                MARCH to MonthlyBalance(
                    workableMonthlyHoursList[MARCH.value - 1],
                    workedTimeByMonth.getOrDefault(MARCH, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(MARCH, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(MARCH, Duration.ZERO) - suggestWorkingTimeByMonth.getOrDefault(
                        MARCH,
                        Duration.ZERO
                    ),
                    emptyRoles,
                    chargedVacations,
                    enjoyedVacations
                ),
                APRIL to MonthlyBalance(
                    workableMonthlyHoursList[APRIL.value - 1],
                    workedTimeByMonth.getOrDefault(APRIL, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(APRIL, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(APRIL, Duration.ZERO) - suggestWorkingTimeByMonth.getOrDefault(
                        APRIL,
                        Duration.ZERO
                    ),
                    emptyRoles,
                    chargedVacations,
                    enjoyedVacations
                ),
                MAY to MonthlyBalance(
                    workableMonthlyHoursList[MAY.value - 1],
                    workedTimeByMonth.getOrDefault(MAY, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(MAY, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(MAY, Duration.ZERO) - suggestWorkingTimeByMonth.getOrDefault(
                        MAY,
                        Duration.ZERO
                    ),
                    emptyRoles,
                    chargedVacations,
                    enjoyedVacations
                ),
                JUNE to MonthlyBalance(
                    workableMonthlyHoursList[JUNE.value - 1],
                    workedTimeByMonth.getOrDefault(JUNE, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(JUNE, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(JUNE, Duration.ZERO) - suggestWorkingTimeByMonth.getOrDefault(
                        JUNE,
                        Duration.ZERO
                    ),
                    emptyRoles,
                    chargedVacations,
                    enjoyedVacations
                ),
                JULY to MonthlyBalance(
                    workableMonthlyHoursList[JULY.value - 1],
                    workedTimeByMonth.getOrDefault(JULY, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(JULY, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(JULY, Duration.ZERO) - suggestWorkingTimeByMonth.getOrDefault(
                        JULY,
                        Duration.ZERO
                    ),
                    emptyRoles,
                    chargedVacations,
                    enjoyedVacations
                ),
                AUGUST to MonthlyBalance(
                    workableMonthlyHoursList[AUGUST.value - 1],
                    workedTimeByMonth.getOrDefault(AUGUST, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(AUGUST, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(
                        AUGUST,
                        Duration.ZERO
                    ) - suggestWorkingTimeByMonth.getOrDefault(AUGUST, Duration.ZERO),
                    emptyRoles,
                    chargedVacations,
                    enjoyedVacations
                ),
                SEPTEMBER to MonthlyBalance(
                    workableMonthlyHoursList[SEPTEMBER.value - 1],
                    workedTimeByMonth.getOrDefault(SEPTEMBER, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(SEPTEMBER, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(
                        SEPTEMBER,
                        Duration.ZERO
                    ) - suggestWorkingTimeByMonth.getOrDefault(SEPTEMBER, Duration.ZERO),
                    emptyRoles,
                    chargedVacations,
                    enjoyedVacations
                ),
                OCTOBER to MonthlyBalance(
                    workableMonthlyHoursList[OCTOBER.value - 1],
                    workedTimeByMonth.getOrDefault(OCTOBER, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(OCTOBER, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(
                        OCTOBER,
                        Duration.ZERO
                    ) - suggestWorkingTimeByMonth.getOrDefault(OCTOBER, Duration.ZERO),
                    emptyRoles,
                    chargedVacations,
                    enjoyedVacations
                ),
                NOVEMBER to MonthlyBalance(
                    workableMonthlyHoursList[NOVEMBER.value - 1],
                    workedTimeByMonth.getOrDefault(NOVEMBER, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(NOVEMBER, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(
                        NOVEMBER,
                        Duration.ZERO
                    ) - suggestWorkingTimeByMonth.getOrDefault(NOVEMBER, Duration.ZERO),
                    emptyRoles,
                    chargedVacations,
                    enjoyedVacations
                ),
                DECEMBER to MonthlyBalance(
                    workableMonthlyHoursList[DECEMBER.value - 1],
                    workedTimeByMonth.getOrDefault(DECEMBER, Duration.ZERO),
                    suggestWorkingTimeByMonth.getOrDefault(DECEMBER, Duration.ZERO),
                    workedTimeByMonth.getOrDefault(
                        DECEMBER,
                        Duration.ZERO
                    ) - suggestWorkingTimeByMonth.getOrDefault(DECEMBER, Duration.ZERO),
                    emptyRoles,
                    chargedVacations,
                    enjoyedVacations
                ),
            )
        )
    }
    
    private fun getBigDecimalHours(duration: Duration): BigDecimal =
        BigDecimal.valueOf(duration.inWholeMinutes)
            .divide(BigDecimal.valueOf(60L), 2, RoundingMode.HALF_EVEN)

}
