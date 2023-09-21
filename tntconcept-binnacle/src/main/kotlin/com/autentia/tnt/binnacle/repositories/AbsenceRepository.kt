package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Absence
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Repository
internal interface AbsenceRepository: JpaRepository<Absence, Long>{
    @Query(
        """
            SELECT rh.id, rh.userId, u.name, 'VACATION', rh.beginDate, rh.finalDate
             FROM
                RequestHoliday rh
                INNER JOIN User u ON rh.userId = u.id
             WHERE 
                rh.beginDate BETWEEN :startDate AND :endDate
                AND (COALESCE(:userIds) IS NULL OR rh.userId IN (:userIds))
            UNION ALL
            SELECT  a.id, a.userId, u.name, 'PAID_LEAVE', DATE(a.start), DATE(a.end)
             FROM
                Activity a
                INNER JOIN User u ON a.userId = u.id
             WHERE 
                a.start BETWEEN :startDate AND :endDate
                AND (COALESCE(:userIds) IS NULL OR a.userId IN (:userIds))
               
       """
    , nativeQuery = true
    )
    fun findAllByDateBetween(startDate: LocalDate, endDate: LocalDate, userIds: Set<Long>?): List<Absence>

}