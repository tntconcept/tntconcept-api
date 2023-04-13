package com.autentia.tnt.binnacle.core.domain

import jakarta.inject.Singleton

@Singleton
internal class ActivitiesCalendarFactory(private val calendarFactory: CalendarFactory) {

    fun create(dateInterval: DateInterval) = ActivitiesCalendar(calendarFactory.create(dateInterval))
}