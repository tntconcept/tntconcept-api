package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ProjectRoleByProjectIdUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val activityCalendarFactory = ActivitiesCalendarFactory(calendarFactory)
    private val activityCalendarService = ActivityCalendarService(calendarFactory, activityCalendarFactory)
    private val projectRoleResponseConverter = ProjectRoleResponseConverter()
    private val projectRoleByProjectIdUseCase =
        ProjectRoleByProjectIdUseCase(activityService, activityCalendarService, projectRoleResponseConverter)

    @Test
    fun `return the expected project role`() {

        val timeInterval = TimeInterval.ofYear(2023)

        val activities = listOf(
            createActivity().copy(projectRole = createProjectRole().copy(name = "Role ID 1")),
            createActivity().copy(projectRole = createProjectRole(id = 2).copy(name = "Role ID 2"))
        )

        doReturn(activities).whenever(activityService).getActivitiesByProjectId(timeInterval, 1L)

        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(1L),
            buildProjectRoleUserDTO(2L),
        )

        assertEquals(expectedProjectRoles, projectRoleByProjectIdUseCase.get(PROJECT_ID))
    }

    private companion object {
        private const val PROJECT_ID = 1L

        private val ORGANIZATION = Organization(1L, "Nuestra empresa", listOf())
        private val PROJECT = Project(1L, "Dummy project", true, false, ORGANIZATION, listOf())

        private val PROJECT_ROLE =
            ProjectRole(PROJECT_ID, "Dummy Role", RequireEvidence.NO, PROJECT, 0, true, false, TimeUnit.MINUTES)


    }

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
}
