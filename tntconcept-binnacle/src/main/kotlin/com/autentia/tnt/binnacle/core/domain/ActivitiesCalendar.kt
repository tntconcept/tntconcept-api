package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.DateInterval

class ActivitiesCalendar(val calendar: Calendar) {

    val activitiesCalendarMap = calendar.allDays.associateWith { mutableListOf<Activity>() }

    fun addActivity(activity: Activity) {
        val dateInterval = activity.getDateInterval()
        if (activity.isOneDay()) {
            activitiesCalendarMap[dateInterval.start]?.add(activity)
        } else {
            getWorkableDays(dateInterval).forEach { activitiesCalendarMap[it]?.add(activity) }
        }
    }

    fun addAllActivities(activities: List<Activity>) {
        activities.forEach(this::addActivity)
    }

    private fun getWorkableDays(dateInterval: DateInterval) =
        calendar.workableDays.filter { dateInterval.includes(it) }.toList()
}