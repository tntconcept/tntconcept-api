package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createAttachmentInfoEntityWithFilenameAndMimetype
import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.config.createProject
import com.autentia.tnt.binnacle.converters.ActivityEvidenceResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@TestInstance(PER_CLASS)
internal class ActivityCreationUseCaseTest {
    private val user = createDomainUser()
    private val activityService = mock<ActivityService>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityRepository = mock<ActivityRepository>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val userService = mock<UserService>()
    private val attachmentInfoRepository = mock<AttachmentInfoRepository>()
    private val dateService = mock<DateService>()
    private val activityRequestBodyConverter = ActivityRequestBodyConverter()
    private val activityResponseConverter = ActivityResponseConverter(
            ActivityIntervalResponseConverter(),
            ActivityEvidenceResponseConverter()
    )
    private val activityValidator = mock<ActivityValidator>()
    private val sendPendingApproveActivityMailUseCase = mock<SendPendingApproveActivityMailUseCase>()

    private val sut = ActivityCreationUseCase(
            projectRoleRepository,
            activityRepository,
            attachmentInfoRepository,
            activityCalendarService,
            userService,
            activityValidator,
            activityRequestBodyConverter,
            activityResponseConverter,
            sendPendingApproveActivityMailUseCase,
            dateService)

    @Nested
    @TestInstance(PER_CLASS)
    inner class ActivityCreationWithValidationFailures {
        @AfterEach
        fun resetMocks() {
            reset(
                    activityService,
                    projectRoleRepository,
                    activityRepository,
                    attachmentInfoRepository,
                    activityCalendarService,
                    userService,
                    sendPendingApproveActivityMailUseCase,
                    activityValidator
            )
        }

        @BeforeEach
        fun setup() {
            whenever(userService.getAuthenticatedDomainUser()).thenReturn(user)
            whenever(dateService.getDateNow()).doReturn(TIME_NOW)
        }

        @Test
        fun `create activity with a nonexistent projectRol throws an exception`() {
            whenever(projectRoleRepository.findById(PROJECT_ROLE_NO_APPROVAL.id)).thenReturn(null)

            assertThrows<ProjectRoleNotFoundException> {
                sut.createActivity(ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO, Locale.ENGLISH)
            }

            verifyNoInteractions(activityRepository, sendPendingApproveActivityMailUseCase)
        }


        @ParameterizedTest
        @MethodSource("com.autentia.tnt.binnacle.usecases.ActivityCreationUseCaseTest#validationExceptions")
        fun `activity is not created if validator throws a binnacle exception`(binnacleException: BinnacleException) {
            val someRequestDTO = ActivityRequestDTO(
                    null,
                    TIME_NOW,
                    TIME_NOW.plusMinutes(75L),
                    "New activity wit",
                    false,
                    PROJECT_ROLE_NO_APPROVAL.id,
                    EVIDENCES
            )

            whenever(projectRoleRepository.findById(PROJECT_ROLE_NO_APPROVAL.id)).thenReturn(PROJECT_ROLE_NO_APPROVAL)
            whenever(activityValidator.checkActivityIsValidForCreation(any(), any())).thenThrow(binnacleException)

            assertThrows<BinnacleException> {
                sut.createActivity(someRequestDTO, Locale.ENGLISH)
            }

            verifyNoInteractions(activityRepository, sendPendingApproveActivityMailUseCase)
        }

    }

