package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityEvidenceMailService
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.willDoNothing
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

internal class ActivityUpdateUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val activityValidator = mock<ActivityValidator>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val userService = mock<UserService>()
    private val activityEvidenceMailService = mock<ActivityEvidenceMailService>()
    private val activityEvidenceService = mock<ActivityEvidenceService>()

    private val activityUpdateUseCase = ActivityUpdateUseCase(
        activityRepository,
        activityCalendarService,
        projectRoleRepository,
        userService,
        activityValidator,
        ActivityRequestBodyConverter(),
        ActivityResponseConverter(
            ActivityIntervalResponseConverter()
        ),
        activityEvidenceMailService,
        activityEvidenceService
    )

    private val projectRole = createDomainProjectRole().copy(id = 10L)

    @Test
    fun `update activity and update the stored image`() {
        val activityId = NEW_ACTIVITY_DTO_WITH_EVIDENCES.id!!

        val oldActivityInsertDate = Date()
        val oldActivity = Activity(
            id = activityId,
            start = activityToUpdate.getStart(),
            duration = 120,
            end = activityToUpdate.getStart().plusHours(2L),
            description = "Test activity",
            projectRole = PROJECT_ROLE,
            userId = USER.id,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.NA,
            insertDate = oldActivityInsertDate,
        )

        doReturn(USER).whenever(userService).getAuthenticatedDomainUser()
        whenever(projectRoleRepository.findById(10L)).thenReturn(PROJECT_ROLE)
        whenever(activityRepository.findById(activityId)).thenReturn(oldActivity)
        willDoNothing().given(activityEvidenceService)
            .storeActivityEvidence(activityId, evidenceDomain, oldActivityInsertDate)
        whenever(activityRepository.update(any())).thenReturn(oldActivity)

        activityUpdateUseCase.updateActivity(NEW_ACTIVITY_DTO_WITH_EVIDENCES, Locale.ENGLISH)

        verify(activityEvidenceService).storeActivityEvidence(
            activityId, evidenceDomain, oldActivityInsertDate
        )
    }

    @Test
    fun `update activity should delete the stored image`() {
        val activityId = NEW_ACTIVITY_DTO.id!!

        val oldActivityInsertDate = Date()
        val oldActivity = Activity(
            id = activityId,
            start = activityToUpdate.getStart(),
            duration = 120,
            end = activityToUpdate.getStart().plusHours(2L),
            description = "Test activity",
            projectRole = PROJECT_ROLE,
            userId = USER.id,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.NA,
            insertDate = oldActivityInsertDate,
        )

        doReturn(USER).whenever(userService).getAuthenticatedDomainUser()
        whenever(projectRoleRepository.findById(10L)).thenReturn(PROJECT_ROLE)
        whenever(activityRepository.findById(activityId)).thenReturn(oldActivity)
        willDoNothing().given(activityEvidenceService)
            .storeActivityEvidence(activityToUpdate.id!!, evidence.toDomain(), oldActivityInsertDate)
        whenever(activityRepository.update(any())).thenReturn(oldActivity)
        whenever(activityEvidenceService.deleteActivityEvidence(activityId, oldActivityInsertDate)).thenReturn(
            true
        )

        activityUpdateUseCase.updateActivity(NEW_ACTIVITY_DTO, Locale.ENGLISH)

        verify(activityEvidenceService).deleteActivityEvidence(activityId, oldActivityInsertDate)
    }

    @Test
    fun `return updated activity for the authenticated user when it is valid`() {
        doReturn(USER).whenever(userService).getAuthenticatedDomainUser()

        doReturn(PROJECT_ROLE).whenever(projectRoleRepository).findById(projectRole.id)

        doReturn(currentTodayActivity).whenever(activityRepository).findById(todayActivity.id!!)

        doReturn(currentTodayActivity).whenever(activityRepository).update(any())

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

        doReturn(PROJECT_ROLE).whenever(projectRoleRepository)
            .findById(projectRole.id)

        doReturn(currentTodayActivity).whenever(activityRepository).findById(activity.id!!)

        doReturn(currentTodayActivity).whenever(activityRepository).update(any())

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

        doReturn(PROJECT_ROLE).whenever(projectRoleRepository)
            .findById(projectRole.id)

        doReturn(currentTodayActivity).whenever(activityRepository).findById(activity.id!!)
        doReturn(currentTodayActivity).whenever(activityRepository).update(any())

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
            hasEvidences = true,
            insertDate = LocalDateTime.now()
        )

        val currentActivity = Activity.of(activity, PROJECT_ROLE.copy(requireEvidence = RequireEvidence.WEEKLY))
        doReturn(USER).whenever(userService).getAuthenticatedDomainUser()

        doReturn(currentActivity).whenever(activityRepository).findById(activity.id!!)
        doReturn(currentActivity).whenever(activityRepository).update(any())

        doReturn(PROJECT_ROLE).whenever(projectRoleRepository)
            .findById(projectRole.id)

        activityUpdateUseCase.updateActivity(NEW_ACTIVITY_DTO, Locale.ENGLISH)

        verify(activityEvidenceMailService, times(1)).sendActivityEvidenceMail(
            currentActivity.toDomain(),
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
            LocalDate.now(),
            null,
            null,
            projectRoles = listOf(),
            organization = ORGANIZATION
        )

        private val evidence = EvidenceDTO.from("data:application/pdf;base64,SGVsbG8gV29ybGQh")
        private val evidenceDomain = evidence.toDomain()

        private val PROJECT_ROLE =
            ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, 0, true, false, TimeUnit.MINUTES)
        private val PROJECT_ROLE_RESPONSE_DTO = ProjectRoleUserDTO(
            10L,
            "Dummy Project role",
            PROJECT_ROLE.project.organization.id,
            PROJECT_ROLE.project.id,
            PROJECT_ROLE.maxTimeAllowedByYear,
            PROJECT_ROLE.maxTimeAllowedByYear,
            PROJECT_ROLE.timeUnit,
            PROJECT_ROLE.requireEvidence,
            PROJECT_ROLE.isApprovalRequired,
            USER.id
        )
        private val NEW_ACTIVITY_DTO = ActivityRequestDTO(
            1L,
            TODAY,
            TODAY.plusMinutes(75L),
            "New activity",
            false,
            PROJECT_ROLE.id,
            false,
        )
        private val NEW_ACTIVITY_DTO_WITH_EVIDENCES = ActivityRequestDTO(
            90,
            TODAY,
            TODAY.plusMinutes(75L),
            "New activity",
            false,
            PROJECT_ROLE.id,
            true,
            evidence = evidence
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
            ApprovalState.NA,
            null
        )

        private val currentActivity = Activity.of(activityToUpdate, PROJECT_ROLE)

        private val currentTodayActivity = Activity.of(todayActivity, PROJECT_ROLE)
    }
}

