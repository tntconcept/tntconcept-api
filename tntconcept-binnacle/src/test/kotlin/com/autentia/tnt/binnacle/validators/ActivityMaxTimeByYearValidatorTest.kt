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
class ActivityMaxTimeByYearValidatorTest {

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

    private fun testInputsInDays() = arrayOf(
        arrayOf(0, 1, true),
        arrayOf(0, 1, false),
        arrayOf(0, 2, true),
        arrayOf(0, 2, false),
        arrayOf(0, 3, true),
        arrayOf(0, 3, false),
        arrayOf(0, 4, true),
        arrayOf(0, 4, false),
        arrayOf(1, 1, true),
        arrayOf(1, 1, false),
        arrayOf(1, 2, true),
        arrayOf(1, 2, false),
        arrayOf(1, 3, true),
        arrayOf(1, 3, false),
        arrayOf(2, 1, true),
        arrayOf(2, 1, false),
        arrayOf(2, 2, true),
        arrayOf(2, 2, false),
        arrayOf(3, 1, true),

        )

    private fun testInputsInNaturalDays() = arrayOf(
        arrayOf(0, 1, true),
        arrayOf(0, 1, false),
        arrayOf(0, 2, true),
        arrayOf(0, 2, false),
        arrayOf(0, 3, true),
        arrayOf(0, 3, false),
        arrayOf(0, 4, true),
        arrayOf(0, 4, false),
        arrayOf(1, 1, true),
        arrayOf(1, 1, false),
        arrayOf(1, 2, true),
        arrayOf(1, 2, false),
        arrayOf(1, 3, true),
        arrayOf(2, 1, true),
        arrayOf(2, 1, false),
        arrayOf(2, 2, true),
        arrayOf(2, 2, false),
        arrayOf(3, 1, true)
    )

    private fun testInputsInMinutes() = arrayOf(
        arrayOf(0, 1),
        arrayOf(0, 2),
        arrayOf(0, 3),
        arrayOf(0, 4),
        arrayOf(1, 1),
        arrayOf(1, 2),
        arrayOf(1, 3),
        arrayOf(2, 1),
        arrayOf(2, 2),
        arrayOf(3, 1)
    )

