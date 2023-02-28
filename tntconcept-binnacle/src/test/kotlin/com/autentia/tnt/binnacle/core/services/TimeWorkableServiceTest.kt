package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.config.getHolidaysFrom2022
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.Month
import kotlin.time.Duration
import kotlin.time.DurationUnit.HOURS
import kotlin.time.toDuration

internal class TimeWorkableServiceTest {

    private lateinit var sut: TimeWorkableService

    @BeforeEach
    fun setUp() {
        sut = TimeWorkableService()
    }

    @ParameterizedTest(name = "[$INDEX_PLACEHOLDER] [{0}]")
    @MethodSource("annualParametersProvider")
    fun `given different hiring dates and different earned vacations from last year should return ANNUAL workable time`(
        testDescription: String,
        vacationsAgreement: Int,
        hiringUserDate: LocalDate,
        year: Int,
        publicHolidays: List<LocalDate>,
        expectedWorkableTime: Int,
    ) {
        val workableTime = sut.getAnnualPrevisionWorkingTime(
            year,
            createUser(hiringUserDate),
            publicHolidays
        )

        assertEquals(expectedWorkableTime.toDuration(HOURS), workableTime)
    }

    @ParameterizedTest(name = "[$INDEX_PLACEHOLDER] [{0}]")
    @MethodSource("monthlyParametersProvider")
    fun `given different hiring dates and different vacations consumed should return MONTHLY workable time`(
        testDescription: String,
        year: Int,
        hiringDate: LocalDate,
        publicHolidays: List<LocalDate>,
        vacationsRequested: List<LocalDate>,
        expectedWorkableTime: Map<Month, Duration>,
    ) {
        val workableTime = sut.getMonthlyWorkingTime(
            year,
            hiringDate,
            publicHolidays,
            vacationsRequested
        )

        assertEquals(workableTime.keys.toList(), expectedWorkableTime.keys.toList())
        assertEquals(expectedWorkableTime, workableTime)
    }

    @ParameterizedTest(name = "[$INDEX_PLACEHOLDER] [{0}]")
    @MethodSource("earnedVacationsParametersProvider")
    fun `get vacations with different requested years and hiring dates`(
        testDescription: String,
        year: Int,
        hiringDate: LocalDate,
        vacationsAgreement: Int,
        expectedVacations: Int,
    ) {
        val result = sut.getEarnedVacationsSinceHiringDate(
            createUser(hiringDate),
            year
        )

        assertEquals(expectedVacations, result)
    }

