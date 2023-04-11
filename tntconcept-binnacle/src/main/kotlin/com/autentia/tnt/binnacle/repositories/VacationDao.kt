package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Vacation
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.time.LocalDate
import java.util.*

@Repository
internal interface VacationDao : CrudRepository<Vacation, Long> {
    fun findByIdAndUserId(vacationId: Long, userId: Long): Vacation?

    @Query("SELECT h FROM Vacation h WHERE h.userId= :userId AND (h.startDate >= :startDate AND h.endDate <= :endDate OR h.endDate >= :startDate AND h.startDate <= :endDate)")
    fun find(
        startDate: LocalDate,
        endDate: LocalDate,
        userId: Long
    ): List<Vacation>

    @Query("SELECT h FROM Vacation h WHERE h.userId= :userId AND h.chargeYear BETWEEN :startYear AND :endYear")
    fun findBetweenChargeYears(
        startYear: LocalDate,
        endYear: LocalDate,
        userId: Long
    ): List<Vacation>
}
