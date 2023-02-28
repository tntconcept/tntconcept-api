package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivityResponse
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.converters.OrganizationResponseConverter
import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.converters.TimeSummaryConverter
import com.autentia.tnt.binnacle.core.domain.AnnualBalance
import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import com.autentia.tnt.binnacle.core.domain.MonthlyBalance
import com.autentia.tnt.binnacle.core.domain.MonthlyRoles
import com.autentia.tnt.binnacle.core.domain.PreviousAnnualBalance
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.domain.TimeSummary
import com.autentia.tnt.binnacle.core.domain.Vacation
import com.autentia.tnt.binnacle.core.domain.YearAnnualBalance
import com.autentia.tnt.binnacle.core.services.TimeSummaryService
import com.autentia.tnt.binnacle.core.utils.toBigDecimalHours
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.VacationState.ACCEPT
import com.autentia.tnt.binnacle.entities.VacationState.PENDING
import com.autentia.tnt.binnacle.entities.dto.MonthlyRolesDTO
import com.autentia.tnt.binnacle.entities.dto.AnnualBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.MonthlyBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.PreviousAnnualBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.TimeSummaryDTO
import com.autentia.tnt.binnacle.entities.dto.YearAnnualBalanceDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.AnnualWorkSummaryService
import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.binnacle.services.MyVacationsDetailService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.services.VacationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import kotlin.time.Duration
import java.math.BigDecimal
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain

internal class UserWorkTimeUseCaseTest {
    private val userService = mock<UserService> ()
    private val holidayService = mock<HolidayService> ()
    private val annualWorkSummaryService = mock<AnnualWorkSummaryService> ()
    private val activityService = mock<ActivityService> ()
    private val vacationService = mock<VacationService> ()
    private val myVacationsDetailService = mock<MyVacationsDetailService> ()
    private val workTimeService = mock<TimeSummaryService> ()

    private val userWorkTimeUseCase = UserTimeSummaryUseCase(
        userService,
        holidayService,
        annualWorkSummaryService,
        activityService,
        vacationService,
        myVacationsDetailService,
        workTimeService,
        ActivityResponseConverter(
            OrganizationResponseConverter(),
            ProjectResponseConverter(),
            ProjectRoleResponseConverter()
        ),
        TimeSummaryConverter()
    )

    @Test
    fun `given date should return working time`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(annualWorkSummary).whenever(annualWorkSummaryService).getAnnualWorkSummary(USER, TODAY_LAST_YEAR.minusYears(1).year)

        doReturn(HOLIDAYS).whenever(holidayService).findAllBetweenDate(FIRST_DAY_LAST_YEAR, LAST_DAY_LAST_YEAR)

        doReturn(vacations).whenever(vacationService).getVacationsBetweenDates(FIRST_DAY_LAST_YEAR, LAST_DAY_LAST_YEAR, USER)

        vacations.add(vacation)

        doReturn(listOf(LAST_YEAR_ACTIVITY)).whenever(activityService).getActivitiesBetweenDates(FIRST_DAY_LAST_YEAR, LAST_DAY_LAST_YEAR, USER.id)

        doReturn(CORRESPONDING_VACATIONS).whenever(myVacationsDetailService).getCorrespondingVacationDaysSinceHiringDate(
            USER, FIRST_DAY_LAST_YEAR.year)

        doReturn(vacationDaysRequestedThisYear).whenever(vacationService).getVacationsByChargeYear(FIRST_DAY_LAST_YEAR.year, USER)

        doReturn(vacationsChargedThisYear).whenever(vacationService).getVacationsByChargeYear(FIRST_DAY_LAST_YEAR.year, USER)

        doReturn(TIME_SUMMARY).whenever(workTimeService).getTimeSummaryBalance( eq(TODAY_LAST_YEAR),
            eq(USER),
            eq(annualWorkSummary),
            eq(listOf(CHRISTMAS_DATE)),
            eq(vacationDaysRequestedThisYear),
            eq(vacationsChargedThisYear),
            eq(CORRESPONDING_VACATIONS),
            any(),
            any(),
        )

