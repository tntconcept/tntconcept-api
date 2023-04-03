package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.annotation.EntityGraph
import io.micronaut.data.repository.CrudRepository
import java.time.LocalDateTime

@Repository
internal interface ActivityDao : CrudRepository<Activity, Long> {

    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun findByIdAndUserId(id: Long, userId: Long): Activity?

    @Query("SELECT a FROM Activity a WHERE a.userId= :userId AND a.startDate BETWEEN :startDate AND :endDate")
    @EntityGraph(value = "fetch-activity-with-project-and-organization")
    fun find(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity>

    @Query("SELECT new com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly(a.startDate, a.duration, a.projectRole.id) FROM Activity a WHERE a.userId= :userId AND a.startDate BETWEEN :startDate AND :endDate")
    fun findWorkedMinutes(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<ActivityTimeOnly>

}