package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.TimeSummaryConverter
import com.autentia.tnt.binnacle.core.domain.*
import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import com.autentia.tnt.binnacle.core.domain.Vacation
import com.autentia.tnt.binnacle.core.services.TimeSummaryService
import com.autentia.tnt.binnacle.core.utils.toBigDecimalHours
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.VacationState.ACCEPT
import com.autentia.tnt.binnacle.entities.VacationState.PENDING
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.services.AnnualWorkSummaryService
import com.autentia.tnt.binnacle.services.MyVacationsDetailService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.services.VacationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import kotlin.time.Duration
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain

internal class UserTimeSummaryUseCaseTest {
    private val userService = mock<UserService>()
    private val holidayRepository = mock<HolidayRepository>()
    private val annualWorkSummaryService = mock<AnnualWorkSummaryService>()
    private val activityRepository = mock<ActivityRepository>()
    private val vacationService = mock<VacationService>()
    private val myVacationsDetailService = mock<MyVacationsDetailService>()
    private val workTimeService = mock<TimeSummaryService>()

    private val userWorkTimeUseCase = UserTimeSummaryUseCase(
        userService,
        holidayRepository,
        annualWorkSummaryService,
        activityRepository,
        vacationService,
        myVacationsDetailService,
        workTimeService,
        TimeSummaryConverter(),
    )

    @Test
    fun `given date should return working time`() {
        val userId = 1L
        whenever(userService.getAuthenticatedUser()).thenReturn(USER)
        whenever(annualWorkSummaryService.getAnnualWorkSummary(USER, TODAY_LAST_YEAR.minusYears(1).year)).thenReturn(
            annualWorkSummary
        )
        whenever(
            holidayRepository.findAllByDateBetween(
                FIRST_DAY_LAST_YEAR.atTime(LocalTime.MIN),
                LAST_DAY_LAST_YEAR.atTime(23, 59, 59)
            )
        ).thenReturn(HOLIDAYS)
        whenever(
            vacationService.getVacationsBetweenDates(
                FIRST_DAY_LAST_YEAR,
                LAST_DAY_LAST_YEAR
            )
        ).thenReturn(vacations)
        vacations.add(vacation)
        whenever(
            activityRepository.findByUserId(
                    FIRST_DAY_LAST_YEAR.atTime(LocalTime.MIN),
                    LAST_DAY_LAST_YEAR.atTime(LocalTime.MAX),
                userId
            )
        ).thenReturn(listOf(LAST_YEAR_ACTIVITY))
        whenever(
            myVacationsDetailService.getCorrespondingVacationDaysSinceHiringDate(
                USER,
                FIRST_DAY_LAST_YEAR.year
            )
        ).thenReturn(CORRESPONDING_VACATIONS)
        whenever(vacationService.getVacationsByChargeYear(FIRST_DAY_LAST_YEAR.year, USER)).thenReturn(
            vacationsChargedThisYear
        )
        whenever(
            workTimeService.getTimeSummaryBalance(
                eq(TODAY_LAST_YEAR),
                eq(USER),
                eq(annualWorkSummary),
                eq(listOf(CHRISTMAS_DATE)),
                eq(vacationDaysRequestedThisYear),
                eq(vacationsChargedThisYear),
                eq(CORRESPONDING_VACATIONS),
                any(),
                any(),
            )
        ).thenReturn(TIME_SUMMARY)

        //When
        val actualWorkingTime = userWorkTimeUseCase.getTimeSummary(TODAY_LAST_YEAR)

        //Then
        verify(userService).getAuthenticatedUser()
        verify(annualWorkSummaryService).getAnnualWorkSummary(any(), any())
        verify(holidayRepository).findAllByDateBetween(any(), any())
        verify(vacationService).getVacationsBetweenDates(any(), any(), eq(USER))
        verify(activityRepository, times(2)).findByUserId(any(), any(), any())
        verify(workTimeService).getTimeSummaryBalance(any(), any(), any(), any(), any(), any(), any(), any(), any())
        assertEquals(expectedTimeSummaryDTO, actualWorkingTime)
    }

    private companion object {

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
        private val ORGANIZATION = Organization(1L, "Dummy Organization", 1, listOf())
        private val PROJECT = Project(
            1L,
            "Dummy Project",
            open = true,
            LocalDate.now(),
            null,
            null,
            ORGANIZATION,
            listOf(),
            "NO_BILLABLE"
        )

        private val PROJECT_ROLE = ProjectRole(
            10L, "Dummy Project role", RequireEvidence.NO,
            PROJECT, 0, 0, true, false, TimeUnit.MINUTES
        )

        private val LAST_YEAR_ACTIVITY = Activity(
            1,
            LocalDateTime.of(LAST_YEAR, TODAY.month, 2, 12, 30),
            LocalDateTime.of(LAST_YEAR, TODAY.month, 2, 12, 30),
            45,
            "New activity",
            PROJECT_ROLE,
            USER.id,
            false,
            null,
            null,
            false,
            approvalState = ApprovalState.NA
        )

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
        val rolesFebruary =
            listOf(MonthlyRoles(QA_ROLE.id, Duration.parse("10h")), MonthlyRoles(DEV_OPS_ROLE.id, Duration.parse("4h")))

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
                Month.JANUARY to MonthlyBalance(workable, workedJanuary, recommendedJanuary, balanceJanuary, rolesJanuary, Duration.parse("32h"), Duration.parse("40h")),
                Month.FEBRUARY to MonthlyBalance(workable, workedFebruary, recommendedFebruary, balanceFebruary, rolesFebruary, Duration.parse("8h"), Duration.parse("8h")),
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
                    VacationsDTO(
                        Duration.parse("32h").toBigDecimalHours(),
                        Duration.parse("40h").toBigDecimalHours()
                    )
                ),
                MonthlyBalanceDTO(
                    workable.toBigDecimalHours(),
                    workedFebruary.toBigDecimalHours(),
                    recommendedFebruary.toBigDecimalHours(),
                    balanceFebruary.toBigDecimalHours(),
                    listOf(
                        MonthlyRolesDTO(QA_ROLE.id, BigDecimal("10.00")),
                        MonthlyRolesDTO(DEV_OPS_ROLE.id, BigDecimal("4.00"))
                    ),
                    VacationsDTO(
                        Duration.parse("8h").toBigDecimalHours(),
                        Duration.parse("8h").toBigDecimalHours()
                    )
                ),
            )
        )

    }
}

