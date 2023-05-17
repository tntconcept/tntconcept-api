package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import io.micronaut.data.jpa.repository.criteria.Specification
import java.time.LocalDateTime

internal interface ActivityRepository {

    fun findAll(activitySpecification: Specification<Activity>): List<Activity>

    fun findById(id: Long): Activity?

    fun findByUserId(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity>

    fun find(startDate: LocalDateTime, endDate: LocalDateTime, userIds: List<Long>): List<Activity>

    fun findByProjectRoleIds(
        start: LocalDateTime,
        end: LocalDateTime,
        projectRoleIds: List<Long>,
        userId: Long
    ): List<Activity>

    fun findOfLatestProjects(start: LocalDateTime, end: LocalDateTime, userId: Long): List<Activity>

    fun findByProjectId(start: LocalDateTime, end: LocalDateTime, projectId: Long, userId: Long): List<Activity>

    fun find(approvalState: ApprovalState): List<Activity>

    fun findByProjectRoleIdAndUserId(projectRoleId: Long, userId: Long): List<Activity>

    @Deprecated("Use findIntervals function instead")
    fun findWorkedMinutes(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<ActivityTimeOnly>

    fun findOverlapped(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity>

    fun save(activity: Activity): Activity

    fun update(activity: Activity): Activity

    fun deleteById(id: Long)
}