package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.reset
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityMaxHoursPerYearValidatorTest {

    private val holidayRepository = mock<HolidayRepository>()
    private val activityRepository = mock<ActivityRepository>()
    private val projectRepository = mock<ProjectRepository>()

    private val calendarFactory: CalendarFactory = CalendarFactory(holidayRepository)
    private val activitiesCalendarFactory: ActivitiesCalendarFactory = ActivitiesCalendarFactory(calendarFactory)
    private val activityCalendarService = ActivityCalendarService(calendarFactory, activitiesCalendarFactory)

    private val activityService = ActivityService(activityRepository)
    private val activityValidator =
        ActivityValidator(
            activityService,
            activityCalendarService,
            projectRepository
        )

    @AfterEach
    fun resetMocks() {
        reset(
            holidayRepository,
            activityRepository,
            projectRepository
        )
    }

    private fun testInputs() = arrayOf (
//        arrayOf(2, 3, TimeUnit.NATURAL_DAYS, true),
        arrayOf(0, 1, TimeUnit.DAYS, true),
        arrayOf(0, 1, TimeUnit.DAYS, false),
        arrayOf(0, 1, TimeUnit.NATURAL_DAYS, true),
        arrayOf(0, 1, TimeUnit.NATURAL_DAYS, false),
        arrayOf(0, 2, TimeUnit.MINUTES, true),
        arrayOf(0, 2, TimeUnit.DAYS, true),
        arrayOf(0, 2, TimeUnit.DAYS, false),
        arrayOf(0, 2, TimeUnit.NATURAL_DAYS, true),
        arrayOf(0, 2, TimeUnit.NATURAL_DAYS, false),
        arrayOf(0, 3, TimeUnit.MINUTES, true),
        arrayOf(0, 3, TimeUnit.DAYS, true),
        arrayOf(0, 3, TimeUnit.DAYS, false),
        arrayOf(0, 3, TimeUnit.NATURAL_DAYS, true),
        arrayOf(0, 3, TimeUnit.NATURAL_DAYS, false),
        arrayOf(0, 4, TimeUnit.MINUTES, true),
        arrayOf(0, 4, TimeUnit.DAYS, true),
        arrayOf(0, 4, TimeUnit.DAYS, false),
        arrayOf(0, 4, TimeUnit.NATURAL_DAYS, true),
        arrayOf(0, 4, TimeUnit.NATURAL_DAYS, false),
        arrayOf(1, 1, TimeUnit.MINUTES, true),
        arrayOf(1, 1, TimeUnit.DAYS, true),
        arrayOf(1, 1, TimeUnit.DAYS, false),
        arrayOf(1, 1, TimeUnit.NATURAL_DAYS, true),
        arrayOf(1, 1, TimeUnit.NATURAL_DAYS, false),
        arrayOf(1, 2, TimeUnit.MINUTES, true),
        arrayOf(1, 2, TimeUnit.DAYS, true),
        arrayOf(1, 2, TimeUnit.DAYS, false),
        arrayOf(1, 2, TimeUnit.NATURAL_DAYS, true),
        arrayOf(1, 2, TimeUnit.NATURAL_DAYS, false),
        arrayOf(1, 3, TimeUnit.MINUTES, true),
        arrayOf(1, 3, TimeUnit.DAYS, true),
        arrayOf(1, 3, TimeUnit.DAYS, false),
        arrayOf(1, 3, TimeUnit.NATURAL_DAYS, true),
        arrayOf(2, 1, TimeUnit.MINUTES, true),
        arrayOf(2, 1, TimeUnit.DAYS, true),
        arrayOf(2, 1, TimeUnit.DAYS, false),
        arrayOf(2, 1, TimeUnit.NATURAL_DAYS, true),
        arrayOf(2, 1, TimeUnit.NATURAL_DAYS, false),
        arrayOf(2, 2, TimeUnit.MINUTES, true),
        arrayOf(2, 2, TimeUnit.DAYS, true),
        arrayOf(2, 2, TimeUnit.DAYS, false),
        arrayOf(2, 2, TimeUnit.NATURAL_DAYS, true),
        arrayOf(2, 2, TimeUnit.NATURAL_DAYS, false),
        arrayOf(3, 1, TimeUnit.MINUTES, true),
        arrayOf(3, 1, TimeUnit.DAYS, true),
        arrayOf(3, 1, TimeUnit.NATURAL_DAYS, true)
    )

    @ParameterizedTest
    @MethodSource("testInputs")
    fun `do nothing when activity is valid for creation in days and natural days`(
        previousTimeRegisteredForProjectRole: Int,
        newActivityDuration: Long,
        timeUnit: TimeUnit,
        activityEndsInCurrentYear: Boolean,
    ) {


        val activityCanBeConfiguredWithAChangeOfYear =
            !activityEndsInCurrentYear && previousTimeRegisteredForProjectRole <= 2 && newActivityDuration > 1 && timeUnit != TimeUnit.MINUTES

        println("Test Params:: TimeUsedForCurrentActivities: $previousTimeRegisteredForProjectRole; TimeForNewActivity: $newActivityDuration; TimeUnit: $timeUnit; isThisYear: $activityEndsInCurrentYear");

        var newActivity = createNewActivity (timeUnit, newActivityDuration)

        if (activityCanBeConfiguredWithAChangeOfYear)
            newActivity = createNewActivityWithChangeOfYear(timeUnit, newActivityDuration)

        if (activityCanBeConfiguredWithAChangeOfYear || activityEndsInCurrentYear) {
            val activities2023 = ArrayList<Activity>()

            if (previousTimeRegisteredForProjectRole > 0)
                for (index in 1..previousTimeRegisteredForProjectRole)
                    activities2023.add(
                        createExistingActivity(previousTimeRegisteredForProjectRole, timeUnit, index)
                    )

            val timeInterval2023 = TimeInterval.ofYear(2023)

            doReturn(Optional.of(project))
                .whenever(projectRepository)
                .findById(projectRoleLimitedByYear.project.id)

            doReturn(activities2023)
                .whenever(activityRepository)
                .findByProjectRoleIds(
                    timeInterval2023.start,
                    timeInterval2023.end,
                    listOf(projectRoleLimitedByYear.id),
                    user.id
                )

            activityValidator.checkActivityIsValidForCreation(newActivity, user)
        }
    }

    private fun createExistingActivity(
        timeUsedForCurrentActivities: Int,
        timeUnit: TimeUnit,
        index: Int
    ) = customActivity.copy(
        id = timeUsedForCurrentActivities.toLong(),
        start = if (timeUnit != TimeUnit.MINUTES) customActivity.start.plusDays(index.toLong() - 1)
        else customActivity.start.plusHours(index.toLong()),
        end = if (timeUnit != TimeUnit.MINUTES) customActivity.end.plusDays(index.toLong() - 1)
        else customActivity.start.plusHours(index.toLong()).plusMinutes(60),
        duration = if (timeUnit != TimeUnit.MINUTES) 60 * 8
        else 60,
        projectRole = if (timeUnit == TimeUnit.MINUTES) projectRoleLimitedByYearMinutesVersion else projectRoleLimitedByYear.copy(
            timeUnit = timeUnit
        )
    )

    private fun createNewActivityWithChangeOfYear(
        timeUnit: TimeUnit,
        timeForNewActivity: Long
    ) = customActivity.copy(
        id = null,
        start = LocalDate.of(
            2023,
            12,
            if (TimeUnit.DAYS == timeUnit) 29 else 31  // 29 is Friday
        ).atTime(0, 0, 0),
        end = LocalDate.of(
            2024,
            1,
            timeForNewActivity.toInt() - 1
        ).atTime(23, 59, 59),
        duration = timeForNewActivity.toInt() * 60 * 8,
        projectRole = projectRoleLimitedByYear.copy(timeUnit = timeUnit)
    ).toDomain()

    private fun createNewActivity(
        timeUnit: TimeUnit,
        timeForNewActivity: Long
    ) = customActivity.copy(
        id = null,
        start = customActivity.start.plusDays(7), // Next monday
        end =
        if (timeUnit != TimeUnit.MINUTES) customActivity.start.plusDays(7).plusDays(timeForNewActivity - 1)
        else customActivity.start.plusDays(7).plusMinutes(timeForNewActivity * 60),
        duration = timeForNewActivity.toInt() * 60 * 8,
        projectRole = if (timeUnit == TimeUnit.MINUTES) projectRoleLimitedByYearMinutesVersion else projectRoleLimitedByYear.copy(
            timeUnit = timeUnit
        )
    ).toDomain()

    private companion object {


        private val user = createDomainUser()

        private val project = Project(
            1,
            "project",
            true,
            true,
            LocalDate.now().minusYears(1),
            null,
            null,
            Organization(1, "Organization", emptyList()),
            emptyList()
        )

        private val projectRoleLimitedByYear = ProjectRole(
            1,
            "project role",
            RequireEvidence.NO,
            project,
            1920, // limited to 4 days
            0,
            true,
            false,
            TimeUnit.DAYS
        )

        private val projectRoleLimitedByYearMinutesVersion = ProjectRole(
            1,
            "project role",
            RequireEvidence.NO,
            project,
            240, // limited to 4h
            0,
            true,
            false,
            TimeUnit.MINUTES
        )

        private val customActivity = Activity(
            1L,
            LocalDateTime.of(2023, 3, 6, 0,0, 0),
            LocalDateTime.of(2023, 3, 6, 23, 59, 59),
            60,
            "Activity",
            projectRoleLimitedByYear,
            1L,
            true,
            1L,
            null,
            false,
            ApprovalState.NA
        )
    }
}