    @ParameterizedTest
    @MethodSource("testInputsInDays")
    fun `do nothing when activity is valid for creation in days`(
        previousTimeRegisteredForProjectRole: Int,
        newActivityDuration: Long,
        activityEndsInCurrentYear: Boolean,
    ) {

        val activityCanBeConfiguredWithAChangeOfYear =
            !activityEndsInCurrentYear && previousTimeRegisteredForProjectRole <= 2 && newActivityDuration > 1

        println("Test Params:: TimeUsedForCurrentActivities: $previousTimeRegisteredForProjectRole; TimeForNewActivity: $newActivityDuration; isThisYear: $activityEndsInCurrentYear");

        var newActivity = createNewActivity(newActivityDuration, TimeUnit.DAYS)

        if (activityCanBeConfiguredWithAChangeOfYear)
            newActivity = createNewActivityWithChangeOfYear(newActivityDuration, TimeUnit.DAYS)

        if (activityCanBeConfiguredWithAChangeOfYear || activityEndsInCurrentYear) {
            val activities2023 = ArrayList<Activity>()

            if (previousTimeRegisteredForProjectRole > 0)
                for (index in 1..previousTimeRegisteredForProjectRole)
                    activities2023.add(
                        createExistingActivity(previousTimeRegisteredForProjectRole, TimeUnit.DAYS, index)
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

    @ParameterizedTest
    @MethodSource("testInputsInNaturalDays")
    fun `do nothing when activity is valid for creation in natural days`(
        previousTimeRegisteredForProjectRole: Int,
        newActivityDuration: Long,
        activityEndsInCurrentYear: Boolean,
    ) {

        val activityCanBeConfiguredWithAChangeOfYear =
            !activityEndsInCurrentYear && previousTimeRegisteredForProjectRole <= 2 && newActivityDuration > 1

        println("Test Params:: TimeUsedForCurrentActivities: $previousTimeRegisteredForProjectRole; TimeForNewActivity: $newActivityDuration; isThisYear: $activityEndsInCurrentYear");

        var newActivity = createNewActivity(newActivityDuration, TimeUnit.NATURAL_DAYS)

        if (activityCanBeConfiguredWithAChangeOfYear)
            newActivity = createNewActivityWithChangeOfYear(newActivityDuration, TimeUnit.NATURAL_DAYS)

        if (activityCanBeConfiguredWithAChangeOfYear || activityEndsInCurrentYear) {
            val activities2023 = ArrayList<Activity>()

            if (previousTimeRegisteredForProjectRole > 0)
                for (index in 1..previousTimeRegisteredForProjectRole)
                    activities2023.add(
                        createExistingActivity(previousTimeRegisteredForProjectRole, TimeUnit.NATURAL_DAYS, index)
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

    @ParameterizedTest
    @MethodSource("testInputsInMinutes")
    fun `do nothing when activity is valid for creation in minutes`(
        previousTimeRegisteredForProjectRole: Int,
        newActivityDuration: Long,
    ) {
        //println("Test Params:: TimeUsedForCurrentActivities: $previousTimeRegisteredForProjectRole; TimeForNewActivity: $newActivityDuration; TimeUnit: $timeUnit; isThisYear: $activityEndsInCurrentYear");

        val newActivity = createNewActivityInMinutes(newActivityDuration)

        val activities2023 = ArrayList<Activity>()

        if (previousTimeRegisteredForProjectRole > 0)
            for (index in 1..previousTimeRegisteredForProjectRole)
                activities2023.add(
                    createExistingActivityInMinutes(previousTimeRegisteredForProjectRole, index)
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

    private fun createExistingActivity(
        timeUsedForCurrentActivities: Int,
        timeUnit: TimeUnit,
        index: Int,
    ) = customActivity.copy(
        id = timeUsedForCurrentActivities.toLong(),
        start = customActivity.start.plusDays(index.toLong() - 1),
        end = customActivity.end.plusDays(index.toLong() - 1),
        duration = 60 * 8,
        projectRole = projectRoleLimitedByYear.copy(
            timeUnit = timeUnit
        )
    )

    private fun createExistingActivityInMinutes(
        timeUsedForCurrentActivities: Int,
        index: Int,
    ) = customActivity.copy(
        id = timeUsedForCurrentActivities.toLong(),
        start = customActivity.start.plusHours(index.toLong()),
        end = customActivity.start.plusHours(index.toLong()).plusMinutes(60),
        duration = 60,
        projectRole = projectRoleLimitedByYearMinutesVersion
    )

    private fun createNewActivityWithChangeOfYear(
        timeForNewActivity: Long,
        timeUnit: TimeUnit,
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
        timeForNewActivity: Long,
        timeUnit: TimeUnit,
    ) = customActivity.copy(
        id = null,
        start = customActivity.start.plusDays(7), // Next monday
        end = customActivity.start.plusDays(7).plusDays(timeForNewActivity - 1),
        duration = timeForNewActivity.toInt() * 60 * 8,
        projectRole = projectRoleLimitedByYear.copy(
            timeUnit = timeUnit
        )
    ).toDomain()

    private fun createNewActivityInMinutes(timeForNewActivity: Long) = customActivity.copy(
        id = null,
        start = customActivity.start.plusDays(7), // Next monday
        end = customActivity.start.plusDays(7).plusMinutes(timeForNewActivity * 60),
        duration = timeForNewActivity.toInt() * 60 * 8,
        projectRole = projectRoleLimitedByYearMinutesVersion
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
            LocalDateTime.of(2023, 3, 6, 0, 0, 0),
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