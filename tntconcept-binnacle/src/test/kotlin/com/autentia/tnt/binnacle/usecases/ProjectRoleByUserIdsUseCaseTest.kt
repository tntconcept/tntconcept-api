package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.converters.ProjectRoleConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.HolidayService
import io.micronaut.security.authentication.ClientAuthentication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class ProjectRoleByUserIdsUseCaseTest {
    private val projectRoleResponseConverter = ProjectRoleResponseConverter()
    private val projectRoleConverter = ProjectRoleConverter()
    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val activityCalendarFactory = ActivitiesCalendarFactory(calendarFactory)
    private val activityCalendarService = ActivityCalendarService(calendarFactory, activityCalendarFactory)
    private val activityService =  mock<ActivityService>()

    private val projectRoleByUserIdsUseCase = ProjectRoleByUserIdsUseCase(activityService, activityCalendarService, projectRoleResponseConverter, projectRoleConverter)

    @Test
    fun `should return project roles for userIds`() {
        val userIds = listOf(USER_ID1, USER_ID2)
        val timeInterval = TimeInterval.ofYear(TODAY.year)

        val activities = listOf(
            createActivity().copy(projectRole = projectRole1).copy(start = TODAY.minusDays(15).atTime(7,30,0)).copy(end = TODAY.minusDays(15).atTime(9,0,0)).copy(userId = USER_ID1),
            createActivity().copy(projectRole = projectRole2).copy(userId = USER_ID2),
            createActivity().copy(projectRole = projectRole2).copy(userId = USER_ID2).copy(start = TODAY.minusDays(2).atStartOfDay()).copy(end = TODAY.minusDays(2).atTime(9,0,0)),
        )

        whenever(activityService.getActivities(timeInterval, userIds)).thenReturn(activities)

        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(1L, 30, 120, USER_ID1),
            buildProjectRoleUserDTO(1L, 120, 120, USER_ID2),
            buildProjectRoleUserDTO(2L, 0, 0, USER_ID2),
            buildProjectRoleUserDTO(2L, 0, 0, USER_ID1),
        )

        val result = projectRoleByUserIdsUseCase.get(userIds)

        assertTrue(result.isNotEmpty())
        expectedProjectRoles.forEach{expectedProjectRole ->
            assertTrue(result.contains(expectedProjectRole))
        }
    }

    private companion object {
        private const val USER_ID1 = 1L
        private const val USER_ID2 = 2L
        private val TODAY = LocalDate.now()
        private val projectRole1 = createProjectRole().copy(name = "Role ID 1").copy(maxAllowed = 120)
        private val projectRole2 = createProjectRole(id = 2).copy(name = "Role ID 2")

        private fun buildProjectRoleUserDTO(id: Long, remaining: Int, maxAllowed: Int, userId: Long): ProjectRoleUserDTO = ProjectRoleUserDTO(
            id = id,
            name = "Role ID $id",
            projectId = 1L,
            organizationId = 1L,
            maxAllowed = maxAllowed,
            remaining = remaining,
            timeUnit = TimeUnit.MINUTES,
            requireEvidence = RequireEvidence.WEEKLY,
            requireApproval = false,
            userId = userId
        )
    }
}