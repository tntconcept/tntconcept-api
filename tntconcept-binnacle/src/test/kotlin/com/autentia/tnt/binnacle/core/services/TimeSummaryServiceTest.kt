package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.config.*
import com.autentia.tnt.binnacle.converters.TimeSummaryConverter
import com.autentia.tnt.binnacle.core.domain.*
import com.autentia.tnt.binnacle.core.utils.toBigDecimalHours
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import kotlin.time.Duration
import kotlin.time.DurationUnit.*
import kotlin.time.toDuration

internal class TimeSummaryServiceTest {

    private val targetWorkService = TargetWorkService()
    private val timeWorkableService = TimeWorkableService()
    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val activityCalendarService =
        ActivityCalendarService(calendarFactory, ActivitiesCalendarFactory(calendarFactory))
    private val workedTimeService = WorkedTimeService(activityCalendarService)
    private val workRecommendationService = WorkRecommendationCurrentMonthAccumulationService()
    private val timeSummaryConverter = TimeSummaryConverter()
    private val timeSummaryService = TimeSummaryService(
        targetWorkService, timeWorkableService, workedTimeService, workRecommendationService, timeSummaryConverter
    )

    @Test
    fun `calculate Working Balance applying annual working hours from agreement`() {

        val timeSummaryBalance = timeSummaryService.getTimeSummaryBalance(
            date,
            USER,
            ANNUAL_WORK_SUMMARY,
            PUBLIC_HOLIDAYS,
            vacationsEnjoyedThisYearToThisAndNextYear,
            vacationsChargedThisYearToThisAndNextYear,
            correspondingVacations,
            activities,
            previousActivities
        )

        assertEquals(
            YearAnnualBalance(
                PreviousAnnualBalance(WORKED, TARGET, PREVIOUS_BALANCE),
                AnnualBalance(WORKED, TARGET, NOT_CONSUMED_VACATIONS, BALANCE)
            ), timeSummaryBalance.year
        )
        assertEquals(12, timeSummaryBalance.months.size)
    }

    @Test
    fun `calculate workable hours for a month with all workable hours for that month requested as vacations`() {
        val user = createUser(LocalDate.of(2020, Month.MARCH, 3))
        val vacationsEnjoyedThisYear = getVacationsInOneMonth2022()

        val vacationsChargedThisYear = listOf(
            Vacation(
                state = VacationState.PENDING,
                startDate = LocalDate.of(2022, Month.JANUARY, 1),
                endDate = LocalDate.of(2022, Month.JANUARY, 31),
                days = vacationsEnjoyedThisYear,
                chargeYear = LocalDate.ofYearDay(2022, 1),
                observations = "charge year 2022 to 2022"
            )
        )

        val consumedVacationsDays = vacationsChargedThisYear.filter { it.isRequestedVacation() }.flatMap { it.days }
            .count { it.year == date.year }


        val timeSummaryBalance = timeSummaryService.getTimeSummaryBalance(
            date,
            user,
            ANNUAL_WORK_SUMMARY,
            PUBLIC_HOLIDAYS,
            vacationsEnjoyedThisYear,
            vacationsChargedThisYear,
            correspondingVacations,
            activities,
            previousActivities
        )

        val notConsumedVacations =
            Duration.parse(((correspondingVacations - consumedVacationsDays) * 8).toString() + "h")
        val balance = WORKED - (TARGET + notConsumedVacations)

        assertEquals(
            YearAnnualBalance(
                PreviousAnnualBalance(WORKED, TARGET, PREVIOUS_BALANCE),
                AnnualBalance(WORKED, TARGET, notConsumedVacations, balance)
            ), timeSummaryBalance.year
        )
        assertEquals(12, timeSummaryBalance.months.size)
    }

    @Test
    fun `return working balance for a user which the current year is his first year`() {
        val user = createUser(LocalDate.of(2020, Month.MARCH, 3))

        val timeSummaryBalance = timeSummaryService.getTimeSummaryBalance(
            date,
            user,
            ANNUAL_WORK_SUMMARY,
            PUBLIC_HOLIDAYS,
            vacationsEnjoyedThisYearToThisAndNextYear,
            vacationsChargedThisYearToThisAndNextYear,
            correspondingVacations,
            activities,
            previousActivities
        )

        assertEquals(
            YearAnnualBalance(
                PreviousAnnualBalance(WORKED, TARGET, PREVIOUS_BALANCE),
                AnnualBalance(WORKED, TARGET, NOT_CONSUMED_VACATIONS, BALANCE)
            ), timeSummaryBalance.year
        )
        assertEquals(12, timeSummaryBalance.months.size)
    }

