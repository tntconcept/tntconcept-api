package com.autentia.tnt.binnacle.config.emptyActivitiesReminder

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.usecases.EmptyActivitiesReminderUseCase
import io.micronaut.context.annotation.Context
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.TaskScheduler
import jakarta.inject.Named

@Context
internal class EmptyActivitiesReminderJobConfig(
    private val appProperties: AppProperties,
    private val emptyActivitiesReminderUseCase: EmptyActivitiesReminderUseCase,
    @Named(TaskExecutors.SCHEDULED) taskScheduler: TaskScheduler
) {
    init {
        if (appProperties.binnacle.emptyActivitiesReminder.cronExpression != null) {
            taskScheduler.schedule(appProperties.binnacle.emptyActivitiesReminder.cronExpression) {
                emptyActivitiesReminderUseCase.sendReminders()
            }
        }
    }
}