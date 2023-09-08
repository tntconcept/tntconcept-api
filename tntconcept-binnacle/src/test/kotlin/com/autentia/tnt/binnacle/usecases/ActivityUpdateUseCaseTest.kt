package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.converters.ActivityEvidenceResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.ApprovalState.NA
import com.autentia.tnt.binnacle.entities.ApprovalState.PENDING
import com.autentia.tnt.binnacle.entities.RequireEvidence.*
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.kotlin.*
import java.lang.IllegalArgumentException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(PER_CLASS)
internal class ActivityUpdateUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val activityValidator = mock<ActivityValidator>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val userService = mock<UserService>()
    private val sendPendingApproveActivityMailUseCase = mock<SendPendingApproveActivityMailUseCase>()
    private val attachmentInfoRepository = mock<AttachmentInfoRepository>()
    private val activityRequestBodyConverter = ActivityRequestBodyConverter()
    private val activityResponseConverter = ActivityResponseConverter(
            ActivityIntervalResponseConverter(),
            ActivityEvidenceResponseConverter()
    )

    private val sut = ActivityUpdateUseCase(
            activityRepository,
            activityCalendarService,
            projectRoleRepository,
            userService,
            activityValidator,
            activityRequestBodyConverter,
            activityResponseConverter,
            sendPendingApproveActivityMailUseCase,
            attachmentInfoRepository
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
        reset(activityRepository, activityValidator, attachmentInfoRepository,
                userService, sendPendingApproveActivityMailUseCase, projectRoleRepository, activityCalendarService)
    }

    @Test
    fun `should update an existing activity with no evidence in a role that does not require evidence nor approval`() {
        // Arrange
        val role = `get role that does not require evidence nor approval`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with no evidence`(role, NA)
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
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
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

        val existingActivity = `get existing activity with no evidence`(role, NA)
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
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
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
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
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
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
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

        val existingActivity = `get existing activity with no evidence`(role, NA)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with evidence`(existingActivity, duration, SAMPLE_EVIDENCES_IDS)
        whenever(attachmentInfoRepository.findByIds(SAMPLE_EVIDENCES_IDS)).thenReturn(SAMPLE_EVIDENCES)

        val updatedActivity = `get activity updated with request`(existingActivity, request, duration, SAMPLE_EVIDENCES.map { it.copy(isTemporary = false) }.toMutableList())
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(NA)

        // Verify
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)

        verify(attachmentInfoRepository).update(listOf(SAMPLE_EVIDENCE_1.copy(isTemporary = false), SAMPLE_EVIDENCE_2.copy(isTemporary = false)))
        verify(attachmentInfoRepository).findByIds(request.evidences)
    }

    @Test
    fun `should update an existing activity with new evidence in a role that not requires evidence but requires approval`() {
        // Arrange
        val role = `get role that requires approval`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with no evidence`(role, PENDING)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with evidence`(existingActivity, duration, listOf(SAMPLE_EVIDENCE_1.id))
        whenever(attachmentInfoRepository.findByIds(listOf(SAMPLE_EVIDENCE_1.id))).thenReturn(listOf(SAMPLE_EVIDENCE_1))
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration, mutableListOf(SAMPLE_EVIDENCE_1.copy(isTemporary = false)))
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(PENDING)

        // Verify
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
        verify(attachmentInfoRepository).update(listOf(SAMPLE_EVIDENCE_1.copy(isTemporary = false)))
        verify(attachmentInfoRepository).findByIds(request.evidences)
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

        val request = `get activity update request with evidence`(existingActivity, duration, SAMPLE_EVIDENCES_IDS)
        whenever(attachmentInfoRepository.findByIds(SAMPLE_EVIDENCES_IDS)).thenReturn(SAMPLE_EVIDENCES)
        doNothing().`when`(attachmentInfoRepository).update(any<List<AttachmentInfo>>())

        val updatedActivity = `get activity updated with request`(existingActivity, request, duration, SAMPLE_EVIDENCES.map { it.copy(isTemporary = false) }.toMutableList())
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(NA)

        // Verify
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)

        verify(attachmentInfoRepository).findByIds(SAMPLE_EVIDENCES_IDS)
        verify(attachmentInfoRepository).update(SAMPLE_EVIDENCES.map { it.copy(isTemporary = false) })
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

        val request = `get activity update request with evidence`(existingActivity, duration, SAMPLE_EVIDENCES_IDS)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration, SAMPLE_EVIDENCES.map { it.copy(isTemporary = false) }.toMutableList())
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)
        whenever(attachmentInfoRepository.findByIds(SAMPLE_EVIDENCES_IDS)).thenReturn(SAMPLE_EVIDENCES)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(PENDING)

        // Verify
        verify(sendPendingApproveActivityMailUseCase).send(updatedActivity.toDomain(), USER.username, LOCALE)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
        verify(attachmentInfoRepository).findByIds(request.evidences)
        verify(attachmentInfoRepository).update(SAMPLE_EVIDENCES.map { it.copy(isTemporary = false) })
    }

    @Test
    fun `should update an existing activity with the same evidence to another role that requires evidence and approval`() {
        // Arrange
        val role = `get role that requires evidence and approval`()
        val originRole = `get role that does not require evidence nor approval`()

        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing pending activity with evidence`(originRole)
        val existingEvidenceIds = existingActivity.evidences.map { it.id }
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)
        whenever(attachmentInfoRepository.findByIds(existingEvidenceIds)).thenReturn(existingActivity.evidences.map { it.copy(isTemporary = false) })

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with evidence`(existingActivity, duration, existingEvidenceIds).copy(projectRoleId = role.id)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration,
                existingActivity.evidences.map { it.copy(isTemporary = false) }.toMutableList()).copy(projectRole = role)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(PENDING)

        // Verify
        verify(sendPendingApproveActivityMailUseCase).send(updatedActivity.toDomain(), USER.username, LOCALE)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
    }

    @Test
    fun `should update an existing activity with the same evidence to another role that not requires evidence but approval`() {
        // Arrange
        val role = `get role that does not require evidence but approval`()
        val originRole = `get role that does not require evidence nor approval`()

        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing pending activity with evidence`(originRole)
        val existingEvidenceIds = existingActivity.evidences.map { it.id }
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)
        whenever(attachmentInfoRepository.findByIds(existingEvidenceIds)).thenReturn(existingActivity.evidences)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with evidence`(existingActivity, duration, existingEvidenceIds).copy(projectRoleId = role.id)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration, existingActivity.evidences).copy(projectRole = role)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(PENDING)

        // Verify
        verify(sendPendingApproveActivityMailUseCase).send(updatedActivity.toDomain(), USER.username, LOCALE)
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
        val existingAttachmentIds = existingActivity.evidences.map { it.id }
        whenever(attachmentInfoRepository.findByIds(existingAttachmentIds)).thenReturn(existingActivity.evidences.map { it.copy(isTemporary = false) })
        doNothing().`when`(attachmentInfoRepository).update(any<List<AttachmentInfo>>())

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
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
        verify(attachmentInfoRepository).findByIds(existingAttachmentIds)
        verify(attachmentInfoRepository).update(existingActivity.evidences.map { it.copy(isTemporary = true) })
    }

    @Test
    fun `should update an existing activity with evidence and remove the evidence in a role that requires evidence`() {
        // Arrange
        val role = `get role that requires evidence`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)
        val existingAttachmentIds = existingActivity.evidences.map { it.id }
        whenever(attachmentInfoRepository.findByIds(existingAttachmentIds)).thenReturn(existingActivity.evidences.map { it.copy(isTemporary = false) })
        doNothing().`when`(attachmentInfoRepository).update(any<List<AttachmentInfo>>())

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
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
        verify(attachmentInfoRepository).findByIds(existingAttachmentIds)
        verify(attachmentInfoRepository).update(existingActivity.evidences.map { it.copy(isTemporary = true) })
    }

    @Test
    fun `should update an existing activity with evidence and keep the current evidence the previous evidence`() {
        // Arrange
        val role = `get role that requires evidence`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingEvidence = createAttachmentInfoEntity().copy(isTemporary = false)
        val existingActivity = `get existing activity with evidence`(role, mutableListOf(existingEvidence))

        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)
        whenever(attachmentInfoRepository.findByIds(listOf(existingEvidence.id, SAMPLE_EVIDENCE_1.id))).thenReturn(listOf(existingEvidence, SAMPLE_EVIDENCE_1))

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with evidence`(existingActivity, duration, listOf(existingEvidence.id, SAMPLE_EVIDENCE_1.id))
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration, mutableListOf(existingEvidence, SAMPLE_EVIDENCE_1.copy(isTemporary = false) ) )
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(NA)

        // Verify
        verifyNoInteractions(sendPendingApproveActivityMailUseCase)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)

        verify(attachmentInfoRepository).update(listOf(SAMPLE_EVIDENCE_1.copy(isTemporary = false)))
    }

    @Test
    fun `should update an existing activity with evidence and mark previous evidence as temporary in a role that requires evidence and approval`() {
        // Arrange
        val role = `get role that requires evidence and approval`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing pending activity with evidence`(role)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val existingAttachmentIds =  existingActivity.evidences.map { it.id }
        whenever(attachmentInfoRepository.findByIds(existingAttachmentIds + SAMPLE_EVIDENCE_1.id)).thenReturn(existingActivity.evidences.map { it.copy(isTemporary = false) } + SAMPLE_EVIDENCE_1)

        val duration = 60
        whenever(activityCalendarService.getDurationByCountingWorkingDays(any())).thenReturn(duration)

        val request = `get activity update request with evidence`(existingActivity, duration, listOf(SAMPLE_EVIDENCE_1.id))
        whenever(attachmentInfoRepository.findByIds(listOf(SAMPLE_EVIDENCE_1.id))).thenReturn(listOf(SAMPLE_EVIDENCE_1))

        val updatedActivity = `get activity updated with request`(existingActivity, request, duration, mutableListOf(SAMPLE_EVIDENCE_1.copy(isTemporary = false)))
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(PENDING)

        // Verify
        verify(sendPendingApproveActivityMailUseCase).send(updatedActivity.toDomain(), USER.username, LOCALE)
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityCalendarService).getDurationByCountingWorkingDays(any())
        verify(activityRepository).update(updatedActivity)
        verify(attachmentInfoRepository).update(listOf(SAMPLE_EVIDENCE_1.copy(isTemporary = false)))
        verify(attachmentInfoRepository).update(existingActivity.evidences.map { it.copy(isTemporary = true) })
    }

    @Test
    fun `should not update an activity when user is not authenticated`() {
        // Arrange
        doThrow(IllegalStateException::class).whenever(userService).getAuthenticatedDomainUser()

        // Act, Assert
        assertThatThrownBy { sut.updateActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(IllegalStateException::class.java)

        // Verify
        verify(userService).getAuthenticatedDomainUser()
        verifyNoInteractions(activityRepository, activityValidator, attachmentInfoRepository, sendPendingApproveActivityMailUseCase, projectRoleRepository, activityCalendarService)
    }

    @Test
    fun `should not update an existing activity with a role that is not found`() {
        // Arrange
        whenever(projectRoleRepository.findById(any())).thenReturn(null)

        // Act, Assert
        assertThatThrownBy { sut.updateActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(ProjectRoleNotFoundException::class.java)

        // Verify
        verify(projectRoleRepository).findById(SOME_ACTIVITY_REQUEST.projectRoleId)
        verifyNoInteractions(activityRepository, activityValidator, attachmentInfoRepository, sendPendingApproveActivityMailUseCase, activityCalendarService)
        verifyNoMoreInteractions(projectRoleRepository)
    }

    @Test
    fun `should not update an existing activity when activity is not valid for updating`() {
        // Arrange
        val role = PROJECT_ROLE
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with no evidence`(role, NA)
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
        verifyNoInteractions(attachmentInfoRepository, sendPendingApproveActivityMailUseCase)
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
        verifyNoInteractions(activityValidator, attachmentInfoRepository, activityCalendarService, sendPendingApproveActivityMailUseCase)
        verifyNoMoreInteractions(projectRoleRepository, activityRepository)
    }

    private fun `get activity updated with request`(existingActivity: Activity, request: ActivityRequestDTO, duration: Int,
                                                    evidences: MutableList<AttachmentInfo> = mutableListOf()) =
            existingActivity.copy(
                    duration = duration,
                    start = request.interval.start,
                    end = request.interval.end,
                    description = request.description,
                    billable = request.billable,
                    evidences = evidences
            )


    private fun `get existing pending activity with no evidence`(role: ProjectRole) =
            Activity.emptyActivity(role).copy(
                    id = 1L,
                    userId = USER.id,
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
                    evidences = mutableListOf(createAttachmentInfoEntity()),
                    approvalState = PENDING,
                    start = LocalDate.now().atTime(8, 0),
                    end = LocalDate.now().atTime(12, 0),
                    insertDate = Date.from(Instant.now()),
                    departmentId = 1L,
            )

    private fun `get existing activity with evidence`(role: ProjectRole, evidences: MutableList<AttachmentInfo> = mutableListOf(createAttachmentInfoEntity())) =
            Activity.emptyActivity(role).copy(
                    id = 1L,
                    userId = USER.id,
                    evidences = evidences,
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
                    evidences = arrayListOf()
            )

    private fun `get activity update request with evidence`(existingActivity: Activity, duration: Int, evidenceIds: List<UUID>) =
            ActivityRequestDTO(
                    id = existingActivity.id!!,
                    start = TODAY,
                    end = TODAY.plusMinutes(duration.toLong()),
                    description = existingActivity.description + " updated",
                    billable = existingActivity.billable,
                    projectRoleId = existingActivity.projectRole.id,
                    evidences = evidenceIds
            )

    private fun `get role that requires approval`() =
            PROJECT_ROLE.copy(isApprovalRequired = true, requireEvidence = NO, timeUnit = TimeUnit.MINUTES)

    private fun `get role that does not require evidence nor approval`() =
            PROJECT_ROLE.copy(isApprovalRequired = false, requireEvidence = NO, timeUnit = TimeUnit.MINUTES)

    private fun `get role that does not require evidence but approval`() =
            PROJECT_ROLE.copy(isApprovalRequired = true, requireEvidence = NO, timeUnit = TimeUnit.MINUTES)

    private fun `get role that requires evidence`() =
            PROJECT_ROLE.copy(isApprovalRequired = false, requireEvidence = WEEKLY, timeUnit = TimeUnit.MINUTES)

    private fun `get role that requires evidence and approval`() =
            PROJECT_ROLE.copy(isApprovalRequired = true, requireEvidence = ONCE, timeUnit = TimeUnit.MINUTES)

    private fun `get existing activity with no evidence`(role: ProjectRole, approvalState: ApprovalState = NA) =
            Activity.emptyActivity(role).copy(
                    id = 1L,
                    userId = USER.id,
                    approvalState = approvalState,
                    start = LocalDate.now().atTime(8, 0),
                    end = LocalDate.now().atTime(12, 0),
                    insertDate = Date.from(Instant.now()),
                    departmentId = 1L,
            )

    private fun assertThatUpdatedActivityIsEquivalent(result: ActivityResponseDTO, request: ActivityRequestDTO) {

        val evidences = request.evidences.map { it.toString() }

        assertThat(result.interval.start).isEqualTo(request.interval.start)
        assertThat(result.interval.end).isEqualTo(request.interval.end)
        assertThat(result.description).isEqualTo(request.description)
        assertThat(result.billable).isEqualTo(request.billable)
        assertThat(result.evidences).containsExactlyInAnyOrderElementsOf(evidences)
        assertThat(result.projectRoleId).isEqualTo(request.projectRoleId)
    }

    private fun createAttachmentInfoEntity() = AttachmentInfo(
            id = UUID.randomUUID(),
            userId = 1L,
            path = "/",
            fileName = "Evidence001",
            mimeType = "application/png",
            uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
            isTemporary = false
    )


    private companion object {
        private val USER = createDomainUser()
        private val TODAY = LocalDate.now().atTime(8, 0)
        private val ORGANIZATION = Organization(1L, "Organization", listOf())
        private val PROJECT = Project(1L, "Project", open = true, billable = false,
                LocalDate.now(), null, null, projectRoles = listOf(),
                organization = ORGANIZATION
        )
        private val PROJECT_ROLE = ProjectRole(10L, "Project role", NO, PROJECT, 5000,
                1000, true, false, TimeUnit.MINUTES)
        private val LOCALE = Locale.ENGLISH

        private val SAMPLE_EVIDENCE_1 = AttachmentInfo(
                id = UUID.randomUUID(),
                userId = USER.id,
                path = "/file1.jpg",
                fileName = "file1.jpg",
                mimeType = "image/jpg",
                uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
                isTemporary = true
        )

        private val SAMPLE_EVIDENCE_2 = AttachmentInfo(
                id = UUID.randomUUID(),
                userId = USER.id,
                path = "/file2.jpg",
                fileName = "file2.jpg",
                mimeType = "image/jpg",
                uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
                isTemporary = true
        )

        private val SAMPLE_EVIDENCES = arrayListOf(SAMPLE_EVIDENCE_1, SAMPLE_EVIDENCE_2)
        private val SAMPLE_EVIDENCES_IDS = arrayListOf(SAMPLE_EVIDENCE_1.id, SAMPLE_EVIDENCE_2.id)

        private val SOME_ACTIVITY_REQUEST = ActivityRequestDTO(
                1L,
                TODAY,
                TODAY.plusMinutes(75L),
                "New activity",
                false,
                PROJECT_ROLE.id,
        )
    }

}