    @Test
    fun `calculate working balance applying annual working hours from user`() {
        val user = createUser().copy(agreementYearDuration = 100000)

        val timeSummaryBalance = timeSummaryService.getTimeSummaryBalance(
            date,
            user,
            ANNUAL_WORK_SUMMARY,
            PUBLIC_HOLIDAYS,
            vacationsEnjoyedThisYearToThisAndNextYear,
            vacationsChargedThisYearToThisAndNextYear,
            correspondingVacations,
            activities,
            previousActivities
        )

        val worked = 16.toDuration(HOURS)
        val target = 69.toDuration(DAYS) + 10.toDuration(HOURS) + 40.toDuration(MINUTES)
        val balance = worked - (target + NOT_CONSUMED_VACATIONS)
        val previousBalance = worked - target

        assertEquals(
            YearAnnualBalance(
                PreviousAnnualBalance(worked, target, previousBalance),
                AnnualBalance(worked, target, NOT_CONSUMED_VACATIONS, balance)
            ), timeSummaryBalance.year
        )
        assertEquals(12, timeSummaryBalance.months.size)
    }

    @Test
    fun `return roles list grouped by month, projectRole and worked time for that role`() {
        val act1 = createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.of(LocalDate.now().year, Month.JANUARY, 8, 0, 0),
                LocalDateTime.of(LocalDate.now().year, Month.JANUARY, 8, 0, 0).plusHours(5)
            ), projectRole = createDomainProjectRole().copy(id = 10L)
        )

        val act2 = createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.of(LocalDate.now().year, Month.JANUARY, 8, 0, 0),
                LocalDateTime.of(LocalDate.now().year, Month.JANUARY, 8, 0, 0).plusHours(6)
            ),
            projectRole = createDomainProjectRole().copy(id = 10L),
        )
        val act3 = createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.of(LocalDate.now().year, Month.JANUARY, 8, 0, 0),
                LocalDateTime.of(LocalDate.now().year, Month.JANUARY, 8, 0, 0).plusHours(6)
            ),
            projectRole = createDomainProjectRole().copy(id = 3L),
        )

        val act4 = createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.of(LocalDate.now().year, Month.FEBRUARY, 8, 0, 0),
                LocalDateTime.of(LocalDate.now().year, Month.FEBRUARY, 8, 0, 0).plusHours(10)
            ),
            projectRole = createDomainProjectRole().copy(id = 5L),
        )
        val act5 = createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.of(LocalDate.now().year, Month.FEBRUARY, 8, 0, 0),
                LocalDateTime.of(LocalDate.now().year, Month.FEBRUARY, 8, 0, 0).plusHours(6)
            ),
            projectRole = createDomainProjectRole().copy(id = 1L),
        )
        val act6 = createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.of(LocalDate.now().year, Month.MARCH, 8, 0, 0),
                LocalDateTime.of(LocalDate.now().year, Month.MARCH, 8, 0, 0).plusHours(10)
            ),
            projectRole = createDomainProjectRole().copy(id = 5L),
        )
        val act7 = createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.of(LocalDate.now().year, Month.MARCH, 8, 0, 0),
                LocalDateTime.of(LocalDate.now().year, Month.MARCH, 8, 0, 0).plusHours(10)
            ),
            projectRole = createDomainProjectRole().copy(id = 5L),
        )

        val activities = listOf(act1, act2, act3, act4, act5, act6, act7)
        val workingTime = timeSummaryService.getTimeSummaryBalance(
            LocalDate.of(LocalDate.now().year, Month.MARCH, 31),
            USER,
            ANNUAL_WORK_SUMMARY,
            PUBLIC_HOLIDAYS,
            vacationsEnjoyedThisYearToThisAndNextYear,
            vacationsChargedThisYearToThisAndNextYear,
            correspondingVacations,
            activities,
            previousActivities
        )


        val JanuaryRoles = listOf(MonthlyRoles(10L, Duration.parse("11h")), MonthlyRoles(3L, Duration.parse("6h")))
        val FebRoles = listOf(MonthlyRoles(5L, Duration.parse("10h")), MonthlyRoles(1L, Duration.parse("6h")))
        val MarchRoles = listOf(MonthlyRoles(5L, Duration.parse("20h")))

        assertEquals(JanuaryRoles, workingTime.months[Month.JANUARY]!!.roles)
        assertEquals(FebRoles, workingTime.months[Month.FEBRUARY]!!.roles)
        assertEquals(MarchRoles, workingTime.months[Month.MARCH]!!.roles)
    }

    @Test
    fun `return enjoyed and charged vacations when there are no vacations for current year left`() {
        val dailyWorkingHours = 8
        val totalEnjoyedHours = (vacationsEnjoyedThisYear.size*dailyWorkingHours).toBigDecimal().intValueExact()
        val totalChargedHours = (correspondingVacations*dailyWorkingHours).toBigDecimal().intValueExact()

        val workingTime = timeSummaryService.getTimeSummaryBalance(
            LocalDate.of(LocalDate.now().year, Month.MARCH, 31),
            USER,
            ANNUAL_WORK_SUMMARY_2023,
            PUBLIC_HOLIDAYS,
            vacationsEnjoyedThisYear,
            vacationsFullyChargedToThisYear,
            correspondingVacations,
            listOf(),
            listOf()
        )

        assertEquals(
            totalEnjoyedHours,
            (workingTime.months[Month.JANUARY]!!.enjoyedVacations + workingTime.months[Month.FEBRUARY]!!.enjoyedVacations).toBigDecimalHours().intValueExact()
        )
        assertEquals(
            totalChargedHours,
            (workingTime.months[Month.JANUARY]!!.chargedVacations + workingTime.months[Month.FEBRUARY]!!.chargedVacations).toBigDecimalHours().intValueExact()
        )
    }


    private companion object {
        val USER = createUser()

        val PUBLIC_HOLIDAYS = getHolidaysFrom2022()

        val ANNUAL_WORK_SUMMARY = AnnualWorkSummary(2021)
        val ANNUAL_WORK_SUMMARY_2023 = AnnualWorkSummary(2023)

        val date = LocalDate.of(2022, Month.MARCH, 15)

        val vacationsEnjoyedThisYearToThisAndNextYear = listOf(
            LocalDate.of(2022, Month.JANUARY, 10),
            LocalDate.of(2022, Month.JANUARY, 11),
            LocalDate.of(2023, Month.JANUARY, 11),
            LocalDate.of(2023, Month.JANUARY, 11),
        )

        val vacationsChargedThisYearToThisAndNextYear = listOf(
            Vacation(
                state = VacationState.PENDING,
                startDate = LocalDate.of(2022, Month.JANUARY, 10),
                endDate = LocalDate.of(2022, Month.JANUARY, 11),
                days = listOf(
                    LocalDate.of(2022, Month.JANUARY, 10), LocalDate.of(2022, Month.JANUARY, 11)
                ),
                chargeYear = LocalDate.ofYearDay(2022, 1),
                observations = "charge year 2022 to 2022"
            ), Vacation(
                state = VacationState.PENDING,
                startDate = LocalDate.of(2023, Month.JANUARY, 10),
                endDate = LocalDate.of(2023, Month.JANUARY, 11),
                days = listOf(
                    LocalDate.of(2023, Month.JANUARY, 10), LocalDate.of(2023, Month.JANUARY, 11)
                ),
                chargeYear = LocalDate.ofYearDay(2022, 1),
                observations = "charge year 2022 to 2023"
            )
        )

        val vacationsEnjoyedThisYear = listOf(
            LocalDate.of(2023, Month.JANUARY, 10), LocalDate.of(2023, Month.JANUARY, 11),
            LocalDate.of(2023, Month.JANUARY, 12), LocalDate.of(2023, Month.JANUARY, 13),
            LocalDate.of(2023, Month.JANUARY, 16), LocalDate.of(2023, Month.JANUARY, 17),
            LocalDate.of(2023, Month.JANUARY, 18), LocalDate.of(2023, Month.JANUARY, 19),
            LocalDate.of(2023, Month.JANUARY, 20), LocalDate.of(2023, Month.JANUARY, 23),
            LocalDate.of(2023, Month.JANUARY, 24), LocalDate.of(2023, Month.JANUARY, 25),
            LocalDate.of(2023, Month.JANUARY, 26), LocalDate.of(2023, Month.JANUARY, 27),
            LocalDate.of(2023, Month.JANUARY, 30), LocalDate.of(2023, Month.JANUARY, 31),
            LocalDate.of(2023, Month.FEBRUARY, 1), LocalDate.of(2023, Month.FEBRUARY, 2),
            LocalDate.of(2023, Month.FEBRUARY, 3), LocalDate.of(2023, Month.FEBRUARY, 6),
            LocalDate.of(2023, Month.FEBRUARY, 7), LocalDate.of(2023, Month.FEBRUARY, 8),
            LocalDate.of(2023, Month.FEBRUARY, 9), LocalDate.of(2023, Month.FEBRUARY, 10),
            LocalDate.of(2023, Month.FEBRUARY, 13), LocalDate.of(2023, Month.FEBRUARY, 14)
        )

        val vacationsFullyChargedToThisYear = listOf(
            Vacation(
                state = VacationState.PENDING,
                startDate = LocalDate.of(2023, Month.JANUARY, 10),
                endDate = LocalDate.of(2023, Month.FEBRUARY, 10),
                days = listOf(
                    LocalDate.of(2023, Month.JANUARY, 10), LocalDate.of(2023, Month.JANUARY, 11),
                    LocalDate.of(2023, Month.JANUARY, 12), LocalDate.of(2023, Month.JANUARY, 13),
                    LocalDate.of(2023, Month.JANUARY, 16), LocalDate.of(2023, Month.JANUARY, 17),
                    LocalDate.of(2023, Month.JANUARY, 18), LocalDate.of(2023, Month.JANUARY, 19),
                    LocalDate.of(2023, Month.JANUARY, 20), LocalDate.of(2023, Month.JANUARY, 23),
                    LocalDate.of(2023, Month.JANUARY, 24), LocalDate.of(2023, Month.JANUARY, 25),
                    LocalDate.of(2023, Month.JANUARY, 26), LocalDate.of(2023, Month.JANUARY, 27),
                    LocalDate.of(2023, Month.JANUARY, 30), LocalDate.of(2023, Month.JANUARY, 31),
                    LocalDate.of(2023, Month.FEBRUARY, 1), LocalDate.of(2023, Month.FEBRUARY, 2),
                    LocalDate.of(2023, Month.FEBRUARY, 3), LocalDate.of(2023, Month.FEBRUARY, 6),
                    LocalDate.of(2023, Month.FEBRUARY, 7), LocalDate.of(2023, Month.FEBRUARY, 8),
                    LocalDate.of(2023, Month.FEBRUARY, 9)
                ),
                chargeYear = LocalDate.ofYearDay(2023, 1),
                observations = "charge year 2023 for all the corresponding vacations"
            )
        )

        val consumedVacationsDays =
            vacationsChargedThisYearToThisAndNextYear.filter { it.isRequestedVacation() }.flatMap { it.days }
                .count { it.year == date.year }

        const val correspondingVacations = 23

        val activities = listOf(
            createDomainActivity().copy(
                timeInterval = TimeInterval.of(
                    LocalDateTime.of(2022, Month.JANUARY, 3, 9, 0),
                    LocalDateTime.of(2022, Month.JANUARY, 3, 9, 0).plusHours(8)
                ), projectRole = createDomainProjectRole().copy(id = 1L)
            ),
            createDomainActivity().copy(
                timeInterval = TimeInterval.of(
                    LocalDateTime.of(2022, Month.JANUARY, 4, 9, 0),
                    LocalDateTime.of(2022, Month.JANUARY, 4, 9, 0).plusHours(8)
                ), projectRole = createDomainProjectRole().copy(id = 2L, isWorkingTime = false)
            ),
            createDomainActivity().copy(
                timeInterval = TimeInterval.of(
                    LocalDateTime.of(2022, Month.JANUARY, 5, 9, 0),
                    LocalDateTime.of(2022, Month.JANUARY, 5, 9, 0).plusHours(8)
                ), projectRole = createDomainProjectRole().copy(id = 3L)
            ),
        )

        val previousActivities = listOf(
            createDomainActivity().copy(
                timeInterval = TimeInterval.of(
                    LocalDateTime.of(2021, Month.JANUARY, 3, 9, 0),
                    LocalDateTime.of(2021, Month.JANUARY, 3, 9, 0).plusHours(8)
                ),
                projectRole = createDomainProjectRole().copy(id = 1L),
            ),
            createDomainActivity().copy(
                timeInterval = TimeInterval.of(
                    LocalDateTime.of(2021, Month.JANUARY, 4, 9, 0),
                    LocalDateTime.of(2021, Month.JANUARY, 4, 9, 0).plusHours(8)
                ),
                projectRole = createDomainProjectRole().copy(id = 2L, isWorkingTime = false),
            ),
            createDomainActivity().copy(
                timeInterval = TimeInterval.of(
                    LocalDateTime.of(2021, Month.JANUARY, 5, 9, 0),
                    LocalDateTime.of(2021, Month.JANUARY, 5, 9, 0).plusHours(8)
                ),
                projectRole = createDomainProjectRole().copy(id = 3L),
            ),
        )

        val WORKED = 16.toDuration(HOURS)
        val TARGET = 73.toDuration(DAYS) + 13.toDuration(HOURS)
        val NOT_CONSUMED_VACATIONS =
            Duration.parse(((correspondingVacations - consumedVacationsDays) * 8).toString() + "h")
        val BALANCE = WORKED - (TARGET + NOT_CONSUMED_VACATIONS)
        val PREVIOUS_BALANCE = WORKED - TARGET

    }
}