package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.utils.toDate
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.ApprovalState.NA
import com.autentia.tnt.binnacle.entities.ApprovalState.PENDING
import com.autentia.tnt.binnacle.entities.RequireEvidence.*
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.kotlin.*
import java.lang.IllegalArgumentException
import java.time.Instant
import java.time.LocalDate
import java.util.*

@TestInstance(PER_CLASS)
internal class ActivityUpdateUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val activityValidator = mock<ActivityValidator>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val userService = mock<UserService>()
    private val activityEvidenceService = mock<ActivityEvidenceService>()
    private val sendPendingApproveActivityMailUseCase = mock<SendPendingApproveActivityMailUseCase>()

    private val sut = ActivityUpdateUseCase(
            activityRepository,
            activityCalendarService,
            projectRoleRepository,
            userService,
            activityValidator,
            ActivityRequestBodyConverter(),
            ActivityResponseConverter(
                    ActivityIntervalResponseConverter()
            ),
            activityEvidenceService,
            sendPendingApproveActivityMailUseCase
    )

    @BeforeEach
    fun `set auth user`() {
        doReturn(USER).whenever(userService).getAuthenticatedDomainUser()
    }

    @BeforeEach
    fun `check that activity is valid`() {
        doNothing().whenever(activityValidator).checkActivityIsValidForUpdate(any(), any(), any())
    }

    @AfterEach
    fun `reset mocks`() {
        reset(activityRepository, activityValidator, activityEvidenceService,
                userService, sendPendingApproveActivityMailUseCase, projectRoleRepository, activityCalendarService)
    }

    @Test
    fun `should update an existing activity with no evidence in a role that does not require evidence nor approval`() {
        // Arrange
        val role = `get role that does not require evidence nor approval`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with no evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with no evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(NA)

        // Verify
        verifyNoInteractions(activityEvidenceService, sendPendingApproveActivityMailUseCase)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }

    @Test
    fun `should update an existing activity with no evidence in a role that requires evidence`() {
        // Arrange
        val role = `get role that requires evidence`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with no evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with no evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(NA)

        // Verify
        verifyNoInteractions(activityEvidenceService, sendPendingApproveActivityMailUseCase)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }

    @Test
    fun `should update an existing activity with no evidence in a role that requires approval`() {
        // Arrange
        val role = `get role that requires approval`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing pending activity with no evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with no evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(PENDING)

        // Verify
        verifyNoInteractions(activityEvidenceService, sendPendingApproveActivityMailUseCase)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }

    @Test
    fun `should update an existing activity with no evidence in a role that requires evidence and approval`() {
        // Arrange
        val role = `get role that requires evidence and approval`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing pending activity with no evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with no evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(PENDING)

        // Verify
        verifyNoInteractions(activityEvidenceService, sendPendingApproveActivityMailUseCase)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }

    @Test
    fun `should update an existing activity with new evidence in a role that does not require evidence nor approval`() {
        // Arrange
        val role = `get role that does not require evidence nor approval`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with no evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)
        doNothing().whenever(activityEvidenceService).storeActivityEvidence(eq(updatedActivity.id!!), eq(SAMPLE_EVIDENCE), any())

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(NA)

        // Verify
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(activityEvidenceService).storeActivityEvidence(eq(updatedActivity.id!!), eq(SAMPLE_EVIDENCE), any())
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }


    @Test
    fun `should update an existing activity with new evidence in a role that requires evidence`() {
        // Arrange
        val role = `get role that requires evidence`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with no evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)
        doNothing().whenever(activityEvidenceService).storeActivityEvidence(eq(updatedActivity.id!!), eq(SAMPLE_EVIDENCE), any())

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(NA)

        // Verify
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(activityEvidenceService).storeActivityEvidence(eq(updatedActivity.id!!), eq(SAMPLE_EVIDENCE), any())
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }

    @Test
    fun `should update an existing activity with new evidence in a role that requires evidence and approval`() {
        // Arrange
        val role = `get role that requires evidence and approval`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing pending activity with no evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)
        doNothing().whenever(activityEvidenceService).storeActivityEvidence(eq(updatedActivity.id!!), eq(SAMPLE_EVIDENCE), any())

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(PENDING)

        // Verify
        verify(sendPendingApproveActivityMailUseCase).send(updatedActivity.toDomain().copy(evidence = SAMPLE_EVIDENCE), USER.username, LOCALE)
        verify(activityEvidenceService).storeActivityEvidence(eq(updatedActivity.id!!), eq(SAMPLE_EVIDENCE), any())
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }

    @Test
    fun `should update an existing activity with evidence and remove the evidence in a role that does not require evidence nor approval`() {
        // Arrange
        val role = `get role that does not require evidence nor approval`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)
        whenever(activityEvidenceService.getActivityEvidence(existingActivity.id!!, existingActivity.insertDate!!)).thenReturn(
            EVIDENCE)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with no evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)
        doReturn(true).whenever(activityEvidenceService).deleteActivityEvidence(eq(updatedActivity.id!!), any())

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(NA)

        // Verify
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(activityEvidenceService).deleteActivityEvidence(eq(updatedActivity.id!!), any())
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }

    @Test
    fun `should update an existing activity with evidence and remove the evidence in a role that requires evidence`() {
        // Arrange
        val role = `get role that requires evidence`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)
        whenever(activityEvidenceService.getActivityEvidence(existingActivity.id!!, existingActivity.insertDate!!)).thenReturn(
            EVIDENCE)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with no evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)
        doReturn(true).whenever(activityEvidenceService).deleteActivityEvidence(eq(updatedActivity.id!!), any())

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(NA)

        // Verify
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(activityEvidenceService).deleteActivityEvidence(eq(updatedActivity.id!!), any())
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }

    @Test
    fun `should update an existing activity with evidence and remove the evidence in a role that requires evidence and approval`() {
        // Arrange
        val role = `get role that requires evidence and approval`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing pending activity with evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)
        whenever(activityEvidenceService.getActivityEvidence(existingActivity.id!!, existingActivity.insertDate!!)).thenReturn(
            EVIDENCE)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with no evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)
        doReturn(true).whenever(activityEvidenceService).deleteActivityEvidence(eq(updatedActivity.id!!), any())

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(PENDING)

        // Verify
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(activityEvidenceService).deleteActivityEvidence(eq(updatedActivity.id!!), any())
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }

    @Test
    fun `should not update an activity when user is not authenticated`() {
        // Arrange
        doThrow(IllegalStateException::class).whenever(userService).getAuthenticatedDomainUser()

        // Act, Assert
        assertThatThrownBy { sut.updateActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(IllegalStateException::class.java)

        // Verify
        verify(userService).getAuthenticatedDomainUser()
        verifyNoInteractions(activityRepository, activityValidator, activityEvidenceService, sendPendingApproveActivityMailUseCase, projectRoleRepository, activityCalendarService)
    }

    @Test
    fun `should not update an existing activity with a role that is not found`() {
        // Arrange
        whenever(projectRoleRepository.findById(any())).thenReturn(null)

        // Act, Assert
        assertThatThrownBy { sut.updateActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(ProjectRoleNotFoundException::class.java)

        // Verify
        verify(projectRoleRepository).findById(SOME_ACTIVITY_REQUEST.projectRoleId)
        verifyNoInteractions(activityRepository, activityValidator, activityEvidenceService, sendPendingApproveActivityMailUseCase, activityCalendarService)
        verifyNoMoreInteractions(projectRoleRepository)
    }

    @Test
    fun `should not update an existing activity when activity is not valid for updating`() {
        // Arrange
        val role = PROJECT_ROLE
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with no evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with no evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)

        doThrow(IllegalArgumentException::class).whenever(activityValidator)
                .checkActivityIsValidForUpdate(updatedActivity.toDomain(), existingActivity.toDomain(), USER)

        // Act, Assert
        assertThatThrownBy { sut.updateActivity(request, LOCALE) }.isInstanceOf(IllegalArgumentException::class.java)

        // Verify
        verify(projectRoleRepository).findById(request.projectRoleId)
        verify(activityRepository).findById(request.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityValidator).checkActivityIsValidForUpdate(updatedActivity.toDomain(), existingActivity.toDomain(), USER)
        verifyNoInteractions(activityEvidenceService, sendPendingApproveActivityMailUseCase)
        verifyNoMoreInteractions(projectRoleRepository, activityRepository, activityCalendarService)
    }

    @Test
    fun `should not update a non existing activity`() {
        // Arrange
        val role = PROJECT_ROLE
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)
        whenever(activityRepository.findById(any())).thenReturn(null)

        // Act, Assert
        assertThatThrownBy { sut.updateActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(ActivityNotFoundException::class.java)

        // Verify
        verify(projectRoleRepository).findById(SOME_ACTIVITY_REQUEST.projectRoleId)
        verify(activityRepository).findById(SOME_ACTIVITY_REQUEST.id!!)
        verifyNoInteractions(activityValidator, activityEvidenceService, activityCalendarService, sendPendingApproveActivityMailUseCase)
        verifyNoMoreInteractions(projectRoleRepository, activityRepository)
    }

    private fun `get activity updated with request`(existingActivity: Activity, request: ActivityRequestDTO, duration: Int): Activity =
            existingActivity.copy(
                    duration = duration,
                    start = request.interval.start,
                    end = request.interval.end,
                    description = request.description,
                    billable = request.billable,
                    hasEvidences = request.hasEvidences,
            )

    private fun `get existing pending activity with no evidence`(role: ProjectRole) =
            Activity.emptyActivity(role).copy(
                    id = 1L,
                    userId = USER.id,
                    hasEvidences = false,
                    approvalState = PENDING,
                    start = LocalDate.now().atTime(8, 0),
                    end = LocalDate.now().atTime(12, 0),
                    insertDate = Date.from(Instant.now()),
                    departmentId = 1L,
            )

    private fun `get existing pending activity with evidence`(role: ProjectRole) =
            Activity.emptyActivity(role).copy(
                    id = 1L,
                    userId = USER.id,
                    hasEvidences = true,
                    approvalState = PENDING,
                    start = LocalDate.now().atTime(8, 0),
                    end = LocalDate.now().atTime(12, 0),
                    insertDate = Date.from(Instant.now()),
                    departmentId = 1L,
            )

    private fun `get existing activity with evidence`(role: ProjectRole) =
            Activity.emptyActivity(role).copy(
                    id = 1L,
                    userId = USER.id,
                    hasEvidences = true,
                    approvalState = NA,
                    start = LocalDate.now().atTime(8, 0),
                    end = LocalDate.now().atTime(12, 0),
                    insertDate = Date.from(Instant.now()),
                    departmentId = 1L,
            )

    private fun `get activity update request with no evidence`(existingActivity: Activity, duration: Int) =
            ActivityRequestDTO(
                    id = existingActivity.id!!,
                    start = TODAY,
                    end = TODAY.plusMinutes(duration.toLong()),
                    description = existingActivity.description + " updated",
                    billable = existingActivity.billable,
                    projectRoleId = existingActivity.projectRole.id,
                    hasEvidences = false,
                    evidence = null,
            )

    private fun `get activity update request with evidence`(existingActivity: Activity, duration: Int) =
            ActivityRequestDTO(
                    id = existingActivity.id!!,
                    start = TODAY,
                    end = TODAY.plusMinutes(duration.toLong()),
                    description = existingActivity.description + " updated",
                    billable = existingActivity.billable,
                    projectRoleId = existingActivity.projectRole.id,
                    hasEvidences = true,
                    evidence = SAMPLE_EVIDENCE_DTO
            )

    private fun `get role that requires approval`() =
            PROJECT_ROLE.copy(isApprovalRequired = true, requireEvidence = NO, timeUnit = TimeUnit.MINUTES)

    private fun `get role that does not require evidence nor approval`() =
            PROJECT_ROLE.copy(isApprovalRequired = false, requireEvidence = NO, timeUnit = TimeUnit.MINUTES)

    private fun `get role that requires evidence`() =
            PROJECT_ROLE.copy(isApprovalRequired = false, requireEvidence = WEEKLY, timeUnit = TimeUnit.MINUTES)

    private fun `get role that requires evidence and approval`() =
            PROJECT_ROLE.copy(isApprovalRequired = true, requireEvidence = ONCE, timeUnit = TimeUnit.MINUTES)

    private fun `get existing activity with no evidence`(role: ProjectRole) =
            Activity.emptyActivity(role).copy(
                    id = 1L,
                    userId = USER.id,
                    hasEvidences = false,
                    approvalState = NA,
                    start = LocalDate.now().atTime(8, 0),
                    end = LocalDate.now().atTime(12, 0),
                    insertDate = Date.from(Instant.now()),
                    departmentId = 1L,
            )

    private fun assertThatUpdatedActivityIsEquivalent(result: ActivityResponseDTO, request: ActivityRequestDTO) {
        assertThat(result.interval.start).isEqualTo(request.interval.start)
        assertThat(result.interval.end).isEqualTo(request.interval.end)
        assertThat(result.description).isEqualTo(request.description)
        assertThat(result.billable).isEqualTo(request.billable)
        assertThat(result.hasEvidences).isEqualTo(request.hasEvidences)
        assertThat(result.projectRoleId).isEqualTo(request.projectRoleId)
    }

    private companion object {
        private val USER = createDomainUser()
        private val TODAY = LocalDate.now().atTime(8, 0)
        private val ORGANIZATION = Organization(1L, "Organization", listOf())
        private val PROJECT = Project(1L, "Project", open = true, billable = false,
                LocalDate.now(), null, null, projectRoles = listOf(),
                organization = ORGANIZATION
        )
        private val EVIDENCE = EvidenceDTO("mediaType", "fileContent")
        private val PROJECT_ROLE = ProjectRole(10L, "Project role", NO, PROJECT, 5000,
                1000, true, false, TimeUnit.MINUTES)
        private val LOCALE = Locale.ENGLISH
        private val SAMPLE_EVIDENCE_DTO = EvidenceDTO.from("data:image/png;base64,ABCsbG8gV29ybGQh")
        private val SAMPLE_EVIDENCE = SAMPLE_EVIDENCE_DTO.toDomain()
        private val SOME_ACTIVITY_REQUEST = ActivityRequestDTO(
                1L,
                TODAY,
                TODAY.plusMinutes(75L),
                "New activity",
                false,
                PROJECT_ROLE.id,
                false,
        )
    }

}

