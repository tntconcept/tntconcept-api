package com.autentia.tnt.binnacle.core.services


import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


internal class WorkRecommendationCurrentMonthAccumulationServiceTest {

    private lateinit var sut: WorkRecommendationService

    @BeforeEach
    fun setUp() {
        sut = WorkRecommendationCurrentMonthAccumulationService()
    }

    @ParameterizedTest(name = "[$INDEX_PLACEHOLDER] [{0}]")
    @MethodSource("recommendedWorkingTimeParametersProvider")
    fun `should return recommended working time for every month`(
        testDescription: String,
        currentYearMonth: YearMonth,
        hiringDate: LocalDate,
        targetTime: String,
        workedTime: Map<Month, Duration>,
        workableTime: Map<Month, Duration>,
        expectedSuggestWorkingTimeByMonth: Map<Month, Duration>
    ) {
        // When
        val recommendedWorkingHours = sut.suggestWorkingTimeByMonth(
            currentYearMonth,
            hiringDate,
            getDurationFromHours(targetTime),
            workedTime,
            workableTime
        )

        // Then
        recommendedWorkingHours.forEach { (key, value) ->
            run {
                assertTrue(isMatchDurations(expectedSuggestWorkingTimeByMonth.getValue(key), value))
            }
        }

    }

    private companion object {
        @JvmStatic
        private fun recommendedWorkingTimeParametersProvider() = listOf(
            Arguments.of(
                "should return empty when year asked is lower than user hiring",
                YearMonth.of(2021, Month.MARCH),
                LocalDate.parse("2022-01-01"),
                "0h",
                emptyMap<Month, Duration>(),
                emptyMap<Month, Duration>(),
                emptyMap<Month, Duration>()
            ),
            Arguments.of(
                "should return recommended working time for every month",
                YearMonth.of(2021, Month.MARCH),
                LocalDate.parse("2021-01-01"),
                "1765h",
                mapOf(
                    Month.JANUARY to Duration.parse("110h"),
                    Month.FEBRUARY to Duration.parse("142.5h")
                ),
                mapOf(
                    Month.JANUARY to Duration.parse("120h"),
                    Month.FEBRUARY to Duration.parse("136h"),
                    Month.MARCH to Duration.parse("160h"),
                    Month.APRIL to Duration.parse("160h"),
                    Month.MAY to Duration.parse("160h"),
                    Month.JUNE to Duration.parse("176h"),
                    Month.JULY to Duration.parse("176h"),
                    Month.AUGUST to Duration.parse("56h"),
                    Month.SEPTEMBER to Duration.parse("176h"),
                    Month.OCTOBER to Duration.parse("160h"),
                    Month.NOVEMBER to Duration.parse("160h"),
                    Month.DECEMBER to Duration.parse("144h")
                ),
                mapOf(
                    Month.JANUARY to Duration.parse("118.72h"),
                    Month.FEBRUARY to Duration.parse("143.27h"),
                    Month.MARCH to Duration.parse("159.07h"),
                    Month.APRIL to Duration.parse("158.30h"),
                    Month.MAY to Duration.parse("158.30h"),
                    Month.JUNE to Duration.parse("174.13h"),
                    Month.JULY to Duration.parse("174.13h"),
                    Month.AUGUST to Duration.parse("55.40h"),
                    Month.SEPTEMBER to Duration.parse("174.13h"),
                    Month.OCTOBER to Duration.parse("158.30h"),
                    Month.NOVEMBER to Duration.parse("158.30h"),
                    Month.DECEMBER to Duration.parse("142.47h")
                )
            ),
            Arguments.of(
                "should return recommended working time starting when hiring is at february",
                YearMonth.of(2022, Month.FEBRUARY),
                LocalDate.parse("2022-02-09"),
                "1608h",
                emptyMap<Month, Duration>(),
                mapOf(
                    Month.FEBRUARY to Duration.parse("112h"),
                    Month.MARCH to Duration.parse("184h"),
                    Month.APRIL to Duration.parse("152h"),
                    Month.MAY to Duration.parse("160h"),
                    Month.JUNE to Duration.parse("176h"),
                    Month.JULY to Duration.parse("160h"),
                    Month.AUGUST to Duration.parse("176h"),
                    Month.SEPTEMBER to Duration.parse("176h"),
                    Month.OCTOBER to Duration.parse("160h"),
                    Month.NOVEMBER to Duration.parse("160h"),
                    Month.DECEMBER to Duration.parse("152h")
                ),
                mapOf(
                    Month.FEBRUARY to Duration.parse("101.86h"),
                    Month.MARCH to Duration.parse("167.35h"),
                    Month.APRIL to Duration.parse("138.24h"),
                    Month.MAY to Duration.parse("145.52h"),
                    Month.JUNE to Duration.parse("160.07h"),
                    Month.JULY to Duration.parse("145.52h"),
                    Month.AUGUST to Duration.parse("160.07h"),
                    Month.SEPTEMBER to Duration.parse("160.07h"),
                    Month.OCTOBER to Duration.parse("145.52h"),
                    Month.NOVEMBER to Duration.parse("145.52h"),
                    Month.DECEMBER to Duration.parse("138.24h")
                )
            ),
            Arguments.of(
                "should return recommended working time starting when hiring is at february and we are at july",
                YearMonth.of(2022, Month.JULY),
                LocalDate.parse("2022-02-09"),
                "1608h",
                mapOf(
                    Month.FEBRUARY to Duration.parse("110.5h"),
                    Month.MARCH to Duration.parse("184h"),
                    Month.APRIL to Duration.parse("160h"),
                    Month.MAY to Duration.parse("162h"),
                    Month.JUNE to Duration.parse("100h")
                ),
                mapOf(
                    Month.FEBRUARY to Duration.parse("112h"),
                    Month.MARCH to Duration.parse("184h"),
                    Month.APRIL to Duration.parse("152h"),
                    Month.MAY to Duration.parse("160h"),
                    Month.JUNE to Duration.parse("96h"),
                    Month.JULY to Duration.parse("160h"),
                    Month.AUGUST to Duration.parse("176h"),
                    Month.SEPTEMBER to Duration.parse("176h"),
                    Month.OCTOBER to Duration.parse("160h"),
                    Month.NOVEMBER to Duration.parse("160h"),
                    Month.DECEMBER to Duration.parse("152h")
                ),
                mapOf(
                    Month.FEBRUARY to Duration.parse("106.69h"),
                    Month.MARCH to Duration.parse("171.47h"),
                    Month.APRIL to Duration.parse("132.27h"),
                    Month.MAY to Duration.parse("124.68h"),
                    Month.JUNE to Duration.parse("54.14h"),
                    Month.JULY to Duration.parse("106.55h"),
                    Month.AUGUST to Duration.parse("167.66h"),
                    Month.SEPTEMBER to Duration.parse("167.66h"),
                    Month.OCTOBER to Duration.parse("152.42h"),
                    Month.NOVEMBER to Duration.parse("152.42h"),
                    Month.DECEMBER to Duration.parse("144.80h")
                )
            )

        )

        private fun getDurationFromHours(hours: String): Duration {
            return Duration.parse(hours)
        }
    }

    private fun isMatchDurations(expected: Duration, actual: Duration): Boolean {

        val error = 2.toDuration(DurationUnit.MINUTES)
        val initialRange: Duration = actual.minus(error)
        val finalRange: Duration = actual.plus(error)
        return expected.inWholeMinutes > initialRange.inWholeMinutes && expected.inWholeMinutes < finalRange.inWholeMinutes

    }

}
