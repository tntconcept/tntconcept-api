package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityInterval
import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.annotation.EntityGraph
import io.micronaut.data.repository.CrudRepository
import java.time.LocalDateTime

@Repository
internal interface ActivityDao : CrudRepository<Activity, Long> {

    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun findByIdAndUserId(id: Long, userId: Long): Activity?

    @Query("SELECT a FROM Activity a WHERE a.userId= :userId AND a.start <= :endDate AND a.end >= :startDate")
    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun find(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity>

    @Query("SELECT a FROM Activity a WHERE a.userId= :userId AND a.approvalState= :approvalState")
    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun find(approvalState: ApprovalState, userId: Long): List<Activity>

    @Deprecated("Used in the deprecated Activities Controller")
    @Query("SELECT new com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly(a.start, a.duration, a.projectRole.id) FROM Activity a WHERE a.userId= :userId AND a.start BETWEEN :startDate AND :endDate")
    fun findWorkedMinutes(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<ActivityTimeOnly>

    @Query("SELECT a FROM Activity a WHERE a.userId= :userId AND a.start < :end AND a.end > :start ")
    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun findOverlapped(start: LocalDateTime, end: LocalDateTime, userId: Long): List<Activity>

    @Query(
        "SELECT a.start AS start, a.end AS end, pr.timeUnit as timeUnit " +
                "FROM Activity a " +
                "JOIN a.projectRole pr " +
                "WHERE a.userId= :userId " +
                "AND pr.id = :projectRoleId " +
                "AND a.start <= :end AND a.end >= :start " +
                "ORDER BY a.start "
    )
    fun findIntervals(
        start: LocalDateTime, end: LocalDateTime, projectRoleId: Long, userId: Long
    ): List<ActivityInterval>

    //@Query("SELECT a FROM Activity a WHERE a.userId= :userId AND a.projectRole.id = :projectRoleId")
    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun findByProjectRoleIdAndUserId(projectRoleId: Long, userId: Long): List<Activity>

}