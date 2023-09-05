package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createAttachmentInfoEntityWithFilenameAndMimetype
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.core.domain.AttachmentInfoId
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.binnacle.validators.ActivityValidator
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.*
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActivityDeletionUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()
    private val activityValidator = mock<ActivityValidator>()
    private val securityService = mock<SecurityService>()
    private val attachmentRepository = mock<AttachmentInfoRepository>()

    private val useCase = ActivityDeletionUseCase(activityRepository, activityValidator, securityService, attachmentRepository)

    @AfterEach
    fun resetMocks() {
        reset(activityRepository, activityValidator, attachmentRepository)
    }

    @BeforeAll
    fun setAuthMock() {
        val authenticatedUser = ClientAuthentication("id", mapOf("roles" to listOf("activity-approval")))
        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
    }

    @Test
    fun `call only the repository to delete the activity without evidences`() {
        whenever(activityRepository.findById(1L)).thenReturn(entityActivityWithoutEvidences)

        useCase.deleteActivityById(1L)

        verify(activityRepository).deleteById(1L)
        verifyNoInteractions(attachmentRepository)
    }

    @Test
    fun `call the repository and attachment service to mark the activities as temporary and delete activity`() {
        val activity = entityActivityWithEvidences
        val attachmentIds = activity.evidences.map { AttachmentInfoId(it.id) }

        whenever(activityRepository.findById(1L)).thenReturn(activity)
        whenever(attachmentRepository.findByIds(attachmentIds)).doReturn(activity.evidences.map { it.toDomain() })
        doNothing().`when`(attachmentRepository).save(any<List<AttachmentInfo>>())

        useCase.deleteActivityById(1L)

        verify(activityRepository).deleteById(1L)
        verify(attachmentRepository).findByIds(attachmentIds)
        val attachmentTemporaryTrue = activity.evidences.map { it.toDomain().copy(isTemporary = true) }
        verify(attachmentRepository).save(attachmentTemporaryTrue)
    }

    @Test
    fun `throw not found exception from the validator`() {
        doThrow(ActivityNotFoundException(1L)).whenever(activityRepository).findById(1L)

        assertThrows<ActivityNotFoundException> {
            useCase.deleteActivityById(1L)
        }
    }

    private companion object {
        val ORGANIZATION = Organization(1L, "Dummy Organization", listOf())
        val PROJECT = Project(
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
        val PROJECT_ROLE =
                ProjectRole(
                        10L, "Dummy Project role", RequireEvidence.NO,
                        PROJECT, 0, 0, true, false, TimeUnit.MINUTES
                )
        private val TODAY = LocalDateTime.now()

        private val activity = com.autentia.tnt.binnacle.core.domain.Activity.of(
                1L,
                TimeInterval.of(TODAY, TODAY.plusMinutes(75L)),
                75,
                "New activity",
                PROJECT_ROLE.toDomain(),
                1L,
                false,
                null,
                LocalDateTime.now(),
                ApprovalState.NA,
                arrayListOf()
        )

        private val entityActivityWithoutEvidences = Activity.of(activity, PROJECT_ROLE)

        private val entityActivityWithEvidences = Activity.of(activity, PROJECT_ROLE, mutableListOf(
                createAttachmentInfoEntity()))

        private fun createAttachmentInfoEntity() = createAttachmentInfoEntityWithFilenameAndMimetype("sample.jpg", "image/jpg").copy(isTemporary = false)

    }

}
