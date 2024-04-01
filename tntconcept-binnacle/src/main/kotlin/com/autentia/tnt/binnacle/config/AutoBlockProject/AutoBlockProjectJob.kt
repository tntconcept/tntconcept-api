package com.autentia.tnt.binnacle.config.AutoBlockProject

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.usecases.AutoBlockProjectUseCase
import io.archimedesfw.commons.time.EUROPE_MADRID
import io.micronaut.context.annotation.Context
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.TaskScheduler
import jakarta.inject.Named
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Context
internal class AutoBlockProjectJob(
    val autoBlockProjectUseCase: AutoBlockProjectUseCase,
    private val appProperties: AppProperties,
    @Named(TaskExecutors.SCHEDULED) taskScheduler: TaskScheduler
) {
    init {

        if (appProperties.binnacle.autoBlockProject.cronExpression != null) {
            taskScheduler.schedule(appProperties.binnacle.autoBlockProject.cronExpression) {
                autoBlockProjectUseCase.blockOpenProjectsOnSecondDayOfMonth()
            }

        }
    }

}