    private companion object {
        @JvmStatic
        private fun earnedVacationsParametersProvider() = listOf(
            Arguments.of(
                "given requested year greater than the hiring date year and current agreement",
                2023,
                LocalDate.of(2019, Month.JANUARY, 1),
                23,
                23
            ),
            Arguments.of(
                "given requested year greater than the hiring date year and current agreement",
                2022,
                LocalDate.of(2019, Month.JANUARY, 1),
                23,
                23
            ),
            Arguments.of(
                "given requested year greater than the hiring date year and old agreement",
                2021,
                LocalDate.of(2019, Month.JANUARY, 1),
                23,
                22
            ),
            Arguments.of(
                "given requested year equals to the hiring date year",
                2019,
                LocalDate.of(2019, Month.JULY, 1),
                23,
                10
            ),
            Arguments.of(
                "given requested year lower than the hiring date year",
                2020,
                LocalDate.of(2021, Month.JUNE, 1),
                23,
                0
            )
        )

        @JvmStatic
        private fun annualParametersProvider() = listOf(
            Arguments.of(
                "given hiring date in the past",
                22,
                "2021-03-15",
                2022,
                publicHolidays,
                1792
            ),
            Arguments.of(
                "given hiring date in the current year, rounding down earned vacations",
                22,
                "2022-02-10",
                2022,
                publicHolidays,
                1600
            ),
            Arguments.of(
                "given hiring date in the current year, rounding up earned vacations",
                22,
                "2022-02-09",
                2022,
                publicHolidays,
                1608
            )
        )

        @JvmStatic
        private fun monthlyParametersProvider() = listOf(
            Arguments.of(
                "given hiring date in the past, holidays, different vacations requested",
                2022,
                "2021-03-15",
                publicHolidays,
                vacationsRequested,
                mapOf(
                    Month.JANUARY to 144.toDuration(HOURS),
                    Month.FEBRUARY to 160.toDuration(HOURS),
                    Month.MARCH to 176.toDuration(HOURS),
                    Month.APRIL to 128.toDuration(HOURS),
                    Month.MAY to 160.toDuration(HOURS),
                    Month.JUNE to 176.toDuration(HOURS),
                    Month.JULY to 160.toDuration(HOURS),
                    Month.AUGUST to 176.toDuration(HOURS),
                    Month.SEPTEMBER to 176.toDuration(HOURS),
                    Month.OCTOBER to 152.toDuration(HOURS),
                    Month.NOVEMBER to 160.toDuration(HOURS),
                    Month.DECEMBER to 144.toDuration(HOURS),
                )
            ),
            Arguments.of(
                "given hiring date in the current year, holidays, different vacations requested",
                2022,
                "2022-10-17",
                publicHolidays,
                vacationsRequested,
                mapOf(
                    Month.JANUARY to 0.toDuration(HOURS),
                    Month.FEBRUARY to 0.toDuration(HOURS),
                    Month.MARCH to 0.toDuration(HOURS),
                    Month.APRIL to 0.toDuration(HOURS),
                    Month.MAY to 0.toDuration(HOURS),
                    Month.JUNE to 0.toDuration(HOURS),
                    Month.JULY to 0.toDuration(HOURS),
                    Month.AUGUST to 0.toDuration(HOURS),
                    Month.SEPTEMBER to 0.toDuration(HOURS),
                    Month.OCTOBER to 80.toDuration(HOURS),
                    Month.NOVEMBER to 160.toDuration(HOURS),
                    Month.DECEMBER to 144.toDuration(HOURS),
                )
            ),
            Arguments.of(
                "given hiring date in the current year, holidays, vacations in all days of the month",
                2022,
                "2022-10-17",
                publicHolidays,
                getVacationsInOneMonth2022(Month.MARCH),
                mapOf(
                    Month.JANUARY to 0.toDuration(HOURS),
                    Month.FEBRUARY to 0.toDuration(HOURS),
                    Month.MARCH to 0.toDuration(HOURS),
                    Month.APRIL to 0.toDuration(HOURS),
                    Month.MAY to 0.toDuration(HOURS),
                    Month.JUNE to 0.toDuration(HOURS),
                    Month.JULY to 0.toDuration(HOURS),
                    Month.AUGUST to 0.toDuration(HOURS),
                    Month.SEPTEMBER to 0.toDuration(HOURS),
                    Month.OCTOBER to 88.toDuration(HOURS),
                    Month.NOVEMBER to 160.toDuration(HOURS),
                    Month.DECEMBER to 152.toDuration(HOURS),
                )
            )
        )


        private fun getVacationsInOneMonth2022(month: Month): List<LocalDate> {
            val vacationsRequestedInAMonth = mutableListOf<LocalDate>()

            for (i in 1..month.length(false)) {
                vacationsRequestedInAMonth.add(LocalDate.of(2022, month, i))
            }
            return vacationsRequestedInAMonth
        }


        private val vacationsRequested = listOf(
            LocalDate.of(2022, Month.JANUARY, 10),
            LocalDate.of(2022, Month.JANUARY, 11),
            LocalDate.of(2022, Month.MARCH, 17),
            LocalDate.of(2022, Month.APRIL, 11),
            LocalDate.of(2022, Month.APRIL, 12),
            LocalDate.of(2022, Month.APRIL, 13),
            LocalDate.of(2022, Month.APRIL, 14),
            LocalDate.of(2022, Month.APRIL, 15),
            LocalDate.of(2022, Month.APRIL, 16),
            LocalDate.of(2022, Month.APRIL, 17),
            LocalDate.of(2022, Month.OCTOBER, 20),
            LocalDate.of(2022, Month.DECEMBER, 26),
            LocalDate.of(2022, Month.DECEMBER, 27),
        )

        private val publicHolidays = getHolidaysFrom2022()
    }

}
