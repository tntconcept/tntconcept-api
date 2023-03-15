package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.DateInterval
import jakarta.inject.Singleton

@Singleton
internal class ActivitiesCalendarFactory(private val calendarFactory: CalendarFactory) {

    fun create(dateInterval: DateInterval) = ActivitiesCalendar(calendarFactory.create(dateInterval))
}