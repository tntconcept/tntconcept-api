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
import org.assertj.core.api.Assumptions.assumeThat
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.jupiter.api.AfterEach
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.reset
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@RunWith(Theories::class)
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

    @Theory
    fun `do nothing when activity is valid for creation in days and natural days`(
        timeUsedForCurrentActivities: Int,
        timeForNewActivity: Long,
        timeUnit: TimeUnit,
        isThisYear: Boolean,
    ) {
        assumeThat(timeForNewActivity + timeUsedForCurrentActivities).isLessThanOrEqualTo(4)

        val activityCanBeConfiguredWithAChangeOfYear =
            !isThisYear && timeUsedForCurrentActivities <= 2 && timeUnit != TimeUnit.MINUTES

        var newActivity = customActivity.copy(
            id = null,
            start = customActivity.start.plusMonths(1),
            end =
            if (timeUnit != TimeUnit.MINUTES) customActivity.start.plusMonths(1).plusDays(timeForNewActivity - 1)
            else customActivity.start.plusMonths(1).plusMinutes(timeForNewActivity * 60),
            duration = timeForNewActivity.toInt() * 60 * 8,
            projectRole = projectRoleLimitedByYear.copy(timeUnit = timeUnit)
        ).toDomain()

        if (activityCanBeConfiguredWithAChangeOfYear)
            newActivity = customActivity.copy(
                id = null,
                start = LocalDate.of(
                    2023,
                    12,
                    if (TimeUnit.DAYS == timeUnit) 29 else 31
                ).atTime(0, 0, 0),
                end = LocalDate.of(
                    2024,
                    1,
                    1
                ).atTime(0, 0, 0),
                duration = timeForNewActivity.toInt() * 60 * 8,
                projectRole = projectRoleLimitedByYear.copy(timeUnit = timeUnit)
            ).toDomain()

        if (activityCanBeConfiguredWithAChangeOfYear || isThisYear) {
            val activities2023 = ArrayList<Activity>()

            if (timeUsedForCurrentActivities > 0)
                for (index in 1..timeUsedForCurrentActivities)
                    activities2023.add(
                        customActivity.copy(
                            id = timeUsedForCurrentActivities.toLong(),
                            start = if (timeUnit != TimeUnit.MINUTES) customActivity.start.plusDays(index.toLong() - 1)
                            else customActivity.start.plusHours(index.toLong()),
                            end = if (timeUnit != TimeUnit.MINUTES) customActivity.start.plusDays(index.toLong() - 1)
                            else customActivity.start.plusHours(index.toLong()).plusMinutes(60),
                            duration = if (timeUnit != TimeUnit.MINUTES) timeUsedForCurrentActivities * 60 * 8
                            else 60,
                            projectRole = projectRoleLimitedByYear.copy(timeUnit = timeUnit)
                        )
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

    private companion object {

        @DataPoints
        @JvmField
        val timeUsedForCurrentActivities: List<Int> = listOf(0, 1, 2, 3, 4)

        @DataPoints
        @JvmField
        val timeForNewActivity: List<Long> = listOf(1L, 2L, 3L, 4L)

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
            1920,
            0,
            true,
            false,
            TimeUnit.MINUTES
        )

        private val customActivity = Activity(
            1L,
            LocalDateTime.of(2023, 3, 1, 13, 5, 25),
            LocalDateTime.of(2023, 3, 1, 13, 5, 25).plusHours(1),
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