package com.autentia.tnt.api.binnacle.project

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class AutoBlockProjectUseCase(

) {
    @Scheduled(cron = "* * 19 * *")
    fun dailyCheck(){
        println("ey")
        val secondDay = false//isSecondWorkableDayOfMonth()
        if(secondDay) println("dia 2")
        else println("no")
    }
    /*
    fun isSecondWorkableDayOfMonth(): Boolean {
        val today = LocalDate.now()
        val firstDayOfMonth = today.withDayOfMonth(1)
        val workableDays = calendarWorkableDaysUseCase.get(firstDayOfMonth, today)

        // Verificar si hoy es el segundo día laborable del mes
        return workableDays >= 2 && today.dayOfMonth == 2
    }*/
}