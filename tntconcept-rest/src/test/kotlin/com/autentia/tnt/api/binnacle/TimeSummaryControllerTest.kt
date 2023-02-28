package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.core.utils.toBigDecimalHours
import com.autentia.tnt.binnacle.entities.dto.AnnualBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.MonthlyBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.MonthlyRolesDTO
import com.autentia.tnt.binnacle.entities.dto.PreviousAnnualBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.TimeSummaryDTO
import com.autentia.tnt.binnacle.entities.dto.YearAnnualBalanceDTO
import com.autentia.tnt.binnacle.usecases.UserTimeSummaryUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import kotlin.time.Duration

internal class TimeSummaryControllerTest {

    private val timeSummaryUseCase = mock<UserTimeSummaryUseCase>()

    private val timeSummaryController = TimeSummaryController(timeSummaryUseCase)

    @Test
    fun `get working time`() {

        val date = LocalDate.of(2020, Month.FEBRUARY, 15)

        doReturn(TIME_SUMMARY_DTO).whenever(timeSummaryUseCase).getTimeSummary(date)

        val timeSummaryDTO = timeSummaryController.getTimeSummary(date)

        assertEquals(TIME_SUMMARY_DTO, timeSummaryDTO)

    }

    private companion object {

        private val workedJanuary: BigDecimal = BigDecimal.valueOf(20L)
        private val recommendedJanuary: BigDecimal = BigDecimal.valueOf(150L)
        private val balanceJanuary = workedJanuary - recommendedJanuary
        private val rolesJanuary = listOf<MonthlyRolesDTO>()

        private val workedFebruary: BigDecimal = BigDecimal.valueOf(20L)
        private val recommendedFebruary: BigDecimal = BigDecimal.valueOf(150L)
        private val balanceFebruary = workedFebruary - recommendedFebruary
        private val rolesFebruary = listOf<MonthlyRolesDTO>()

        private val workedMarch: BigDecimal = BigDecimal.valueOf(40L)
        private val recommendedMarch: BigDecimal = BigDecimal.valueOf(136L)
        private val balanceMarch = workedMarch - recommendedMarch
        private val rolesMarch = listOf<MonthlyRolesDTO>()
        private val vacation = Duration.parse("0h")

        private val worked = workedJanuary + workedFebruary + workedMarch
        private val target: BigDecimal = BigDecimal.valueOf(1765)
        private val notRequestedVacations: BigDecimal = BigDecimal.valueOf(8)
        private val balance = worked - (target + notRequestedVacations)
        private val previousBalance = worked - target

        private val TIME_SUMMARY_DTO = TimeSummaryDTO(
            YearAnnualBalanceDTO(
                PreviousAnnualBalanceDTO(
                    worked,
                    target,
                    previousBalance
                ),
                AnnualBalanceDTO(
                    worked,
                    target,
                    notRequestedVacations,
                    balance
                )
            ),
            listOf(
                MonthlyBalanceDTO(150.toBigDecimal(), workedJanuary, recommendedJanuary, balanceJanuary, rolesJanuary, vacation.toBigDecimalHours()),
                MonthlyBalanceDTO(
                    120.toBigDecimal(),
                    workedFebruary,
                    recommendedFebruary,
                    balanceFebruary,
                    rolesFebruary,
                    vacation.toBigDecimalHours()
                ),
                MonthlyBalanceDTO(110.toBigDecimal(), workedMarch, recommendedMarch, balanceMarch, rolesMarch, vacation.toBigDecimalHours()),
            )
        )

    }

}
