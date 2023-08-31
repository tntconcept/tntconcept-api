package com.autentia.tnt.binnacle.config.deleteTemporaryAttachments

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.usecases.TemporaryAttachmentsDeletionUseCase
import io.micronaut.context.annotation.Context
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.TaskScheduler
import jakarta.inject.Named

@Context
internal class DeleteTemporaryAttachmentsJob(
    private val appProperties: AppProperties,
    private val temporaryAttachmentsDeletionUseCase: TemporaryAttachmentsDeletionUseCase,
    @Named(TaskExecutors.SCHEDULED) taskScheduler: TaskScheduler,
) {

    init {
        if (appProperties.binnacle.temporaryAttachments.cronExpression != null) {
            taskScheduler.schedule(appProperties.binnacle.temporaryAttachments.cronExpression) {
                temporaryAttachmentsDeletionUseCase.delete()
            }
        }
    }
}