    @Nested
    @TestInstance(PER_CLASS)
    inner class ActivityCreationValidActivity {
        @AfterEach
        fun resetMocks() {
            reset(
                    activityService,
                    projectRoleRepository,
                    activityRepository,
                    attachmentInfoRepository,
                    activityCalendarService,
                    userService,
                    sendPendingApproveActivityMailUseCase,
                    activityValidator
            )
        }

        @BeforeEach
        fun fixedSetup() {
            whenever(dateService.getDateNow()).doReturn(TIME_NOW)
            whenever(userService.getAuthenticatedDomainUser()).thenReturn(user)
            doNothing().`when`(activityValidator).checkActivityIsValidForCreation(any(), any())
        }

        @Test
        fun `created activity with no approval required and with no evidence`() {
            // Given
            val request = `an activity with no approval and no evidences`()
            val activityEntity = createActivity(userId = user.id)

            whenever(projectRoleRepository.findById(PROJECT_ROLE_NO_APPROVAL.id)).thenReturn(PROJECT_ROLE_NO_APPROVAL)
            whenever(activityRepository.save(any())).thenReturn(activityEntity)

            // When
            val activityCreated = sut.createActivity(request, Locale.ENGLISH)

            // Then
            val expectedResponseDTO = createActivityResponseDTO(userId = user.id)
            Assertions.assertThat(activityCreated)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResponseDTO)

            // Verify
            verifyNoInteractions(attachmentInfoRepository, sendPendingApproveActivityMailUseCase)
        }

        @Test
        fun `create activity with interval of natural days`() {
            // Given
            val start = LocalDate.now().atTime(LocalTime.MIN)
            val end = LocalDate.now().plusDays(2).atTime(23, 59, 59)
            val projectRole = NATURAL_DAYS_PROJECT_ROLE

            val request = ActivityRequestDTO(
                    interval = TimeIntervalRequestDTO(start = start, end = end),
                    description = "NATURAL DAYS ACTIVITY",
                    billable = true,
                    projectRoleId = projectRole.id,
            )

            val activityEntity = createActivity(evidences = mutableListOf(), userId = user.id, projectRole = projectRole, start = start, end = end)

            whenever(activityRepository.save(any())).thenReturn(activityEntity)
            whenever(projectRoleRepository.findById(any())).thenReturn(projectRole)

            // When
            val result = sut.createActivity(request, Locale.ENGLISH)

            // Then
            val expectedDays = 3
            val expectedResponseDTO = createActivityResponseDTO(userId = user.id, start = start, end = end, duration = expectedDays, timeUnit = TimeUnit.NATURAL_DAYS)
            Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(expectedResponseDTO)

            // Verify
            verifyNoInteractions(attachmentInfoRepository, sendPendingApproveActivityMailUseCase)
        }

        @Test
        fun `create activity with interval of workable days`() {
            // Given
            val start = LocalDate.now().atTime(LocalTime.MIN)
            val end = LocalDate.now().plusDays(2).atTime(23, 59, 59)
            val activityEntity = createActivity(evidences = mutableListOf(), userId = user.id, projectRole = WORKABLE_DAYS_PROJECT_ROLE, start = start, end = end, duration = 960)
            val activityDomain = activityEntity.toDomain()
            val request = `an activity with no approval and no evidences`()

            whenever(activityRepository.save(any())).thenReturn(activityEntity)
            whenever(projectRoleRepository.findById(any())).thenReturn(
                    ProjectRole.of(
                            activityDomain.projectRole,
                            createProject()
                    )
            )

            // When
            val result = sut.createActivity(request, Locale.ENGLISH)

            // Then
            val expectedResponseDTO = createActivityResponseDTO(userId = user.id, start = start, end = end, duration = 2, timeUnit = TimeUnit.DAYS)
            Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(expectedResponseDTO)

            // Verify
            verifyNoInteractions(attachmentInfoRepository, sendPendingApproveActivityMailUseCase)
        }

