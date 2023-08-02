package com.autentia.tnt.binnacle.config.missingevidencesnotification

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.usecases.ActivityEvidenceMissingReminderUseCase
import com.autentia.tnt.binnacle.usecases.NotificationType
import io.micronaut.context.annotation.Context
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.TaskScheduler
import jakarta.inject.Named


@Context
internal class MissingWeeklyEvidencesNotificationJobConfig(
    private val appProperties: AppProperties,
    private val activityEvidenceMissingReminderUseCase: ActivityEvidenceMissingReminderUseCase,
    @Named(TaskExecutors.SCHEDULED) taskScheduler: TaskScheduler
) {
    init {
        if (appProperties.binnacle.missingEvidencesNotification.weekly.cronExpression != null) {
            taskScheduler.schedule(appProperties.binnacle.missingEvidencesNotification.weekly.cronExpression) {
                activityEvidenceMissingReminderUseCase.sendReminders(NotificationType.WEEKLY)
            }
        }
    }
}