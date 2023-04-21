package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.converters.TimeIntervalConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ApproveActivityMailService
import com.autentia.tnt.binnacle.services.ProjectRoleService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Locale

internal class ActivityCreationUseCaseTest {

    private val user = createUser()
    private val activityService = mock<ActivityService>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val approveActivityMailService = mock<ApproveActivityMailService>()
    private val activityResponseConverter = ActivityResponseConverter(ActivityIntervalResponseConverter())
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
        TimeIntervalConverter(),
        approveActivityMailService,
    )

    @Test
    fun `created activity`() {
        val activity = createActivity(userId = user.id)
        whenever(userService.getAuthenticatedUser()).thenReturn(user)
        whenever(activityService.createActivity(any(), eq(user))).thenReturn(activity)
        whenever(projectRoleRepository.findById(any())).thenReturn(activity.projectRole)

        val result = activityCreationUseCase.createActivity(ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO, Locale.ENGLISH)

        val expectedResponseDTO = createActivityResponseDTO(userId = user.id)
        assertEquals(expectedResponseDTO, result)
    }

    @Test
    fun `created activity with approval required`() {
        val activity = createActivity(userId = user.id, projectRole = PROJECT_ROLE_APPROVAL)
        whenever(userService.getAuthenticatedUser()).thenReturn(user)
        whenever(activityService.createActivity(any(), eq(user))).thenReturn(activity)
        whenever(projectRoleRepository.findById(any())).thenReturn(activity.projectRole)

        val result = activityCreationUseCase.createActivity(ACTIVITY_APPROVAL_REQUEST_BODY_DTO, Locale.ENGLISH)

        val expectedResponseDTO = createActivityResponseDTO(userId = user.id)
        assertEquals(expectedResponseDTO, result)
        verify(approveActivityMailService, times(1)).sendApprovalActivityMail(
            activityResponseConverter.mapActivityToActivityResponse(activity),
            user.username,
            Locale.ENGLISH
        )
    }

    @Test
    fun `created activity without approval required`() {
        val activity = createActivity(userId = user.id, projectRole = PROJECT_ROLE_NO_APPROVAL)
        whenever(userService.getAuthenticatedUser()).thenReturn(user)
        whenever(activityService.createActivity(any(), eq(user))).thenReturn(activity)
        whenever(projectRoleRepository.findById(any())).thenReturn(activity.projectRole)

        val result = activityCreationUseCase.createActivity(ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO, Locale.ENGLISH)

        val expectedResponseDTO = createActivityResponseDTO(userId = user.id)
        assertEquals(expectedResponseDTO, result)
        verify(approveActivityMailService, times(0)).sendApprovalActivityMail(
            activityResponseConverter.mapActivityToActivityResponse(activity),
            user.email,
            Locale.ENGLISH
        )
    }

    private companion object {
        private val TIME_NOW = LocalDateTime.now()

        private val ORGANIZATION = Organization(1L, "Dummy Organization", listOf())

        private val PROJECT = Project(
            1L,
            "Dummy Project",
            open = true,
            billable = false,
            ORGANIZATION,
            listOf()
        )
        private val PROJECT_ROLE_NO_APPROVAL =
            ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, true, false, TimeUnit.MINUTES)

        private val PROJECT_ROLE_APPROVAL =
            ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, true, true, TimeUnit.MINUTES)


        private val ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO = ActivityRequestBodyDTO(
            null,
            TIME_NOW,
            TIME_NOW.plusMinutes(75L),
            "New activity",
            false,
            PROJECT_ROLE_NO_APPROVAL.id,
            false,
            null,
        )

        private val ACTIVITY_APPROVAL_REQUEST_BODY_DTO = ActivityRequestBodyDTO(
            null,
            TIME_NOW,
            TIME_NOW.plusMinutes(75L),
            "New activity",
            false,
            PROJECT_ROLE_APPROVAL.id,
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
            projectRole: ProjectRole = PROJECT_ROLE_NO_APPROVAL,
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
            projectRoleId: Long = 10L,
            approvalState: ApprovalState = ApprovalState.NA
        ): ActivityResponseDTO =
            ActivityResponseDTO(
                billable,
                description,
                hasEvidences,
                id,
                projectRoleId,
                IntervalResponseDTO(start, end, duration, PROJECT_ROLE_NO_APPROVAL.timeUnit),
                userId,
                approvalState
            )

    }

}
