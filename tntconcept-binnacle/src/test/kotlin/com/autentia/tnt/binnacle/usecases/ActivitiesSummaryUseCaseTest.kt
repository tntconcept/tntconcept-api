package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.converters.ActivitySummaryConverter
import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.DailyWorkingTime
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

class ActivitiesSummaryUseCaseTest {
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val activityService = mock<ActivityService>()
    private val activitiesSummaryConverter = mock<ActivitySummaryConverter>()

    private val activitiesSummaryUseCase = ActivitiesSummaryUseCase(
        activityCalendarService,
        activityService,
        activitiesSummaryConverter
    )

    @Test
    fun `get activities summary should return activity summary dto`() {
        val start = LocalDate.now()
        val end = start.plusDays(5)

        val dateInterval = DateInterval.of(
            start, end
        )
        val activitiesEntity = listOf(createActivity())
        val activitiesDomain: List<Activity> =
            activitiesEntity.map(com.autentia.tnt.binnacle.entities.Activity::toDomain)
        val dailyWorkingTime = listOf(DailyWorkingTime(start, BigDecimal(10)))
        val activitiesSummaryDto = listOf(
            ActivitySummaryDTO(
                start,
                BigDecimal(10)
            )
        )
        whenever(activityService.getActivitiesBetweenDates(dateInterval)).thenReturn(activitiesEntity)
        whenever(activityCalendarService.getActivityDurationSummaryInHours(activitiesDomain, dateInterval)).thenReturn(
            dailyWorkingTime
        )
        whenever(activitiesSummaryConverter.toListActivitySummaryDTO(dailyWorkingTime)).thenReturn(activitiesSummaryDto)

        val result = activitiesSummaryUseCase.getActivitiesSummary(start, end)

        assertEquals(activitiesSummaryDto, result)
    }
}