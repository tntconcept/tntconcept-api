package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.converters.ActivitySummaryConverter
import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.DailyWorkingTime
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActivitiesSummaryUseCaseTest {
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val activityService = mock<ActivityService>()
    private val activitiesSummaryConverter = ActivitySummaryConverter()
    private val securityService = mock<SecurityService>()

    private val activitiesSummaryUseCase = ActivitiesSummaryUseCase(
        activityCalendarService, activityService, activitiesSummaryConverter, securityService
    )

    @BeforeAll
    fun prepareUser() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
    }

    @Test
    fun `get activities summary should return activity summary dto`() {
        // Given
        val start = LocalDate.now()
        val end = LocalDate.now().plusDays(5)
        val dateInterval = DateInterval.of(
            start, end
        )

        val activitiesEntity = listOf(createActivity())
        val activitiesDomain: List<Activity> =
            activitiesEntity.map(com.autentia.tnt.binnacle.entities.Activity::toDomain)
        val dailyWorkingTime = listOf(DailyWorkingTime(start, BigDecimal(10)))
        val activitiesSummaryDto = activitiesSummaryConverter.toListActivitySummaryDTO(dailyWorkingTime)

        whenever(activityService.getActivitiesBetweenDates(dateInterval, userId)).thenReturn(activitiesEntity)
        whenever(activityCalendarService.getActivityDurationSummaryInHours(activitiesDomain, dateInterval)).thenReturn(
            dailyWorkingTime
        )

        // When
        val result = activitiesSummaryUseCase.getActivitiesSummary(start, end)

        // Then
        assertEquals(activitiesSummaryDto, result)
    }


    private companion object {
        private const val userId = 1L

        private val authenticatedUser = ClientAuthentication(
            userId.toString(), mapOf("roles" to listOf("user"))
        )
    }
}