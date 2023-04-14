package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.DailyWorkingTime
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

internal class ActivitySummaryConverterTest {

    private val activitySummaryConverter = ActivitySummaryConverter()

    @Test
    fun `given a list of DailyWorkingTime should return list of ActivitySummaryDTO`() {
        val firstDay = LocalDateTime.now().plusMonths(2)
        val secondDay = firstDay.plusDays(1)

        val dailyWorkingTimes = listOf(
            DailyWorkingTime(firstDay.toLocalDate(), BigDecimal("25.0")),
            DailyWorkingTime(secondDay.toLocalDate(), BigDecimal("35.5"))
        )
        val expected = listOf(
            ActivitySummaryDTO(firstDay.toLocalDate(), BigDecimal("25.0")),
            ActivitySummaryDTO(secondDay.toLocalDate(), BigDecimal("35.5"))
        )

        val result = activitySummaryConverter.toListActivitySummaryDTO(dailyWorkingTimes)

        assertEquals(expected, result)
    }

}