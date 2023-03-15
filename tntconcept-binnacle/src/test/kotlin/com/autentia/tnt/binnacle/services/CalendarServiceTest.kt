package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.DurationCalculator
import com.autentia.tnt.binnacle.entities.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CalendarServiceTest {


    private val date = LocalDateTime.of(2023, 3, 13, 14, 21, 26)
    private val datePlusHalfDay = date.plusHours(12)
    private val datePlusOneDay = date.plusDays(1)
    private fun provideDurationTestArguments() =
        Stream.of(
            Arguments.of(date, date, TimeUnit.MINUTES, 0),
            Arguments.of(date, date, TimeUnit.DAYS, 0),
            Arguments.of(date, datePlusHalfDay, TimeUnit.MINUTES, 720),
            Arguments.of(date, datePlusHalfDay, TimeUnit.DAYS, 0),
            Arguments.of(date, datePlusOneDay, TimeUnit.MINUTES, 1440),
            Arguments.of(date, datePlusOneDay, TimeUnit.DAYS, 480),
        )

    @ParameterizedTest
    @MethodSource("provideDurationTestArguments")
    fun `duration test`(start: LocalDateTime, end: LocalDateTime, timeUnit: TimeUnit, expectedResult: Int) {

        val actualResult = DurationCalculator.getDuration(start, end, timeUnit)
        assertEquals(expectedResult, actualResult)
    }
}