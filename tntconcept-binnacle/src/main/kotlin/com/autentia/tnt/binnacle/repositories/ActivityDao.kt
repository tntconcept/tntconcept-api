package com.autentia.tnt.binnacle.repositories

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

    @Query("SELECT a FROM Activity a WHERE a.userId IN :userIds AND a.start <= :endDate AND a.end >= :startDate")
    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun find(startDate: LocalDateTime, endDate: LocalDateTime, userIds: List<Long>): List<Activity>

    @Query(
        """SELECT a 
            FROM Activity a
            JOIN FETCH a.projectRole pr
            JOIN FETCH pr.project p
            JOIN FETCH p.organization o
            WHERE a.userId = :userId 
            AND a.start <= :endDate
            AND a.end >= :startDate 
            AND p.open = true
            ORDER BY a.start DESC"""
    )
    fun findOfLatestProjects(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity>

    @Query(
        """SELECT a 
            FROM Activity a
            JOIN a.projectRole.project p
            WHERE a.userId = :userId
            AND p.id = :projectId
            AND a.start <= :end 
            AND a.end >= :start"""
    )
    fun findByProjectId(
        start: LocalDateTime,
        end: LocalDateTime,
        projectId: Long,
        userId: Long
    ): List<Activity>

    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun findByApprovalStateAndUserId(approvalState: ApprovalState, userId: Long): List<Activity>

    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun findByApprovalState(approvalState: ApprovalState): List<Activity>

    @Deprecated("Used in the deprecated Activities Controller")
    @Query("SELECT new com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly(a.start, a.duration, a.projectRole.id) FROM Activity a WHERE a.userId= :userId AND a.start BETWEEN :startDate AND :endDate")
    fun findWorkedMinutes(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<ActivityTimeOnly>

    @Query("SELECT a FROM Activity a WHERE a.userId= :userId AND a.start < :endDate AND a.end > :startDate ")
    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun findOverlapped(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity>

    @Query(
        "SELECT a " +
                "FROM Activity a " +
                "WHERE a.userId= :userId " +
                "AND a.projectRole.id = :projectRoleId " +
                "AND a.start <= :endDate AND a.end >= :startDate " +
                "ORDER BY a.start "
    )
    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun find(
        startDate: LocalDateTime, endDate: LocalDateTime, projectRoleId: Long, userId: Long
    ): List<Activity>

    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun findByProjectRoleIdAndUserId(projectRoleId: Long, userId: Long): List<Activity>


}