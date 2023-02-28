package com.autentia.tnt.binnacle.config

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.converters.TimeSummaryConverter
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.services.TargetWorkService
import com.autentia.tnt.binnacle.core.services.TimeWorkableService
import com.autentia.tnt.binnacle.core.services.WorkRecommendationCurrentMonthAccumulationService
import com.autentia.tnt.binnacle.core.services.WorkRecommendationService
import com.autentia.tnt.binnacle.core.services.TimeSummaryService
import com.autentia.tnt.binnacle.core.services.WorkedTimeService
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import com.autentia.tnt.binnacle.services.ProjectRoleService
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
internal class WorkTimeConfig {

    @Singleton
    fun targetWorkService(): TargetWorkService = TargetWorkService()

    @Singleton
    fun timeWorkableService(): TimeWorkableService = TimeWorkableService()

    @Singleton
    fun workedTimeService(workableProjectRoleIdChecker: WorkableProjectRoleIdChecker): WorkedTimeService =
        WorkedTimeService(workableProjectRoleIdChecker)

    @Singleton
    fun workableProjectRoleIdChecker(
        appProperties: AppProperties,
        projectRoleService: ProjectRoleService,
    ): WorkableProjectRoleIdChecker {
        val projectRoleIds =
            if (appProperties.binnacle.notWorkableProjects.isEmpty()) emptyList()
            else projectRoleService
                .getAllByProjectIds(appProperties.binnacle.notWorkableProjects)
                .map { ProjectRoleId(it.id) }
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
    ): TimeSummaryService =
        TimeSummaryService(
            targetWorkService,
            timeWorkableService,
            workedTimeService,
            workRecommendationService,
            timeSummaryConverter
        )

}
