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
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.HolidayService
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

internal class LatestProjectRolesForAuthenticatedUserUseCaseTest {
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val projectRoleResponseConverter = ProjectRoleResponseConverter()
    private val activityRepository: ActivityRepository = mock()
    private val activityService = ActivityService(
        activityRepository, mock(), mock(), mock()
    )
    private val holidayService = mock<HolidayService>()
    private val securityService = mock<SecurityService>()
    private val projectRoleConverter = ProjectRoleConverter()

    private val calendarFactory = CalendarFactory(holidayService)
    private val activityCalendarFactory = ActivitiesCalendarFactory(calendarFactory)
    private val activityCalendarService = ActivityCalendarService(calendarFactory, activityCalendarFactory)
    private val latestProjectRolesForAuthenticatedUserUseCase = LatestProjectRolesForAuthenticatedUserUseCase(
        projectRoleRepository,
        projectRoleResponseConverter,
        activityService,
        activityCalendarService,
        securityService,
        projectRoleConverter
    )

    @Test
    fun `return the last imputed roles ordered by activity start date without year parameter`() {
        val userId = 1L
        val timeInterval =
            TimeInterval.ofYear(BEGINNING_OF_THE_YEAR.year)

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

        whenever(activityRepository.findOfLatestProjects(timeInterval.start, timeInterval.end, userId)).thenReturn(
            activities
        )
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
        val timeInterval = TimeInterval.ofYear(year)
        val lastDayOfYear = LocalDate.of(year, 12, 31)

        val activities = listOf(
            createActivity().copy(
                projectRole = projectRole1,
                start = lastDayOfYear.minusDays(15).atTime(7, 30, 0),
                end = lastDayOfYear.minusDays(15).atTime(9, 0, 0)
            ),
            createActivity().copy(projectRole = projectRole2),
            createActivity().copy(
                projectRole = projectRole2,
                start = lastDayOfYear.minusDays(2).atStartOfDay(),
                end = lastDayOfYear.minusDays(2).atTime(9, 0, 0)
            )
        )

        whenever(activityRepository.findOfLatestProjects(timeInterval.start, timeInterval.end, userId)).thenReturn(
            activities
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))

        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(2L, 0, 0),
            buildProjectRoleUserDTO(1L, 30, 120),
        )

        assertEquals(expectedProjectRoles, latestProjectRolesForAuthenticatedUserUseCase.get(year))
    }

    @Test
    fun `return the last imputed roles ordered by activity start date with future year parameter`() {
        val userId = 1L
        val year = 2025
        val timeInterval =
            TimeInterval.ofYear(BEGINNING_OF_THE_YEAR.year)

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

        whenever(activityRepository.findOfLatestProjects(timeInterval.start, timeInterval.end, userId)).thenReturn(
            activities
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))

        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(2L, 0, 0),
            buildProjectRoleUserDTO(1L, 30, 120),
        )

        assertEquals(expectedProjectRoles, latestProjectRolesForAuthenticatedUserUseCase.get(year))
    }

    @Test
    fun `get project roles recent should return project roles`() {
        val startDate = TODAY.minusMonths(1).atTime(LocalTime.MIN)
        val endDate = TODAY.atTime(23, 59, 59)

        doReturn(PROJECT_ROLES_RECENT).whenever(projectRoleRepository)
            .findDistinctProjectRolesBetweenDate(startDate, endDate)

        val expectedProjectRoles = listOf(
            buildProjectRoleRecent(1L, START_DATE),
            buildProjectRoleRecent(2L, START_DATE.minusDays(2)),
        )

        assertEquals(expectedProjectRoles, latestProjectRolesForAuthenticatedUserUseCase.getProjectRolesRecent())
    }

    private companion object {
        private const val USER_ID = 1L
        private val TODAY = LocalDate.now()
        private val BEGINNING_OF_THE_YEAR = LocalDate.of(TODAY.year, 1, 1)
        private val END_OF_THE_YEAR = LocalDate.of(TODAY.year, 12, 31)
        private val START_DATE = TODAY.minusDays(1)
        private val END_DATE = TODAY.minusDays(4)
        private val projectRole1 = createProjectRole().copy(name = "Role ID 1").copy(maxAllowed = 120)
        private val projectRole2 = createProjectRole(id = 2).copy(name = "Role ID 2")
        private val authentication =
            ClientAuthentication(USER_ID.toString(), mapOf("roles" to listOf("admin")))

        private fun buildProjectRoleRecent(id: Long, date: LocalDate, projectOpen: Boolean = true) =
            ProjectRolesRecent(
                id = id,
                date = date.atTime(LocalTime.MIDNIGHT),
                name = "Role ID $id",
                projectBillable = false,
                projectOpen = projectOpen,
                projectName = "Project Name of role $id",
                organizationName = "Org Name of role $id",
                requireEvidence = RequireEvidence.WEEKLY
            )

        private fun buildProjectRoleUserDTO(id: Long, remaining: Int, maxAllowed: Int): ProjectRoleUserDTO =
            ProjectRoleUserDTO(
                id = id,
                name = "Role ID $id",
                projectId = 1L,
                organizationId = 1L,
                maxAllowed = maxAllowed,
                remaining = remaining,
                timeUnit = TimeUnit.MINUTES,
                requireEvidence = RequireEvidence.WEEKLY,
                requireApproval = false,
                userId = 1L
            )

        private val PROJECT_ROLES_RECENT = listOf(
            buildProjectRoleRecent(1L, START_DATE),
            buildProjectRoleRecent(2L, START_DATE.minusDays(2)),
            buildProjectRoleRecent(5L, START_DATE.minusDays(3), false),
            buildProjectRoleRecent(1L, END_DATE),
        )
    }
}