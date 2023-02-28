package com.autentia.tnt.binnacle.config.worksummary

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.usecases.AnnualWorkSummaryJobUseCase
import io.micronaut.context.annotation.Context
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.TaskScheduler
import jakarta.inject.Named

@Context
internal class WorkSummaryJobConfig(
    private val annualWorkSummaryJobUseCase: AnnualWorkSummaryJobUseCase,
    private val appProperties: AppProperties,
    @Named(TaskExecutors.SCHEDULED) taskScheduler: TaskScheduler
) {

    init {
        if (appProperties.binnacle.workSummary.cronExpression != null) {
            taskScheduler.schedule(appProperties.binnacle.workSummary.cronExpression) {
                annualWorkSummaryJobUseCase.createWorkSummariesYearBefore()
            }
        }
    }

}