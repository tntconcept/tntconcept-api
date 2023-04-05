package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Vacation
import java.time.LocalDate

internal interface VacationRepository {
    fun findVacationsBetweenDate(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Vacation>

    fun filterBetweenChargeYears(
        startYear: LocalDate,
        endYear: LocalDate
    ): List<Vacation>

    fun findById(vacationId : Long) : Vacation?
    fun saveAll(vacations : Iterable<Vacation>) : Iterable<Vacation>
    fun update(vacation: Vacation) : Vacation
    fun deleteById(vacationId : Long)
}
