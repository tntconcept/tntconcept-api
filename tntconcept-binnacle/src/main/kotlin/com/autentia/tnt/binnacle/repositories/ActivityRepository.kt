package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityInterval
import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import java.time.LocalDateTime

internal interface ActivityRepository {

    fun findById(id: Long): Activity?

    fun find(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity>

    fun find(startDate: LocalDateTime, endDate: LocalDateTime, userIds: List<Long>): List<Activity>

    fun findWithoutSecurity(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity>

    fun find(start: LocalDateTime, end: LocalDateTime, projectRoleId: Long): List<Activity>

    fun findOfLatestProjects(start: LocalDateTime, end: LocalDateTime): List<Activity>

    fun find(approvalState: ApprovalState): List<Activity>

    fun find(projectRoleId: Long): List<Activity>

    @Deprecated("Use findIntervals function instead")
    fun findWorkedMinutes(startDate: LocalDateTime, endDate: LocalDateTime): List<ActivityTimeOnly>

    fun findOverlapped(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity>

    fun save(activity: Activity): Activity

    fun update(activity: Activity): Activity

    fun deleteById(id: Long)
}