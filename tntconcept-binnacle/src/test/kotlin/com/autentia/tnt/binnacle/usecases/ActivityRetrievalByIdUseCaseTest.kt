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
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month
import org.junit.jupiter.api.Assertions.assertEquals

internal class ActivityRetrievalByIdUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val userService = mock<UserService>()

    private val activityRetrievalByIdUseCase =
        ActivityRetrievalByIdUseCase(
            activityService,
            ActivityResponseConverter(
                ActivityIntervalResponseConverter()
            )
        )

    @Test
    fun `return the activity`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(yesterdayActivity).whenever(activityService).getActivityById(2L)

        assertEquals(yesterdayActivityResponseDTO, activityRetrievalByIdUseCase.getActivityById(2L))
    }


    private companion object{
        private val USER = createUser()
        private val YESTERDAY = LocalDate.now().minusDays(1)
        private val ORGANIZATION_DTO = OrganizationResponseDTO(1L, "Dummy Organization")

        private val PROJECT_RESPONSE_DTO = ProjectResponseDTO(
            1L,
            "Dummy Project",
            open = true,
            billable = false
        )
        private val PROJECT_ROLE = ProjectRole(
            10L,
            "Dummy Project role",
            RequireEvidence.NO,
            Project(
                1L,
                "Dummy Project",
                true,
                false,
                Organization(1L, "Dummy Organization", listOf()),
                listOf(),
            ),
            0,
            true,
            false,
            TimeUnit.MINUTES
        )

        private val PROJECT_ROLE_RESPONSE_DTO = ProjectRoleUserDTO(10L, "Dummy Project role", PROJECT_ROLE.project.organization.id, PROJECT_ROLE.project.id, PROJECT_ROLE.maxAllowed, PROJECT_ROLE.maxAllowed, PROJECT_ROLE.timeUnit, PROJECT_ROLE.requireEvidence, PROJECT_ROLE.isApprovalRequired, USER.id)

        val yesterdayActivity = Activity(
            2L,
            YESTERDAY.atStartOfDay(),
            YESTERDAY.atStartOfDay().plusMinutes(540L),
            540,
            "Dummy description",
            PROJECT_ROLE,
            USER.id,
            true,
            approvalState = ApprovalState.NA
        )

        val savedActivity = Activity(
            2L,
            LocalDate.of(2020, Month.JULY, 2).atStartOfDay(),
            LocalDate.of(2020, Month.JULY, 2).atStartOfDay().plusMinutes(540),
            540,
            "Dummy description",
            PROJECT_ROLE,
            33L,
            true,
            approvalState = ApprovalState.NA
        )
        val yesterdayActivityResponseDTO = ActivityResponseDTO(
            true,
            "Dummy description",
            false,
            2L,
            PROJECT_ROLE_RESPONSE_DTO.id,
            IntervalResponseDTO(YESTERDAY.atStartOfDay(),
                YESTERDAY.atStartOfDay().plusMinutes(540L), 540, PROJECT_ROLE.timeUnit),
            USER.id,
            approvalState = ApprovalState.NA
        )

    }
}
