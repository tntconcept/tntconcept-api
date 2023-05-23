package com.autentia.tnt.binnacle.config

import com.autentia.tnt.binnacle.converters.TimeSummaryConverter
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.services.*
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ProjectRoleService
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
internal class WorkTimeFactory {

    @Singleton
    fun targetWorkService(): TargetWorkService = TargetWorkService()

    @Singleton
    fun timeWorkableService(): TimeWorkableService = TimeWorkableService()

    @Singleton
    fun workedTimeService(
        activityCalendarService: ActivityCalendarService, workableProjectRoleIdChecker: WorkableProjectRoleIdChecker
    ): WorkedTimeService = WorkedTimeService(activityCalendarService, workableProjectRoleIdChecker)

    @Singleton
    fun workableProjectRoleIdChecker(projectRoleService: ProjectRoleService): WorkableProjectRoleIdChecker {
        val projectRoleIds = projectRoleService.getAllNotWorkable().map { ProjectRoleId(it.id) }
        return WorkableProjectRoleIdChecker(projectRoleIds)
    }

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
