package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRole
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime

internal class LatestProjectRolesForAuthenticatedUserUseCaseTest {
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val projectRoleResponseConverter = ProjectRoleResponseConverter()
    private val activityService = mock<ActivityService>()
    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val activityCalendarFactory = ActivitiesCalendarFactory(calendarFactory)
    private val activityCalendarService = ActivityCalendarService(calendarFactory, activityCalendarFactory)
    private val latestProjectRolesForAuthenticatedUserUseCase = LatestProjectRolesForAuthenticatedUserUseCase(
        projectRoleRepository, projectRoleResponseConverter, activityService, activityCalendarService
    )

    @Test
    fun `return the last imputed roles`() {
        val startDate = BEGINNING_OF_THE_YEAR.atTime(LocalTime.MIN)
        val endDate = TODAY.atTime(23, 59, 59)
        val timeInterval = TimeInterval.of(startDate, endDate)

        val activities = listOf(
            createActivity().copy(projectRole = createProjectRole().copy(name = "Role ID 1")).copy(start = TODAY.minusDays(15).atStartOfDay()).copy(end = TODAY.minusDays(15).atTime(9,0,0)),
            createActivity().copy(projectRole = createProjectRole(id = 2).copy(name = "Role ID 2"))
        )

        doReturn(activities).whenever(activityService)
            .getActivitiesOfLatestProjects(timeInterval)

        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(1L),
        )

        assertEquals(expectedProjectRoles, latestProjectRolesForAuthenticatedUserUseCase.get())
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
        private val TODAY = LocalDate.now()
        private val BEGINNING_OF_THE_YEAR = LocalDate.of(TODAY.year, 1, 1)
        private val START_DATE = TODAY.minusDays(1)
        private val END_DATE = TODAY.minusDays(4)

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