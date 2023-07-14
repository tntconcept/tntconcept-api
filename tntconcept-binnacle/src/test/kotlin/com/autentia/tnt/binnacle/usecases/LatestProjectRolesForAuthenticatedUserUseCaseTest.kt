package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.converters.ProjectRoleConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.ProjectRolesRecent
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.MaxTimeAllowedDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.entities.dto.RemainingTimeInfoDTO
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

internal class LatestProjectRolesForAuthenticatedUserUseCaseTest {
    private val projectRoleResponseConverter = ProjectRoleResponseConverter()
    private val activityRepository: ActivityRepository = mock()
    private val holidayRepository = mock<HolidayRepository>()
    private val securityService = mock<SecurityService>()
    private val projectRoleConverter = ProjectRoleConverter()

    private val calendarFactory = CalendarFactory(holidayRepository)
    private val activityCalendarFactory = ActivitiesCalendarFactory(calendarFactory)
    private val activityCalendarService = ActivityCalendarService(calendarFactory, activityCalendarFactory)
    private val latestProjectRolesForAuthenticatedUserUseCase = LatestProjectRolesForAuthenticatedUserUseCase(
        projectRoleResponseConverter,
        activityRepository,
        activityCalendarService,
        securityService,
        projectRoleConverter
    )

    @Test
    fun `return the last imputed roles ordered by activity start date without year parameter`() {
        val userId = 1L
        val yearTimeInterval = TimeInterval.ofYear(BEGINNING_OF_THE_YEAR.year)
        val oneMonthTimeInterval = oneMonthTimeIntervalFromCurrentYear()

        val activities = listOf(
            createActivity().copy(
                projectRole = projectRole1,
                start = TODAY.minusDays(15).atTime(7, 30, 0),
                end = TODAY.minusDays(15).atTime(9, 0, 0)
            ),
            createActivity().copy(projectRole = projectRole2),
            createActivity().copy(
                projectRole = projectRole2,
                start = TODAY.minusDays(2).atStartOfDay(),
                end = TODAY.minusDays(2).atTime(9, 0, 0)
            )
        )

        whenever(
            activityRepository.findOfLatestProjects(
                yearTimeInterval.start,
                yearTimeInterval.end,
                userId
            )
        ).thenReturn(
            activities
        )
        whenever(activityRepository.findOfLatestProjects(oneMonthTimeInterval.start, oneMonthTimeInterval.end, userId))
            .thenReturn(activities)
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))

        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(2L, 0, 0),
            buildProjectRoleUserDTO(1L, 30, 120),
        )

        assertEquals(expectedProjectRoles, latestProjectRolesForAuthenticatedUserUseCase.get(null))
    }

    @Test
    fun `return the last imputed roles ordered by activity start date with past year parameter`() {
        val userId = 1L
        val year = 2021
        val yearTimeInterval = TimeInterval.ofYear(year)
        val yearTimeDate = yearTimeInterval.end
        val oneMonthTimeInterval = oneMonthTimeIntervalFromCurrentYear()

        val lastMonthActivities = listOf(
            createActivity().copy(
                projectRole = projectRole1,
                start = TODAY.minusDays(15).atTime(7, 30, 0),
                end = TODAY.minusDays(15).atTime(9, 0, 0)
            ),
            createActivity().copy(projectRole = projectRole2),
            createActivity().copy(
                projectRole = projectRole2,
                start = TODAY.minusDays(2).atStartOfDay(),
                end = TODAY.minusDays(2).atTime(9, 0, 0)
            )
        )

        val pastYearActivities = listOf(
            createActivity().copy(
                projectRole = projectRole1,
                start = yearTimeDate.minusDays(15).minusMinutes(30),
                end = yearTimeDate.minusDays(15)
            ),
            createActivity().copy(projectRole = projectRole2),
            createActivity().copy(
                projectRole = projectRole2,
                start = yearTimeDate.minusDays(2).minusHours(9),
                end = yearTimeDate.minusDays(2)
            )
        )

        whenever(
            activityRepository.findOfLatestProjects(
                yearTimeInterval.start,
                yearTimeInterval.end,
                userId
            )
        ).thenReturn(
            pastYearActivities
        )
        whenever(activityRepository.findOfLatestProjects(oneMonthTimeInterval.start, oneMonthTimeInterval.end, userId))
            .thenReturn(lastMonthActivities)
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))

        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(2L, 0, 0),
            buildProjectRoleUserDTO(1L, 90, 120),
        )

        assertEquals(expectedProjectRoles, latestProjectRolesForAuthenticatedUserUseCase.get(year))
    }

    @Test
    fun `return the last imputed roles ordered by activity start date with future year parameter`() {
        val userId = 1L
        val year = TODAY.year
        val yearTimeInterval = TimeInterval.ofYear(year)
        val oneMonthTimeInterval = oneMonthTimeIntervalFromCurrentYear()

        val activities = listOf(
            createActivity().copy(
                projectRole = projectRole1,
                start = TODAY.minusDays(15).atTime(7, 30, 0),
                end = TODAY.minusDays(15).atTime(9, 0, 0)
            ),
            createActivity().copy(
                projectRole = projectRole3,
                start = TODAY.minusDays(15).atTime(LocalTime.MIN),
                end = TODAY.minusDays(14).atTime(23, 59, 59)
            ),
            createActivity().copy(projectRole = projectRole2),
            createActivity().copy(
                projectRole = projectRole2,
                start = TODAY.minusDays(2).atStartOfDay(),
                end = TODAY.minusDays(2).atTime(9, 0, 0)
            )
        )

        whenever(
            activityRepository.findOfLatestProjects(
                yearTimeInterval.start,
                yearTimeInterval.end,
                userId
            )
        ).thenReturn(
            activities
        )
        whenever(activityRepository.findOfLatestProjects(oneMonthTimeInterval.start, oneMonthTimeInterval.end, userId))
            .thenReturn(activities)
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))

        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(2L, 0, 0),
            buildProjectRoleUserDTO(1L, 30, 120),
            buildProjectRoleUserDTO(3L, 0, 2, TimeUnit.NATURAL_DAYS),
        )

        assertEquals(expectedProjectRoles, latestProjectRolesForAuthenticatedUserUseCase.get(year))
    }

    private companion object {
        private const val USER_ID = 1L
        private val TODAY = LocalDate.now()
        private val BEGINNING_OF_THE_YEAR = LocalDate.of(TODAY.year, 1, 1)
        private val START_DATE = TODAY.minusDays(1)
        private val END_DATE = TODAY.minusDays(4)
        private val projectRole1 = createProjectRole().copy(name = "Role ID 1").copy(maxTimeAllowedByYear = 120)
        private val projectRole2 = createProjectRole(id = 2).copy(name = "Role ID 2")
        private val projectRole3 = createProjectRole(id = 3).copy(maxTimeAllowedByYear = 960).copy(name = "Role ID 3").copy(timeUnit = TimeUnit.NATURAL_DAYS)
        private val authentication =
            ClientAuthentication(USER_ID.toString(), mapOf("roles" to listOf("admin")))

        private fun buildProjectRoleUserDTO(id: Long, remaining: Int, maxTimeAllowedByYear: Int, timeUnit: TimeUnit? = TimeUnit.MINUTES): ProjectRoleUserDTO =
            ProjectRoleUserDTO(
                id,
                "Role ID $id",
                1L,
                1L,
                RequireEvidence.WEEKLY,
                false,
                1L,
                RemainingTimeInfoDTO(MaxTimeAllowedDTO(maxTimeAllowedByYear, 0), timeUnit!!, remaining)
            )

        private fun oneMonthTimeIntervalFromCurrentYear(): TimeInterval {
            val now = LocalDate.now()

            return TimeInterval.of(
                now.minusMonths(1).atTime(LocalTime.MIN),
                now.atTime(23, 59, 59)
            )
        }
    }
}