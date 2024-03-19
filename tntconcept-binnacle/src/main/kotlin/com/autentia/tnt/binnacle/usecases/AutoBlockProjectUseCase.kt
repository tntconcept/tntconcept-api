package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.repositories.ProjectRepository
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class AutoBlockProjectUseCase internal constructor(
    private val calendarWorkableDaysUseCase: CalendarWorkableDaysUseCase,
    internal val projectRepository: ProjectRepository
) {


    fun dailyCheck(){
        var projects = projectRepository.findAll()
        for(p in projects){
            println(p.id.toString() + "  " + p.blockDate +  "  " + p.open)
        }
        println("ey")
        val secondDay = isSecondWorkableDayOfMonth()
        if(secondDay) projectRepository.blockOpenProjects(LocalDate.now().withDayOfMonth(1).minusDays(1))
        projects = projectRepository.findAll()
        for(p in projects){
            println(p.id.toString() + "  " + p.blockDate +  "  " + p.open)
        }
    }

    fun isSecondWorkableDayOfMonth(): Boolean {
        //val today = LocalDate.now()
        val today = LocalDate.now().withDayOfMonth(4)
        val firstDayOfMonth = LocalDate.now().withDayOfMonth(1)
        val workableDays = calendarWorkableDaysUseCase.get(firstDayOfMonth, today)

        return workableDays == 2
    }
}