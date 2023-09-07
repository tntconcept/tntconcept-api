package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
internal class ActivityService(
    private val activityRepository: ActivityRepository,
) {

    @Transactional
    @ReadOnly
    fun getActivities(timeInterval: TimeInterval, userIds: List<Long>): List<Activity> =
        activityRepository.find(timeInterval.start, timeInterval.end, userIds)

    @Transactional
    @ReadOnly
    fun getActivitiesByProjectRoleIds(timeInterval: TimeInterval, projectRoleIds: List<Long>, userId: Long) =
        activityRepository.findByProjectRoleIds(timeInterval.start, timeInterval.end, projectRoleIds, userId)
            .map(Activity::toDomain)

    @Transactional
    fun findOverlappedActivities(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long) =
        activityRepository.findOverlapped(startDate, endDate, userId).map(Activity::toDomain)

}