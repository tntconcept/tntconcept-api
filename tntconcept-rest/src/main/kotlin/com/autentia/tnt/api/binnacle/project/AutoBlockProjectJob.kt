package com.autentia.tnt.api.binnacle.project

import com.autentia.tnt.binnacle.usecases.AutoBlockProjectUseCase
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton


@Singleton
class AutoBlockProjectJob(val autoBlockProjectUseCase: AutoBlockProjectUseCase) {
    @Scheduled(cron = "\${app.binnacle.work-block.project.daily.check.cron-expression}")
    fun periodBlock(){
        autoBlockProjectUseCase.dailyCheck()
    }
}