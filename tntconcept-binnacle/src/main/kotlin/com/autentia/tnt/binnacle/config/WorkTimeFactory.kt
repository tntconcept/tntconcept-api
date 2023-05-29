package com.autentia.tnt.binnacle.config

import com.autentia.tnt.binnacle.converters.TimeSummaryConverter
import com.autentia.tnt.binnacle.core.services.*
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
internal class WorkTimeFactory {

    @Singleton
    fun targetWorkService(): TargetWorkService = TargetWorkService()

    @Singleton
    fun timeWorkableService(): TimeWorkableService = TimeWorkableService()

    @Singleton
    fun workedTimeService(activityCalendarService: ActivityCalendarService): WorkedTimeService =
        WorkedTimeService(activityCalendarService)

    @Singleton
    fun workRecommendationService(): WorkRecommendationService = WorkRecommendationCurrentMonthAccumulationService()

    @Singleton
    fun workBalanceService(
        targetWorkService: TargetWorkService,
        timeWorkableService: TimeWorkableService,
        workedTimeService: WorkedTimeService,
        workRecommendationService: WorkRecommendationService,
        timeSummaryConverter: TimeSummaryConverter,
    ): TimeSummaryService = TimeSummaryService(
        targetWorkService, timeWorkableService, workedTimeService, workRecommendationService, timeSummaryConverter
    )

}
