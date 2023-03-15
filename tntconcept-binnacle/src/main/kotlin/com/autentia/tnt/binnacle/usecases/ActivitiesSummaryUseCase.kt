package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.DateInterval
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class ActivitiesSummaryUseCase internal constructor(private val activityCalendarService: ActivityCalendarService) {

    fun getActivitiesSummary() {
        activityCalendarService.getActivityDurationSummaryInHours(
            DateInterval.of(
                LocalDate.of(2023, 3, 1),
                LocalDate.of(2023, 3, 31)
            ), 2L
        )
    }
}