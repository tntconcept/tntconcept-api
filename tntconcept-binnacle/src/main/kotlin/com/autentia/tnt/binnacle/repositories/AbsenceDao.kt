package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Absence
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Repository
internal interface AbsenceDao : JpaRepository<Absence, Long> {
    @Query(
        """WITH ActivitiesView (id, userId, type, startDate, endDate, numActivities) AS (
                SELECT
                    a.id,
                    a.userId,
                    CASE WHEN pr.isApprovalRequired = true THEN 'PAID_LEAVE' ELSE 'OTHER_ACTIVITY' END,
                    DATE(a.start), DATE(a.end),
                    COUNT(1) OVER (PARTITION BY a.userId, DATE(a.start))
                 FROM
                    Activity a
                    INNER JOIN ProjectRole pr ON pr.id = a.roleId
                    INNER JOIN Project p ON p.id = pr.projectId
                 WHERE
                    a.start <= :endDate AND a.end >= :startDate
                    AND (COALESCE(:userIds) IS NULL OR a.userId IN (:userIds))
            ), VacationsView (id, userId, type, startDate, endDate) AS (
                SELECT rh.id, rh.userId, 'VACATION', rh.beginDate, rh.finalDate
                 FROM
                    RequestHoliday rh
                 WHERE
                    rh.beginDate <= :endDate AND rh.finalDate >= :startDate
                    AND (COALESCE(:userIds) IS NULL OR rh.userId IN (:userIds))
            )
            SELECT id, userId, type, startDate, endDate
              FROM VacationsView
            UNION ALL
            SELECT id, userId, type, startDate, endDate
              FROM ActivitiesView
              WHERE type = 'PAID_LEAVE' AND numActivities = 1
              ORDER BY startDate""",
        nativeQuery = true
    )
    fun findAllByDateBetweenAndUsers(
        startDate: LocalDate,
        endDate: LocalDate,
        userIds: List<Long>?
    ): List<Absence>
    @Query(
        """WITH ActivitiesView (id, userId, type, startDate, endDate, numActivities) AS (
                SELECT
                    a.id,
                    a.userId,
                    CASE WHEN pr.isApprovalRequired = true THEN 'PAID_LEAVE' ELSE 'OTHER_ACTIVITY' END,
                    DATE(a.start), DATE(a.end),
                    COUNT(1) OVER (PARTITION BY a.userId, DATE(a.start))
                 FROM
                    Activity a
                    INNER JOIN ProjectRole pr ON pr.id = a.roleId
                    INNER JOIN Project p ON p.id = pr.projectId
                 WHERE
                    a.start <= :endDate AND a.end >= :startDate
                    AND a.userId = :userId
            ), VacationsView (id, userId, type, startDate, endDate) AS (
                SELECT rh.id, rh.userId, 'VACATION', rh.beginDate, rh.finalDate
                 FROM
                    RequestHoliday rh
                 WHERE
                    rh.beginDate <= :endDate AND rh.finalDate >= :startDate
                    AND rh.userId = :userId
            )
            SELECT id, userId, type, startDate, endDate
              FROM VacationsView
            UNION ALL
            SELECT id, userId, type, startDate, endDate
              FROM ActivitiesView
              WHERE type = 'PAID_LEAVE' AND numActivities = 1
              ORDER BY startDate""",
        nativeQuery = true
    )
    fun findAllByDateBetweenAndUser(
        startDate: LocalDate,
        endDate: LocalDate,
        userId: Long?
    ): List<Absence>
}
