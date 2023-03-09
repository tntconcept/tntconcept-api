package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityDateConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.converters.OrganizationResponseConverter
import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

internal class ActivitiesBetweenDateUseCaseTest {

    private var userService = mock<UserService>()
    private var activityService = mock<ActivityService>()
    private val activityDateConverter = ActivityDateConverter(
        WorkableProjectRoleIdChecker(emptyList()),
        ActivityResponseConverter(
            OrganizationResponseConverter(),
            ProjectResponseConverter(),
            ProjectRoleResponseConverter()
        )
    )

    private var sut = ActivitiesBetweenDateUseCase(activityService, userService, activityDateConverter)

    @Test
    fun `given start date and end date should return activities`() {
        val startDate = NOW.minusDays(1)
        val endDate = NOW.plusDays(1)

        doReturn(USER).whenever(userService).getAuthenticatedUser()
        doReturn(listOf(ACTIVITY_RESPONSE)).whenever(activityService).getActivitiesBetweenDates(startDate, endDate, USER.id)

        val result = sut.getActivities(startDate, endDate)

        verify(userService).getAuthenticatedUser()
        verify(activityService).getActivitiesBetweenDates(startDate, endDate, USER.id)

        val expectedResult = ActivityDateDTO(NOW, WORKED_TIME, listOf(ACTIVITY_RESPONSE_DTO))
        assertTrue(result.contains(expectedResult))
    }

    private companion object {
        private val TIME_NOW = LocalDateTime.now()
        private val NOW  = TIME_NOW.toLocalDate()

        private val USER = createUser()

        private val ORGANIZATION = Organization(1, "organization", emptyList())
        private val PROJECT = Project(1, "project", true, true, ORGANIZATION, emptyList())
        private val PROJECT_ROLE = ProjectRole(1, "Role", RequireEvidence.NO, PROJECT, 0, true, false)
        private val WORKED_TIME = 120

        private val ACTIVITY_RESPONSE = ActivityResponse(
            1,
            TIME_NOW,
            TIME_NOW,
            WORKED_TIME,
            "Activity",
            PROJECT_ROLE,
            USER.id,
            true,
            ORGANIZATION,
            PROJECT,
            false,
            ApprovalState.PENDING
        )

        private val ACTIVITY_RESPONSE_DTO = ActivityResponseDTO(
            ACTIVITY_RESPONSE.id,
            TIME_NOW,
            TIME_NOW,
            ACTIVITY_RESPONSE.duration,
            ACTIVITY_RESPONSE.description,
            ProjectRoleResponseDTO(PROJECT_ROLE.id, PROJECT_ROLE.name, PROJECT_ROLE.requireEvidence),
            ACTIVITY_RESPONSE.userId,
            ACTIVITY_RESPONSE.billable,
            OrganizationResponseDTO(ORGANIZATION.id, ORGANIZATION.name),
            ProjectResponseDTO(PROJECT.id, PROJECT.name, PROJECT.open, PROJECT.billable),
            ACTIVITY_RESPONSE.hasEvidences,
            ACTIVITY_RESPONSE.approvalState
        )

    }

}
