package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.core.utils.myDatesUntil
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.DateInterval
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.LocalDate
import java.time.LocalDateTime

class ActivitiesCalendarTest {

    private val holidayService: HolidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val date = LocalDate.of(2023, 3, 1)
    private val datePlusWeek = date.plusWeeks(1)

    private val dateTime = LocalDateTime.of(2023, 3, 1, 13, 5, 25)
    private val dateTimePlusOneHour = dateTime.plusHours(1L)
    private val dateTimePlusTwoDays = dateTimePlusOneHour.plusDays(2)

    private val projectRoleInMinutes = createProjectRole()
    private val projectRoleInDays = projectRoleInMinutes.copy(timeUnit = TimeUnit.DAYS)
    private val activityInMinutes = Activity(
        1, dateTime, dateTimePlusOneHour, 60, "Activity",
        projectRoleInMinutes, 1L, true, 1L, null, false, ApprovalState.NA
    )
    private val activityInDays =
        activityInMinutes.copy(
            start = dateTime, end = dateTimePlusTwoDays, duration = 960, projectRole = projectRoleInDays
        )
    private val activityOutOfInterval =
        activityInMinutes.copy(
            start = dateTime.plusMonths(1),
            end = dateTimePlusTwoDays.plusMonths(1),
            duration = 960,
            projectRole = projectRoleInDays
        )
    private val activities = listOf(activityInMinutes, activityInDays)

    @Test
    fun `if no activities were added, there should be no activities on the activitiesCalendarMap`() {

        val calendar = calendarFactory.create(DateInterval.of(date, datePlusWeek))
        val activitiesCalendar = ActivitiesCalendar(calendar)

        assert(activitiesCalendar.activitiesCalendarMap.values.all { it.isEmpty() })
    }

    @Test
    fun `if the activity is out of the calendar interval, it is not added`() {

        val calendar = calendarFactory.create(DateInterval.of(date, datePlusWeek))
        val activitiesCalendar = ActivitiesCalendar(calendar)
        activitiesCalendar.addActivity(activityOutOfInterval)
        assert(activitiesCalendar.activitiesCalendarMap.values.all { it.isEmpty() })
    }

    @Test
    fun `activities are assigned correctly`() {

        val calendar = calendarFactory.create(DateInterval.of(date, datePlusWeek))
        val activitiesCalendar = ActivitiesCalendar(calendar)
        activitiesCalendar.addAllActivities(activities)

        activities.forEach { activity ->
            val dateInterval = activity.getDateInterval()
            dateInterval.start.myDatesUntil(dateInterval.end).forEach {
                assertTrue(activitiesCalendar.activitiesCalendarMap[it]!!.contains(activity))
            }
        }
    }
}