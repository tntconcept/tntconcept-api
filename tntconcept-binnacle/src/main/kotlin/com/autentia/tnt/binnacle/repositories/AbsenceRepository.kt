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
            WITH ActivitiesView (id, userId, userName, type, startDate, endDate, numActivities) AS (
                SELECT
                    a.id,
                    a.userId, u.name,
                    CASE WHEN p.id IN (:paidLeaveProjectIds) THEN 'PAID_LEAVE' ELSE 'OTHER_ACTIVITY' END,
                    DATE(a.start), DATE(a.end),
                    COUNT(1) OVER (PARTITION BY a.userId, DATE(a.start))
                 FROM
                    Activity a
                    INNER JOIN User u ON a.userId = u.id
                    INNER JOIN ProjectRole pr ON pr.id = a.roleId
                    INNER JOIN Project p ON p.id = pr.projectId
                 WHERE
                    a.start BETWEEN :startDate AND :endDate
                    AND (COALESCE(:userIds) IS NULL OR a.userId IN (:userIds))
            ), VacationsView (id, userId, userName, type, startDate, endDate) AS (
                SELECT rh.id, rh.userId, u.name, 'VACATION', rh.beginDate, rh.finalDate
                 FROM
                    RequestHoliday rh
                    INNER JOIN User u ON rh.userId = u.id
                 WHERE
                    rh.beginDate BETWEEN :startDate AND :endDate
                    AND (COALESCE(:userIds) IS NULL OR rh.userId IN (:userIds))
            )
            SELECT id, userId, userName, type, startDate, endDate
              FROM VacationsView
            UNION ALL
            SELECT id, userId, userName, type, startDate, endDate
              FROM ActivitiesView
              WHERE type = 'PAID_LEAVE' AND numActivities = 1
       """
    , nativeQuery = true
    )
    fun findAllByDateBetween(startDate: LocalDate, endDate: LocalDate, paidLeaveProjectIds: Set<Long>, userIds: Set<Long>?): List<Absence>
}
