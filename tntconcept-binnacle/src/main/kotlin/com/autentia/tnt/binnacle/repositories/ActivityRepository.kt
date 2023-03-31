package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import java.time.LocalDateTime

internal interface ActivityRepository {

    fun findById(id: Long): Activity?

    fun find(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity>

    fun findWorkedMinutes(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<ActivityTimeOnly>

    fun find(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity>

    fun findWorkedMinutes(startDate: LocalDateTime, endDate: LocalDateTime): List<ActivityTimeOnly>

    fun save(activity: Activity): Activity

    fun update(activity: Activity): Activity

    fun deleteById(id: Long)
}