        @Test
        fun `created activity with evidence and no approval required for project role, none mail is sent`() {
            // Given
            val attachmentInfo = createAttachmentInfoEntity().copy(isTemporary = true)
            val projectRole = PROJECT_ROLE_NO_APPROVAL

            val request = ActivityRequestDTO(
                    null,
                    TIME_NOW,
                    TIME_NOW.plusMinutes(75L),
                    "New activity wit",
                    false,
                    projectRole.id,
                    listOf(attachmentInfo.id)
            )

            val activityEntity = createActivity(
                    userId = user.id,
                    start = request.interval.start,
                    end = request.interval.end,
                    description = request.description,
                    billable = request.billable,
                    projectRole = projectRole,
                    evidences = mutableListOf(attachmentInfo))

            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(projectRole)
            whenever(activityRepository.save(any())).thenReturn(activityEntity)
            whenever(attachmentInfoRepository.findByIds(request.evidences)).thenReturn(listOf(attachmentInfo))
            doNothing().`when`(attachmentInfoRepository).update(any<List<AttachmentInfo>>())

            // When
            val activityCreated = sut.createActivity(request, Locale.ENGLISH)

            // Then
            val expectedResponseDTO = createActivityResponseDTO(userId = user.id,
                    start = request.interval.start,
                    end = request.interval.end,
                    description = request.description,
                    billable = request.billable,
                    projectRoleId = projectRole.id,
                    evidences = mutableListOf(attachmentInfo.id.toString()))

            Assertions.assertThat(activityCreated).usingRecursiveComparison().isEqualTo(expectedResponseDTO)

            // Verify
            verifyNoInteractions(sendPendingApproveActivityMailUseCase)
            verify(attachmentInfoRepository).update(listOf(attachmentInfo.copy(isTemporary = false)))
        }

        @Test
        fun `created activity with evidence and approval required with evidence, pending approval mail is sent`() {
            // Given
            val projectRoleRequireEvidence = `get role that requires approval and evidence`()
            val attachmentInfo = createAttachmentInfoEntity().copy(isTemporary = true)

            val activityCreateRequest = ActivityRequestDTO(
                    null,
                    TIME_NOW,
                    TIME_NOW.plusMinutes(75L),
                    "New activity wit",
                    false,
                    projectRoleRequireEvidence.id,
                    listOf(attachmentInfo.id)
            )

            val activityEntity = createActivity(description = activityCreateRequest.description, userId = user.id,
                    projectRole = projectRoleRequireEvidence, evidences = mutableListOf(attachmentInfo),
                    approvalState = ApprovalState.PENDING, billable = false,
                    start = activityCreateRequest.interval.start, end = activityCreateRequest.interval.end)

            whenever(projectRoleRepository.findById(projectRoleRequireEvidence.id)).thenReturn(projectRoleRequireEvidence)
            whenever(activityRepository.save(any())).thenReturn(activityEntity)
            whenever(attachmentInfoRepository.findByIds(activityCreateRequest.evidences))
                    .thenReturn(listOf(attachmentInfo))
            doNothing().`when`(attachmentInfoRepository).update(any<List<AttachmentInfo>>())

            // When
            val activityCreated = sut.createActivity(activityCreateRequest, Locale.ENGLISH)

            // Then
            val expectedResponseDTO = createActivityResponseDTO(
                    userId = user.id,
                    evidences = arrayListOf(attachmentInfo.id.toString()),
                    description = activityEntity.description)
                    .copy(approval = ApprovalDTO(state = ApprovalState.PENDING, canBeApproved = true))

            Assertions.assertThat(activityCreated)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResponseDTO)

