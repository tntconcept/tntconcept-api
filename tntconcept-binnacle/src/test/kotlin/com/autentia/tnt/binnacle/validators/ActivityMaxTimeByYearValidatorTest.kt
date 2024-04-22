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
import io.archimedesfw.commons.time.test.ClockTestUtils


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.reset
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.*

private val mockNow = LocalDateTime.of(2023, Month.MARCH, 15, 0, 0, 0)

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
    private val timeInterval2023 = TimeInterval.ofYear(2023)


    @BeforeEach
    fun setupTest() {

        doReturn(Optional.of(project))
            .whenever(projectRepository)
            .findById(projectRoleLimitedByYear.project.id)
    }

    @AfterEach
    fun resetMocks() {
        reset(
            holidayRepository,
            activityRepository,
            projectRepository
        )
    }

    private fun testInputsInDays() = arrayOf(
        arrayOf(0, 1, TimeUnit.DAYS),
        arrayOf(0, 1, TimeUnit.NATURAL_DAYS),
        arrayOf(0, 2, TimeUnit.DAYS),
        arrayOf(0, 2, TimeUnit.NATURAL_DAYS),
        arrayOf(0, 3, TimeUnit.DAYS),
        arrayOf(0, 3, TimeUnit.NATURAL_DAYS),
        arrayOf(0, 4, TimeUnit.DAYS),
        arrayOf(0, 4, TimeUnit.NATURAL_DAYS),
        arrayOf(1, 1, TimeUnit.DAYS),
        arrayOf(1, 1, TimeUnit.NATURAL_DAYS),
        arrayOf(1, 2, TimeUnit.DAYS),
        arrayOf(1, 2, TimeUnit.NATURAL_DAYS),
        arrayOf(1, 3, TimeUnit.DAYS),
        arrayOf(1, 3, TimeUnit.NATURAL_DAYS),
        arrayOf(2, 1, TimeUnit.DAYS),
        arrayOf(2, 1, TimeUnit.NATURAL_DAYS),
        arrayOf(2, 2, TimeUnit.DAYS),
        arrayOf(2, 2, TimeUnit.NATURAL_DAYS),
        arrayOf(3, 1, TimeUnit.DAYS),
        arrayOf(3, 1, TimeUnit.NATURAL_DAYS),
    )

    @ParameterizedTest
    @MethodSource("testInputsInDays")
    fun `do not throw exception when activity is valid for creation in days or natural_days`(
        previousTimeRegisteredForProjectRole: Int,
        newActivityDuration: Long,
        timeUnit: TimeUnit
    ) {

        var newActivity = createNewActivity(newActivityDuration, timeUnit)

        val activities2023 = ArrayList<Activity>()

        if (previousTimeRegisteredForProjectRole > 0)
            for (index in 1..previousTimeRegisteredForProjectRole)
                activities2023.add(
                    createExistingActivity(previousTimeRegisteredForProjectRole, timeUnit, index)
                )

        doReturn(activities2023)
            .whenever(activityRepository)
            .findByProjectRoleIds(
                timeInterval2023.start,
                timeInterval2023.end,
                listOf(projectRoleLimitedByYear.id),
                user.id
            )


        ClockTestUtils.runWithFixed(
            mockNow
        ) {
            activityValidator.checkActivityIsValidForCreation(newActivity, user)
        }

    }

    private fun testInputsInDaysWithChangeOfYear() = arrayOf(
        arrayOf(0, 2, TimeUnit.DAYS),
        arrayOf(0, 2, TimeUnit.NATURAL_DAYS),
        arrayOf(0, 3, TimeUnit.DAYS),
        arrayOf(0, 3, TimeUnit.NATURAL_DAYS),
        arrayOf(0, 4, TimeUnit.DAYS),
        arrayOf(0, 4, TimeUnit.NATURAL_DAYS),
        arrayOf(1, 2, TimeUnit.DAYS),
        arrayOf(1, 2, TimeUnit.NATURAL_DAYS),
        arrayOf(1, 3, TimeUnit.DAYS),
        arrayOf(1, 3, TimeUnit.NATURAL_DAYS),
        arrayOf(2, 2, TimeUnit.DAYS),
        arrayOf(2, 2, TimeUnit.NATURAL_DAYS),
    )

    @ParameterizedTest
    @MethodSource("testInputsInDaysWithChangeOfYear")
    fun `do not throw exception when activity is valid for creation in days or natural days ending in different year`(
        previousTimeRegisteredForProjectRole: Int,
        newActivityDuration: Long,
        timeUnit: TimeUnit
    ) {

        val newActivity = createNewActivityWithChangeOfYear(newActivityDuration, timeUnit)

        val activities2023 = ArrayList<Activity>()

        if (previousTimeRegisteredForProjectRole > 0)
            for (index in 1..previousTimeRegisteredForProjectRole)
                activities2023.add(
                    createExistingActivity(previousTimeRegisteredForProjectRole, timeUnit, index)
                )

        doReturn(activities2023)
            .whenever(activityRepository)
            .findByProjectRoleIds(
                timeInterval2023.start,
                timeInterval2023.end,
                listOf(projectRoleLimitedByYear.id),
                user.id
            )
        ClockTestUtils.runWithFixed(
            mockNow
        ) {
            activityValidator.checkActivityIsValidForCreation(newActivity, user)
        }

    }

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
    @MethodSource("testInputsInMinutes")
    fun `do not throw exception when activity is valid for creation in minutes`(
        previousTimeRegisteredForProjectRole: Int,
        newActivityDuration: Long,
    ) {
        val newActivity = createNewActivityInMinutes(newActivityDuration)

        val activities2023 = ArrayList<Activity>()

        if (previousTimeRegisteredForProjectRole > 0)
            for (index in 1..previousTimeRegisteredForProjectRole)
                activities2023.add(
                    createExistingActivityInMinutes(previousTimeRegisteredForProjectRole, index)
                )

        doReturn(activities2023)
            .whenever(activityRepository)
            .findByProjectRoleIds(
                timeInterval2023.start,
                timeInterval2023.end,
                listOf(projectRoleLimitedByYear.id),
                user.id
            )
        ClockTestUtils.runWithFixed(
            mockNow
        ) {
            activityValidator.checkActivityIsValidForCreation(newActivity, user)
        }
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
            mockNow.toLocalDate().minusYears(1),
            null,
            null,
            Organization(1, "Organization", 1, emptyList()),
            emptyList(),
            "CLOSED_PRICE"
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
