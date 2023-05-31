package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityUseCaseRequest
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.util.*

internal class ActivityUpdateUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val activityValidator = mock<ActivityValidator>()
    private val projectRoleService = mock<ProjectRoleService>()
    private val userService = mock<UserService>()
    private val activityEvidenceMailService = mock<ActivityEvidenceMailService>()

    private val activityUpdateUseCase = ActivityUpdateUseCase(
        activityService,
        activityCalendarService,
        projectRoleService,
        userService,
        activityValidator,
        ActivityRequestBodyConverter(),
        ActivityResponseConverter(
            ActivityIntervalResponseConverter()
        ),
        activityEvidenceMailService
    )

    private val projectRole = createDomainProjectRole().copy(id = 10L)

    @Test
    fun `return updated activity for the authenticated user when it is valid`() {
        doReturn(USER).whenever(userService).getAuthenticatedDomainUser()

        doReturn(projectRole).whenever(projectRoleService).getByProjectRoleId(projectRole.id)

        doReturn(todayActivity).whenever(activityService).getActivityById(todayActivity.id!!)

        doReturn(todayActivity).whenever(activityService).updateActivity(any(), eq(null))

        assertEquals(todayActivityResponseDTO, activityUpdateUseCase.updateActivity(NEW_ACTIVITY_DTO, Locale.ENGLISH))
    }

    @Test
    fun `rethrow any exception from the validator`() {
        doReturn(USER).whenever(userService).getAuthenticatedDomainUser()

        doAnswer { throw Exception() }.whenever(activityValidator)
            .checkActivityIsValidForUpdate(activityToUpdate, activityToUpdate, USER)

        assertThrows<Exception> { activityUpdateUseCase.updateActivity(NEW_ACTIVITY_DTO, Locale.ENGLISH) }
    }

    @Test
    fun `test email is not sent, only project role requireEvidence is true `() {

        val activity = todayActivity.copy(
            approvalState = ApprovalState.ACCEPTED,
            projectRole = todayActivity.projectRole.copy(requireEvidence = RequireEvidence.WEEKLY)
        )

        doReturn(USER).whenever(userService).getAuthenticatedDomainUser()

        doReturn(projectRole).whenever(projectRoleService)
            .getByProjectRoleId(projectRole.id)

        doReturn(activity).whenever(activityService).getActivityById(activity.id!!)

        doReturn(activity).whenever(activityService).updateActivity(any(), eq(null))

        activityUpdateUseCase.updateActivity(NEW_ACTIVITY_DTO.copy(hasEvidences = false), Locale.ENGLISH)

        verify(activityEvidenceMailService, times(0)).sendActivityEvidenceMail(
            activity,
            USER.username,
            Locale.ENGLISH
        )
    }

    @Test
    fun `test email is not sent, only requireEvidence is true and approval state is pending `() {

        val activity = todayActivity.copy(
            approvalState = ApprovalState.PENDING,
            projectRole = todayActivity.projectRole.copy(requireEvidence = RequireEvidence.WEEKLY)
        )
        doReturn(USER).whenever(userService).getAuthenticatedDomainUser()

        doReturn(projectRole).whenever(projectRoleService)
            .getByProjectRoleId(projectRole.id)

        doReturn(activity).whenever(activityService).getActivityById(activity.id!!)
        doReturn(activity).whenever(activityService).updateActivity(any(), eq(null))

        activityUpdateUseCase.updateActivity(NEW_ACTIVITY_DTO, Locale.ENGLISH)

        verify(activityEvidenceMailService, times(0)).sendActivityEvidenceMail(
            activity,
            USER.username,
            Locale.ENGLISH
        )
    }

    @Test
    fun `test email is sent, requireEvidence is true, approval state is pending and hasEvidences is true`() {

        val activity = todayActivity.copy(
            approvalState = ApprovalState.PENDING,
            projectRole = todayActivity.projectRole.copy(requireEvidence = RequireEvidence.WEEKLY),
            hasEvidences = true
        )
        doReturn(USER).whenever(userService).getAuthenticatedDomainUser()

        doReturn(projectRole).whenever(projectRoleService)
            .getByProjectRoleId(projectRole.id)
        doReturn(activity).whenever(activityService).getActivityById(activity.id!!)
        doReturn(activity).whenever(activityService).updateActivity(any(), eq(null))

        activityUpdateUseCase.updateActivity(NEW_ACTIVITY_DTO, Locale.ENGLISH)

        verify(activityEvidenceMailService, times(1)).sendActivityEvidenceMail(
            activity,
            USER.username,
            Locale.ENGLISH
        )
    }

    private companion object {
        private val USER = createDomainUser()
        private val TODAY = LocalDateTime.now()
        private val ORGANIZATION = Organization(1L, "Dummy Organization", listOf())

        private val PROJECT = Project(
            1L,
            "Dummy Project",
            open = true,
            billable = false,
            projectRoles = listOf(),
            organization = ORGANIZATION
        )

        private val PROJECT_ROLE =
            ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, true, false, TimeUnit.MINUTES)
        private val PROJECT_ROLE_RESPONSE_DTO = ProjectRoleUserDTO(
            10L,
            "Dummy Project role",
            PROJECT_ROLE.project.organization.id,
            PROJECT_ROLE.project.id,
            PROJECT_ROLE.maxAllowed,
            PROJECT_ROLE.maxAllowed,
            PROJECT_ROLE.timeUnit,
            PROJECT_ROLE.requireEvidence,
            PROJECT_ROLE.isApprovalRequired,
            USER.id
        )
        private val NEW_ACTIVITY_DTO = ActivityUseCaseRequest(
            1L,
            TODAY,
            TODAY.plusMinutes(75L),
            "New activity",
            false,
            PROJECT_ROLE.id,
            false,
        )
        private val todayActivity = createDomainActivity(
            TODAY,
            TODAY.plusMinutes(75L),
            75,
            PROJECT_ROLE.toDomain(),
        )
        private val todayActivityResponseDTO = ActivityResponseDTO(
            true,
            "Description",
            false,
            1,
            PROJECT_ROLE_RESPONSE_DTO.id,
            IntervalResponseDTO(TODAY, TODAY.plusMinutes(75L), 75, PROJECT_ROLE.timeUnit),
            USER.id,
            approvalState = ApprovalState.NA
        )
        private val activityToUpdate = com.autentia.tnt.binnacle.core.domain.Activity.of(
            1L,
            TimeInterval.of(TODAY, TODAY.plusMinutes(75L)),
            75,
            "New activity",
            PROJECT_ROLE.toDomain(),
            1L,
            false,
            null,
            LocalDateTime.now(),
            false,
            ApprovalState.NA
        )
    }
}