            // Verify
            verify(sendPendingApproveActivityMailUseCase).send(activityEntity.toDomain(), user.username, Locale.ENGLISH)
            verify(attachmentInfoRepository).update(listOf(attachmentInfo.copy(isTemporary = false)))
        }

        @Test
        fun `created activity without evidence and approval required without evidence, pending approval mail is sent`() {
            // Given
            val projectRole = `get role that requires approval but no evidence`()
            val activityEntity = `get activity entity without evidence`(projectRole)
            val activityDomain = activityEntity.toDomain()
            val activityCreateRequest = ACTIVITY_WITH_NO_EVIDENCE_DTO.copy(projectRoleId = projectRole.id)

            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(projectRole)
            whenever(activityRepository.save(any())).thenReturn(activityEntity)

            // When
            val activityCreated = sut.createActivity(activityCreateRequest, Locale.ENGLISH)

            // Then
            val expectedResponseDTO = createActivityResponseDTO(userId = user.id, evidences = arrayListOf(), description = activityEntity.description)
                    .copy(approval = ApprovalDTO(state = ApprovalState.PENDING, canBeApproved = true))

            Assertions.assertThat(activityCreated)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResponseDTO)

            // Verify
            verify(sendPendingApproveActivityMailUseCase).send(activityDomain, user.username, Locale.ENGLISH)
            verifyNoInteractions(attachmentInfoRepository)
        }

        @Test
        fun `created activity without evidence to role with approval required with evidence, no mail is sent`() {
            // Given
            val projectRole = `get role that requires approval and evidence`()
            val activityEntity = `get activity entity without evidence`(projectRole)

            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(projectRole)
            whenever(activityRepository.save(any())).thenReturn(activityEntity)

            // When
            val activityCreateRequest = ACTIVITY_WITH_NO_EVIDENCE_DTO.copy(projectRoleId = projectRole.id)

            val activityCreated = sut.createActivity(activityCreateRequest, Locale.ENGLISH)

            // Then
            val expectedResponseDTO = createActivityResponseDTO(userId = user.id, evidences = arrayListOf(), description = activityEntity.description)
                    .copy(approval = ApprovalDTO(state = ApprovalState.PENDING, canBeApproved = false))

            Assertions.assertThat(activityCreated)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResponseDTO)

            // Verify
            verifyNoInteractions(sendPendingApproveActivityMailUseCase, attachmentInfoRepository)
        }


        @Test
        fun `created activity with evidence and approval required without evidence, pending approval mail is sent`() {
            // Given
            val projectRoleRequireEvidence = `get role that requires approval but no evidence`()
            val attachmentInfo = createAttachmentInfoEntity().copy(isTemporary = true)
            val activityCreateRequest = ActivityRequestDTO(
                    null,
                    TIME_NOW,
                    TIME_NOW.plusMinutes(75L),
                    "New activity wit",
                    false,
                    projectRoleRequireEvidence.id,
                    listOf(attachmentInfo.id)
            )
            val activityEntity = createActivity(description = activityCreateRequest.description, userId = user.id,
                    projectRole = projectRoleRequireEvidence, evidences = mutableListOf(attachmentInfo),
                    approvalState = ApprovalState.PENDING, billable = false,
                    start = activityCreateRequest.interval.start, end = activityCreateRequest.interval.end)

            whenever(projectRoleRepository.findById(projectRoleRequireEvidence.id)).thenReturn(projectRoleRequireEvidence)
            whenever(activityRepository.save(any())).thenReturn(activityEntity)
            whenever(attachmentInfoRepository.findByIds(activityCreateRequest.evidences)).thenReturn(listOf(attachmentInfo))
            doNothing().`when`(attachmentInfoRepository).update(any<List<AttachmentInfo>>())

            // When
            val activityCreated = sut.createActivity(activityCreateRequest, Locale.ENGLISH)

            // Then
            val expectedResponseDTO = createActivityResponseDTO(userId = user.id,
                    evidences = arrayListOf(attachmentInfo.id.toString()), description = activityEntity.description)
                    .copy(approval = ApprovalDTO(state = ApprovalState.PENDING, canBeApproved = true))

            Assertions.assertThat(activityCreated)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResponseDTO)

            // Verify
            verify(sendPendingApproveActivityMailUseCase).send(activityEntity.toDomain(), user.username, Locale.ENGLISH)
            verify(attachmentInfoRepository).update(listOf(attachmentInfo.copy(isTemporary = false)))
        }

        private fun `an activity with no approval and no evidences`() = ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO.copy(evidences = emptyList())

    }

    private fun `get activity entity without evidence`(projectRole: ProjectRole) =
            createActivity(description = "This is an activity without evidence", userId = user.id,
                    projectRole = projectRole, approvalState = ApprovalState.PENDING)

    private fun `get role that requires approval and evidence`() =
            PROJECT_ROLE_APPROVAL.copy(requireEvidence = RequireEvidence.ONCE, isApprovalRequired = true)

    private fun `get role that requires approval but no evidence`() =
            PROJECT_ROLE_APPROVAL.copy(requireEvidence = RequireEvidence.NO, isApprovalRequired = true)

    private fun createAttachmentInfoEntity() = createAttachmentInfoEntityWithFilenameAndMimetype("sample.jpg", "image/jpg")

    private companion object {
        private val TIME_NOW = LocalDateTime.now()

        private val ATTACHMENT_ID_1 = UUID.randomUUID()

        private val ATTACHMENT_ID_2 = UUID.randomUUID()

        private val TODAY = Date()

        private val ORGANIZATION = Organization(1L, "Dummy Organization", 1, listOf())

        private val PROJECT = Project(
                1L,
                "Dummy Project",
                open = true,
                billable = false,
                LocalDate.now(),
                null,
                null,
                ORGANIZATION,
                listOf()
        )
        private val PROJECT_ROLE_NO_APPROVAL =
                ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, 0, true, false, TimeUnit.MINUTES)

        private val PROJECT_ROLE_APPROVAL =
                ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, 0, true, true, TimeUnit.MINUTES)

        private val NATURAL_DAYS_PROJECT_ROLE =
                ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, 0, true, true, TimeUnit.NATURAL_DAYS)

        private val WORKABLE_DAYS_PROJECT_ROLE =
                ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, 0, true, true, TimeUnit.DAYS)

        private val EVIDENCES = arrayListOf(ATTACHMENT_ID_1, ATTACHMENT_ID_2)

        private val ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO = ActivityRequestDTO(
                null,
                TIME_NOW,
                TIME_NOW.plusMinutes(75L),
                "New activity",
                false,
                PROJECT_ROLE_NO_APPROVAL.id,
                EVIDENCES,
        )


        private val ACTIVITY_WITH_NO_EVIDENCE_DTO = ActivityRequestDTO(
                null,
                TIME_NOW,
                TIME_NOW.plusMinutes(75L),
                "New activity wit",
                false,
                PROJECT_ROLE_NO_APPROVAL.id,
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
                evidences: MutableList<AttachmentInfo> = arrayListOf(),
                projectRole: ProjectRole = PROJECT_ROLE_NO_APPROVAL,
                approvalState: ApprovalState = ApprovalState.NA,
                insertDate: Date = TODAY,
        ): Activity =
                Activity(
                        id = id,
                        userId = userId,
                        description = description,
                        start = start,
                        end = end,
                        duration = duration,
                        billable = billable,
                        evidences = evidences,
                        projectRole = projectRole,
                        approvalState = approvalState,
                        insertDate = insertDate
                )

        private fun createActivityResponseDTO(
                id: Long = 1L,
                userId: Long = 0L,
                description: String = generateLargeDescription("New activity").substring(0, 2048),
                start: LocalDateTime = TIME_NOW,
                end: LocalDateTime = TIME_NOW.plusMinutes(75L),
                duration: Int = 75,
                billable: Boolean = false,
                evidences: List<String> = arrayListOf(),
                projectRoleId: Long = 10L,
                approvalState: ApprovalState = ApprovalState.NA,
                timeUnit: TimeUnit = PROJECT_ROLE_NO_APPROVAL.timeUnit,
        ): ActivityResponseDTO =
                ActivityResponseDTO(
                        billable,
                        description,
                        evidences,
                        id,
                        projectRoleId,
                        IntervalResponseDTO(start, end, duration, timeUnit),
                        userId,
                        ApprovalDTO(state = approvalState)
                )


        @JvmStatic
        fun validationExceptions() = listOf(
                Arguments.of(ProjectClosedException()),
                Arguments.of(ActivityPeriodClosedException()),
                Arguments.of(ProjectBlockedException(LocalDate.now())),
                Arguments.of(ActivityBeforeProjectCreationDateException()),
                Arguments.of(OverlapsAnotherTimeException()),
                Arguments.of(ActivityBeforeHiringDateException()),
                Arguments.of(AttachmentNotFoundException()),
                Arguments.of(ActivityPeriodNotValidException()),
                Arguments.of(MaxTimePerActivityRoleException(1, 1, TimeUnit.MINUTES, 2022)),
                Arguments.of(MaxTimePerRoleException(1.0, 1.0, TimeUnit.MINUTES, 2022))
        )
    }


}
