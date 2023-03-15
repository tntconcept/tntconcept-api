package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.DailyWorkingTime
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalDateTime

internal class ActivitySummaryConverterTest {

    private val activitySummaryConverter = ActivitySummaryConverter()

    @Test
    fun `given a list of DailyWorkingTime should return list of ActivitySummaryDTO` (){
        val firstDay = LocalDateTime.now().plusMonths(2)
        val secondDay = firstDay.plusDays(1)

        val dailyWorkingTimes = listOf(
            DailyWorkingTime(firstDay.toLocalDate(), 25.0),
            DailyWorkingTime(secondDay.toLocalDate(), 35.5)
        )
        val expected = listOf(
            ActivitySummaryDTO(firstDay.toLocalDate(), 25.0),
            ActivitySummaryDTO(secondDay.toLocalDate(), 35.5)
        )

        val result = activitySummaryConverter.toListActivitySummaryDTO(dailyWorkingTimes)

        assertEquals(expected, result)
    }

}