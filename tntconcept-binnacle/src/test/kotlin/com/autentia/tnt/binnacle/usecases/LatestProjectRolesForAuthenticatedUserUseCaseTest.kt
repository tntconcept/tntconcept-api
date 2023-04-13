package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.ProjectRolesRecent
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.binnacle.services.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime

internal class LatestProjectRolesForAuthenticatedUserUseCaseTest {

    private val userService = mock<UserService>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val projectRoleResponseConverter = ProjectRoleResponseConverter()
    private val activityService = mock<ActivityService>()
    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val activityCalendarFactory = ActivitiesCalendarFactory(calendarFactory)
    private val activityCalendarService = ActivityCalendarService(calendarFactory, activityCalendarFactory)
    private val latestProjectRolesForAuthenticatedUserUseCase = LatestProjectRolesForAuthenticatedUserUseCase(
        userService, projectRoleRepository, projectRoleResponseConverter, activityService, activityCalendarService
    )

    @Test
    fun testGetProjectRoleRecent() {

        val startDate = TODAY.minusMonths(1).atTime(LocalTime.MIN)
        val endDate = TODAY.atTime(23, 59, 59)
        val lastMonthTimeInterval = TimeInterval.of(startDate, endDate)

        val activities = listOf(
            createActivity().copy(projectRole = createProjectRole().copy(name = "Role ID 1")),
            createActivity().copy(projectRole = createProjectRole(id = 2).copy(name = "Role ID 2"))
        )

        doReturn(USER).whenever(userService).getAuthenticatedUser()
        doReturn(activities).whenever(activityService)
            .getActivitiesOfLatestProjects(lastMonthTimeInterval)

        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(1L),
            buildProjectRoleUserDTO(2L),
        )

        assertEquals(expectedProjectRoles, latestProjectRolesForAuthenticatedUserUseCase.get())
    }

    @Test
    fun testGetProjectRolesRecent() {
        val startDate = TODAY.minusMonths(1).atTime(LocalTime.MIN)
        val endDate = TODAY.atTime(23, 59, 59)

        doReturn(USER).whenever(userService).getAuthenticatedUser()
        doReturn(PROJECT_ROLES_RECENT).whenever(projectRoleRepository)
            .findDistinctProjectRolesBetweenDate(startDate, endDate, USER.id)

        val expectedProjectRoles = listOf(
            buildProjectRolesRecent(1L, START_DATE),
            buildProjectRolesRecent(2L, START_DATE.minusDays(2)),
        )

        assertEquals(expectedProjectRoles, latestProjectRolesForAuthenticatedUserUseCase.getProjectRolesRecent())
    }

    private companion object {
        private val TODAY = LocalDate.now()
        private val USER = createUser()
        private val START_DATE = TODAY.minusDays(1)
        private val END_DATE = TODAY.minusDays(4)

        private fun buildProjectRolesRecent(id: Long, date: LocalDate, projectOpen: Boolean = true) =
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

        private fun buildProjectRoleUserDTO(id: Long): ProjectRoleUserDTO = ProjectRoleUserDTO(
            id = id,
            name = "Role ID $id",
            projectId = 1L,
            organizationId = 1L,
            maxAllowed = 0,
            remaining = 0,
            timeUnit = TimeUnit.MINUTES,
            requireEvidence = RequireEvidence.WEEKLY,
            requireApproval = false,
            userId = USER.id
        )

        private val PROJECT_ROLES_RECENT = listOf(
            buildProjectRolesRecent(1L, START_DATE),
            buildProjectRolesRecent(2L, START_DATE.minusDays(2)),
            buildProjectRolesRecent(5L, START_DATE.minusDays(3), false),
            buildProjectRolesRecent(1L, END_DATE),
        )
    }
}