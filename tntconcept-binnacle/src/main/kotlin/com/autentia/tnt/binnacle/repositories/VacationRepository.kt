package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Vacation
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.time.LocalDate

@Repository
internal interface VacationRepository : CrudRepository<Vacation, Long> {
    @Query("SELECT h FROM Vacation h WHERE h.userId= :userId AND (h.startDate >= :startDate AND h.endDate <= :endDate OR h.endDate >= :startDate AND h.startDate <= :endDate)")
    fun getVacationsBetweenDate(
        startDate: LocalDate,
        endDate: LocalDate,
        userId: Long
    ): List<Vacation>

    @Query("SELECT h FROM Vacation h WHERE h.userId= :userId AND h.chargeYear BETWEEN :startYear AND :endYear")
    fun filterBetweenChargeYears(
        startYear: LocalDate,
        endYear: LocalDate,
        userId: Long
    ): List<Vacation>


}
