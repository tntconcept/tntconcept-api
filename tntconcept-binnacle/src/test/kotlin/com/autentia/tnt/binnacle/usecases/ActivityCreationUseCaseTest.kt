package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.*
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ProjectRoleService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Optional

internal class ActivityCreationUseCaseTest {

    private val user = createUser()
    private val activityService = mock<ActivityService>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val projectRoleService = ProjectRoleService(projectRoleRepository)
    private val activityRepository = mock<ActivityRepository>()
    private val activityValidator =
        ActivityValidator(activityRepository, activityCalendarService, projectRoleRepository)
    private val userService = mock<UserService>()

    private val activityCreationUseCase = ActivityCreationUseCase(
        activityService,
        activityCalendarService,
        projectRoleService,
        userService,
        activityValidator,
        ActivityRequestBodyConverter(),
        ActivityResponseConverter(
            ActivityIntervalResponseConverter()
        ),
        TimeIntervalConverter()
    )

    @Test
    fun `created activity`() {
        doReturn(user).whenever(userService).getAuthenticatedUser()

        val activity = createActivity(userId = user.id)

        val expectedResponseDTO = createActivityResponseDTO(userId = user.id)

        doReturn(activity).whenever(activityService).createActivity(any(), eq(user))
        doReturn(Optional.of(activity.projectRole)).whenever(projectRoleRepository).findById(any())

        val result = activityCreationUseCase.createActivity(ACTIVITY_REQUEST_BODY_DTO)

        assertEquals(expectedResponseDTO, result)
    }

    private companion object {
        private val TIME_NOW = LocalDateTime.now()

        private val ORGANIZATION = Organization(1L, "Dummy Organization", listOf())
        private val ORGANIZATION_DTO = OrganizationResponseDTO(1L, "Dummy Organization")

        private val PROJECT = Project(
            1L,
            "Dummy Project",
            open = true,
            billable = false,
            ORGANIZATION,
            listOf()
        )
        private val PROJECT_RESPONSE_DTO = ProjectResponseDTO(
            1L,
            "Dummy Project",
            open = true,
            billable = false,
        )
        private val PROJECT_ROLE = ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, true, false, TimeUnit.MINUTES)

        private val PROJECT_ROLE_RESPONSE_DTO = ProjectRoleUserDTO(10L, "Dummy Project role", ORGANIZATION_DTO.id, PROJECT.id, PROJECT_ROLE.maxAllowed, 5, PROJECT_ROLE.timeUnit, RequireEvidence.NO, PROJECT_ROLE.isApprovalRequired,1L)

        private val ACTIVITY_REQUEST_BODY_DTO = ActivityRequestBodyDTO(
            null,
            TIME_NOW,
            TIME_NOW.plusMinutes(75L),
            "New activity",
            false,
            PROJECT_ROLE.id,
            false,
            null,
        )

        private fun generateLargeDescription(mainMessage: String): String {
            var description = mainMessage
            for (i in 1..2048) {
                description += "A"
            }
            return description
        }

        private fun createActivity(
            id: Long = 1L,
            userId: Long = 1L,
            description: String = generateLargeDescription("New activity").substring(0, 2048),
            start: LocalDateTime = TIME_NOW,
            end: LocalDateTime = TIME_NOW.plusMinutes(75L),
            duration: Int = 75,
            billable: Boolean = false,
            hasEvidences: Boolean = false,
            projectRole: ProjectRole = PROJECT_ROLE,
            approvalState: ApprovalState = ApprovalState.NA
        ): Activity =
            Activity(
                id = id,
                userId = userId,
                description = description,
                start = start,
                end = end,
                duration = duration,
                billable = billable,
                hasEvidences = hasEvidences,
                projectRole = projectRole,
                approvalState = approvalState
            )

        private fun createActivityResponseDTO(
            id: Long = 1L,
            userId: Long = 0L,
            description: String = generateLargeDescription("New activity").substring(0, 2048),
            start: LocalDateTime = TIME_NOW,
            end: LocalDateTime = TIME_NOW.plusMinutes(75L),
            duration: Int = 75,
            billable: Boolean = false,
            hasEvidences: Boolean = false,
            projectRoleId:  Long = 10L,
            approvalState: ApprovalState = ApprovalState.NA
        ): ActivityResponseDTO =
            ActivityResponseDTO(
                billable,
                description,
                hasEvidences,
                id,
                projectRoleId,
                IntervalResponseDTO(start,end,duration, PROJECT_ROLE.timeUnit),
                userId,
                approvalState
            )

    }

}
