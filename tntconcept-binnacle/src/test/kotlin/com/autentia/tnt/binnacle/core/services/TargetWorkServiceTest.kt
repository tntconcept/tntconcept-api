package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class TargetWorkServiceTest {

    private val sut: TargetWorkService = TargetWorkService()

    @ParameterizedTest
    @MethodSource("annualTargetWorkParametersProvider")
    fun `given different annual working time and different last year annual summary should return target work`(
        testDescription: String,
        year: Int,
        hiringDate: LocalDate,
        annualWorkingTime: Int,
        agreementYearDuration: Int,
        annualWorkSummary: AnnualWorkSummary,
        expectedTargetWork: Int
    ) {
        val targetWork =
            sut.getAnnualTargetWork(
                year,
                hiringDate,
                getDurationFromHours(annualWorkingTime),
                agreementYearDuration.toDuration(DurationUnit.MINUTES),
                annualWorkSummary
            )

        assertEquals(expectedTargetWork, targetWork.inWholeHours.toInt())
    }

    private companion object {
        @JvmStatic
        private fun annualTargetWorkParametersProvider() = listOf(
            Arguments.of(
                "given positive balance from last year, year duration from working agreement",
                2022,
                LocalDate.ofYearDay(2021, 1),
                1800,
                1765 * 60,
                AnnualWorkSummary(
                    year = 2021,
                    workedTime = getDurationFromHours(1710),
                    targetWorkingTime = getDurationFromHours(1700)
                ),
                1755,
            ),
            Arguments.of(
                "given negative balance from last year, year duration from working agreement",
                2022,
                LocalDate.ofYearDay(2021, 1),
                1800,
                1765 * 60,
                AnnualWorkSummary(
                    year = 2021,
                    workedTime = getDurationFromHours(1690),
                    targetWorkingTime = getDurationFromHours(1700)
                ),
                1775,
            ),
            Arguments.of(
                "given annual working time lower than max working effective work, year duration from working agreement",
                2021,
                LocalDate.ofYearDay(2021, 1),
                1764,
                1765 * 60,
                AnnualWorkSummary(
                    year = 2020,
                ),
                1764,
            ),
            Arguments.of(
                "given annual working time greater than max working effective work, year duration from working agreement",
                2021,
                LocalDate.ofYearDay(2021, 1),
                1900,
                1765 * 60,
                AnnualWorkSummary(
                    year = 2020,
                ),
                1765,
            ),
            Arguments.of(
                "given annual working time equals to max working effective work, year duration from working agreement",
                2021,
                LocalDate.ofYearDay(2021, 1),
                1765,
                1765 * 60,
                AnnualWorkSummary(
                    year = 2020,
                ),
                1765,
            ),
            Arguments.of(
                "given hiring date after than year",
                2021,
                LocalDate.ofYearDay(2022, 1),
                1765,
                1765 * 60,
                AnnualWorkSummary(
                    year = 2020,
                ),
                0,
            ),
        )

        private fun getDurationFromHours(hours: Int): Duration {
            return hours.toDuration(DurationUnit.HOURS)
        }
    }

}


