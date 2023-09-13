package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Vacation
import java.time.LocalDate

internal interface VacationRepository {
    fun find(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Vacation>

    fun findWithoutSecurity(
        startDate: LocalDate,
        endDate: LocalDate,
        userId: Long
    ): List<Vacation>

    fun findBetweenChargeYears(
        startYear: LocalDate,
        endYear: LocalDate
    ): List<Vacation>

    fun findByChargeYear(
        chargeYear: LocalDate
    ): List<Vacation>

    fun findBetweenChargeYearsWithoutSecurity(
        startYear: LocalDate,
        endYear: LocalDate,
        userId: Long
    ): List<Vacation>


    fun findById(vacationId: Long): Vacation?
    fun saveAll(vacations: Iterable<Vacation>): Iterable<Vacation>
    fun update(vacation: Vacation): Vacation
    fun deleteById(vacationId: Long)
}
