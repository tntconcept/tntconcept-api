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

internal class ActivityRetrievalByIdUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val userService = mock<UserService>()
    private val activityValidator = mock<ActivityValidator>()

    private val activityRetrievalByIdUseCase =
        ActivityRetrievalByIdUseCase(
            activityService,
            userService,
            activityValidator,
            ActivityResponseConverter(
                ActivityIntervalResponseConverter()
            )
    )

    @Test
    fun `return the activity`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(yesterdayActivity).whenever(activityService).getActivityById(2L)

        doReturn(true).whenever(activityValidator).userHasAccess(yesterdayActivity, USER)

        assertEquals(yesterdayActivityResponseDTO, activityRetrievalByIdUseCase.getActivityById(2L))
    }

    @Test
    fun `return null when authenticated user cannot access the activity`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(savedActivity).whenever(activityService).getActivityById(2L)

        doReturn(false).whenever(activityValidator).userHasAccess(savedActivity, USER)

        assertNull(activityRetrievalByIdUseCase.getActivityById(2L))
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

        private val PROJECT_ROLE_RESPONSE_DTO = ProjectRoleResponseDTO(10L, "Dummy Project role", RequireEvidence.NO)

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