        //When
        val actualWorkingTime = userWorkTimeUseCase.getTimeSummary(TODAY_LAST_YEAR)

        //Then
        verify(userService).getAuthenticatedUser()
        verify(annualWorkSummaryService).getAnnualWorkSummary(any(), any())
        verify(holidayService).findAllBetweenDate(any(), any())
        verify(vacationService).getVacationsBetweenDates(any(), any(), any())
        verify(activityService, times(2)).getActivitiesBetweenDates(any(), any(), any())
        verify(workTimeService).getTimeSummaryBalance(any(), any(), any(), any(), any(), any(), any(), any(), any())
        assertEquals(expectedTimeSummaryDTO, actualWorkingTime)
    }

    private companion object{

        private val TODAY = LocalDate.now()
        private val TOMORROW = TODAY.plusDays(1)
        private val LAST_YEAR = TODAY.minusYears(1).year
        private val TODAY_LAST_YEAR = LocalDate.of(LAST_YEAR, TODAY.month, TODAY.dayOfMonth)
        private val FIRST_DAY_LAST_YEAR = LocalDate.ofYearDay(TODAY_LAST_YEAR.year, 1)
        private val LAST_DAY_LAST_YEAR = LocalDate.of(TODAY_LAST_YEAR.year, Month.DECEMBER, 31)


        private val USER = createUser()
        private val annualWorkSummary = AnnualWorkSummary(TODAY.year)
        private const val CORRESPONDING_VACATIONS = 5

        private val CHRISTMAS_DATE = LocalDate.of(TODAY_LAST_YEAR.year, Month.DECEMBER, 25)
        private val CHRISTMAS = Holiday(
            id = 1,
            description = "Navidad",
            date = LocalDateTime.of(CHRISTMAS_DATE, LocalTime.MIN)
        )
        private val HOLIDAYS = listOf(CHRISTMAS)

        private val vacation =
            VacationDomain(
                startDate = TODAY,
                endDate = TOMORROW,
                state = ACCEPT,
                days = listOf(TODAY, TOMORROW),
                chargeYear = TODAY
        )

        private val vacations = mutableListOf<VacationDomain>()

        private val LAST_YEAR_ACTIVITY = createActivityResponse(1L, LocalDateTime.of(LAST_YEAR, TODAY.month, 2, 12, 30), false)

        val vacationsChargedThisYear = listOf(
            Vacation(
                state = PENDING,
                startDate = LocalDate.of(2022, Month.JANUARY, 10),
                endDate = LocalDate.of(2022, Month.JANUARY, 11),
                days = listOf(
                    LocalDate.of(2022, Month.JANUARY, 10),
                    LocalDate.of(2022, Month.JANUARY, 11)
                ),
                chargeYear = LocalDate.ofYearDay(2022, 1),
                observations = "charge year 2022 en 2022"
            ), Vacation(
                state = PENDING,
                startDate = LocalDate.of(2022, Month.JANUARY, 10),
                endDate = LocalDate.of(2022, Month.JANUARY, 11),
                days = listOf(
                    LocalDate.of(2022, Month.JANUARY, 10),
                    LocalDate.of(2022, Month.JANUARY, 11)
                ),
                chargeYear = LocalDate.ofYearDay(2023, 1),
                observations = "charge year 2022 en 2023"
            ),
            Vacation(
                state = PENDING,
                startDate = LocalDate.of(2023, Month.JANUARY, 10),
                endDate = LocalDate.of(2023, Month.JANUARY, 11),
                days = listOf(
                    LocalDate.of(2023, Month.JANUARY, 10),
                    LocalDate.of(2023, Month.JANUARY, 11)
                ),
                chargeYear = LocalDate.ofYearDay(2022, 1),
                observations = "charge year 2022 para 2023"
            )
        )
        val vacationDaysRequestedThisYear =
            vacations.flatMap { it.days }.filter { it.year == FIRST_DAY_LAST_YEAR.year }

        val consumedVacationsDays = vacations.filter { it.isRequestedVacation() }.flatMap {
            it.days
        }.count { it.year == TODAY_LAST_YEAR.year }

        val worked = Duration.parse("1h")
        val target = Duration.parse("1700h")
        val notConsumedVacations =
            Duration.parse(((CORRESPONDING_VACATIONS - consumedVacationsDays) * 8).toString() + "h")
        val balance = worked - (target + notConsumedVacations)

        val workable = Duration.parse("150h")
        val workedJanuary = Duration.parse("150h")
        val workedFebruary = Duration.parse("40h")
        val recommendedJanuary = Duration.parse("150h")
        val recommendedFebruary = Duration.parse("136h")
        val balanceJanuary = workedJanuary - recommendedJanuary
        val balanceFebruary = workedFebruary - recommendedFebruary

        val DEV_ROLE = ProjectRoleId(10L)
        val QA_ROLE = ProjectRoleId(3L)
        val DEV_OPS_ROLE = ProjectRoleId(5L)

        val rolesJanuary = listOf(MonthlyRoles(DEV_ROLE.id, Duration.parse("11h")))
        val rolesFebruary = listOf(MonthlyRoles(QA_ROLE.id, Duration.parse("10h")),MonthlyRoles(DEV_OPS_ROLE.id, Duration.parse("4h")) )

        val notConsumedVacationsForDTO =
            Duration.parse(((CORRESPONDING_VACATIONS - consumedVacationsDays) * 8).toString() + "h").toBigDecimalHours()

        val expectedBalance = worked.toBigDecimalHours() - (target.toBigDecimalHours() + notConsumedVacationsForDTO)
        val previousExpectedBalance = worked - target

        val TIME_SUMMARY = TimeSummary(
            YearAnnualBalance(
                PreviousAnnualBalance(worked, target, previousExpectedBalance),
                AnnualBalance(worked, target, notConsumedVacations, balance)
            ),
            mapOf(
                Month.JANUARY to MonthlyBalance(workable, workedJanuary, recommendedJanuary, balanceJanuary, rolesJanuary, Duration.parse("8h")),
                Month.FEBRUARY to MonthlyBalance(workable, workedFebruary, recommendedFebruary, balanceFebruary, rolesFebruary, Duration.parse("8h")),
            )
        )

        val expectedTimeSummaryDTO = TimeSummaryDTO(
            YearAnnualBalanceDTO(
                PreviousAnnualBalanceDTO(
                    worked.toBigDecimalHours(),
                    target.toBigDecimalHours(),
                    previousExpectedBalance.toBigDecimalHours()
                ),
                AnnualBalanceDTO(
                    worked.toBigDecimalHours(),
                    target.toBigDecimalHours(),
                    notConsumedVacationsForDTO,
                    expectedBalance
                )
            ),
            listOf(
                MonthlyBalanceDTO(
                    workable.toBigDecimalHours(),
                    workedJanuary.toBigDecimalHours(),
                    recommendedJanuary.toBigDecimalHours(),
                    balanceJanuary.toBigDecimalHours(),
                        listOf(MonthlyRolesDTO(DEV_ROLE.id, BigDecimal("11.00"))),
                    Duration.parse("8h").toBigDecimalHours()
                ),
                MonthlyBalanceDTO(
                    workable.toBigDecimalHours(),
                    workedFebruary.toBigDecimalHours(),
                    recommendedFebruary.toBigDecimalHours(),
                    balanceFebruary.toBigDecimalHours(),
                        listOf(MonthlyRolesDTO(QA_ROLE.id, BigDecimal("10.00")), MonthlyRolesDTO(DEV_OPS_ROLE.id, BigDecimal("4.00"))),
                    Duration.parse("8h").toBigDecimalHours()
                ),
            )
        )

    }
}

