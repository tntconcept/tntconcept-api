package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.config.createProject
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@TestInstance(PER_CLASS)
internal class ActivityCreationUseCaseTest {

    private val user = createDomainUser()

    private val projectService = mock<ProjectService>()
    private val activityService = mock<ActivityService>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityRepository = mock<ActivityRepository>();
    private val activityEvidenceService = mock<ActivityEvidenceService>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val userService = mock<UserService>()

    private val activityValidator =
        ActivityValidator(
            activityService,
            activityCalendarService,
            projectService)

    private val pendingApproveActivityMailService = mock<PendingApproveActivityMailService>()


    private val activityCreationUseCase = ActivityCreationUseCase(
        projectRoleRepository,
        activityRepository,
        activityEvidenceService,
        activityCalendarService,
        userService,
        activityValidator,
        ActivityRequestBodyConverter(),
        ActivityResponseConverter(
            ActivityIntervalResponseConverter()
        ),
        pendingApproveActivityMailService)


    @AfterEach
    fun resetMocks() {
        reset(
            projectService,
            activityService,
            projectRoleRepository,
            activityRepository,
            activityEvidenceService,
            activityCalendarService,
            userService,
            pendingApproveActivityMailService,
        )
    }

    @Test
    fun `create activity with a nonexistent projectRol throws an exception`() {

        whenever(projectRoleRepository.findById(PROJECT_ROLE_NO_APPROVAL.id)).thenReturn(null)

        assertThrows<ProjectRoleNotFoundException> {
            activityCreationUseCase.createActivity(ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO, Locale.ENGLISH)
        }
    }


    private fun exceptionProvider() = arrayOf(
        arrayOf(
            "ActivityWithEvidenceButNotAttachedException",
            ACTIVITY_WITH_EVIDENCE_BUT_NOT_ATTACHED_DTO
        ),
        arrayOf(
            "ActivityWithAttachedEvidenceButNotTrueException",
            ACTIVITY_WITH_ATTACHED_EVIDENCE_BUT_NOT_AS_TRUE
        )
    )

    @ParameterizedTest
    @MethodSource("exceptionProvider")
    fun `create activity with incomplete evidence information throws an exception`(
        testDescription: String,
        activityRequest: ActivityRequestDTO,
    ) {

        val activityEntity = createActivity(userId = user.id)
        val activityDomain = activityEntity.toDomain();


        whenever(userService.getAuthenticatedDomainUser()).thenReturn(user)
        whenever(projectRoleRepository.findById(PROJECT_ROLE_NO_APPROVAL.id)).thenReturn(PROJECT_ROLE_NO_APPROVAL)
        whenever(projectService.findById(activityEntity.projectRole.project.id)).thenReturn(activityDomain.projectRole.project)

        assertThrows<NoEvidenceInActivityException> {
            activityCreationUseCase.createActivity(activityRequest, Locale.ENGLISH)
        }

    }

    @Test
    fun `created activity with no approval required and with no evidence`() {

        val activityEntity = createActivity(userId = user.id)
        val activityDomain = activityEntity.toDomain()

        whenever(userService.getAuthenticatedDomainUser()).thenReturn(user)
        whenever(projectRoleRepository.findById(PROJECT_ROLE_NO_APPROVAL.id)).thenReturn(PROJECT_ROLE_NO_APPROVAL)
        whenever(projectService.findById(activityEntity.projectRole.project.id)).thenReturn(activityDomain.projectRole.project)
        whenever(activityRepository.save(any())).thenReturn(activityEntity);

        val activityCreated = activityCreationUseCase.createActivity(ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO, Locale.ENGLISH)

        verify(activityEvidenceService, times(1)).storeActivityEvidence(
            eq(activityEntity.id!!), any(), eq(activityEntity.insertDate!!)
        )

        verify(pendingApproveActivityMailService, times(0)).sendApprovalActivityMail(
            activityDomain,
            user.username,
            Locale.ENGLISH)

        val expectedResponseDTO = createActivityResponseDTO(userId = user.id)
        assertEquals(expectedResponseDTO, activityCreated)
    }

    @Test
    fun `create activity with interval of natural days`() {

        val start = LocalDate.now().atTime(LocalTime.MIN)
        val end = LocalDate.now().plusDays(2).atTime(23, 59, 59)
        val activityEntity = createActivity(userId = user.id, projectRole = NATURAL_DAYS_PROJECT_ROLE, start = start, end = end)
        val activityDomain = activityEntity.toDomain()

        whenever(userService.getAuthenticatedDomainUser()).thenReturn(user)
        whenever(activityRepository.save(any())).thenReturn(activityEntity)
        whenever(projectService.findById(activityEntity.projectRole.project.id)).thenReturn(activityDomain.projectRole.project)
        whenever(projectRoleRepository.findById(any())).thenReturn(
            ProjectRole.of(
                activityDomain.projectRole,
                createProject()
            )
        )

        val result = activityCreationUseCase.createActivity(ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO, Locale.ENGLISH)

        val expectedResponseDTO = createActivityResponseDTO(userId = user.id, start = start, end = end, duration = 3, timeUnit = TimeUnit.NATURAL_DAYS)
        assertEquals(expectedResponseDTO, result)
    }

    @Test
    fun `create activity with interval of workable days`() {

        val start = LocalDate.now().atTime(LocalTime.MIN)
        val end = LocalDate.now().plusDays(2).atTime(23, 59, 59)
        val activityEntity = createActivity(userId = user.id, projectRole = WORKABLE_DAYS_PROJECT_ROLE, start = start, end = end, duration = 960)
        val activityDomain = activityEntity.toDomain()

        whenever(userService.getAuthenticatedDomainUser()).thenReturn(user)
        whenever(activityRepository.save(any())).thenReturn(activityEntity)
        whenever(projectService.findById(activityEntity.projectRole.project.id)).thenReturn(activityDomain.projectRole.project)
        whenever(projectRoleRepository.findById(any())).thenReturn(
            ProjectRole.of(
                activityDomain.projectRole,
                createProject()
            )
        )

        val result = activityCreationUseCase.createActivity(ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO, Locale.ENGLISH)

        val expectedResponseDTO = createActivityResponseDTO(userId = user.id, start = start, end = end, duration = 2, timeUnit = TimeUnit.DAYS)
        assertEquals(expectedResponseDTO, result)
    }

    @Test
    fun `created activity with with evidence and no approval required for project role, none mail is sent`() {

        val activityEntity = createActivity(userId = user.id)
        val activityDomain = activityEntity.toDomain()

        whenever(userService.getAuthenticatedDomainUser()).thenReturn(user)
        whenever(projectRoleRepository.findById(PROJECT_ROLE_NO_APPROVAL.id)).thenReturn(PROJECT_ROLE_NO_APPROVAL)
        whenever(projectService.findById(activityEntity.projectRole.project.id)).thenReturn(activityDomain.projectRole.project)
        whenever(activityRepository.save(any())).thenReturn(activityEntity);

        val activityCreated = activityCreationUseCase.createActivity(ACTIVITY_WITH_EVIDENCE_DTO, Locale.ENGLISH)

        verify(activityEvidenceService, times(1)).storeActivityEvidence(
            eq(activityEntity.id!!), any(), eq(activityEntity.insertDate!!)
        )

        verify(pendingApproveActivityMailService, times(0)).sendApprovalActivityMail(
            activityDomain,
            user.username,
            Locale.ENGLISH)

        val expectedResponseDTO = createActivityResponseDTO(userId = user.id)
        assertEquals(expectedResponseDTO, activityCreated)
    }

    @Test
    fun `created activity with evidence and approval required, pending approval mail is sent`() {

        val activityEntity = createActivity(userId = user.id, projectRole = PROJECT_ROLE_APPROVAL)
        val activityDomain = activityEntity.toDomain()

        whenever(userService.getAuthenticatedDomainUser()).thenReturn(user)
        whenever(projectRoleRepository.findById(PROJECT_ROLE_APPROVAL.id)).thenReturn(PROJECT_ROLE_APPROVAL)
        whenever(projectService.findById(activityEntity.projectRole.project.id)).thenReturn(activityDomain.projectRole.project)
        whenever(activityRepository.save(any())).thenReturn(activityEntity);

        val activityCreated = activityCreationUseCase.createActivity(ACTIVITY_WITH_EVIDENCE_DTO, Locale.ENGLISH)

        verify(activityEvidenceService, times(1)).storeActivityEvidence(
            eq(activityEntity.id!!), any(), eq(activityEntity.insertDate!!)
        )

        verify(pendingApproveActivityMailService, times(1)).sendApprovalActivityMail(
            activityDomain,
            user.username,
            Locale.ENGLISH)

        val expectedResponseDTO = createActivityResponseDTO(userId = user.id)
        assertEquals(expectedResponseDTO, activityCreated)

    }


    private companion object {
        private val TIME_NOW = LocalDateTime.now()

        private val TODAY = Date()

        private val ORGANIZATION = Organization(1L, "Dummy Organization", listOf())

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



        private val EVIDENCE = EvidenceDTO.from("data:application/pdf;base64,SGVsbG8gV29ybGQh")

        private val ACTIVITY_NO_APPROVAL_REQUEST_BODY_DTO = ActivityRequestDTO(
            null,
            TIME_NOW,
            TIME_NOW.plusMinutes(75L),
            "New activity",
            false,
            PROJECT_ROLE_NO_APPROVAL.id,
            true,
            EVIDENCE,
        )

        private val ACTIVITY_WITH_EVIDENCE_BUT_NOT_ATTACHED_DTO = ActivityRequestDTO(
            null,
            TIME_NOW,
            TIME_NOW.plusMinutes(75L),
            "New activity evidence empty",
            false,
            PROJECT_ROLE_NO_APPROVAL.id,
            true,
            null,
        )

        private val ACTIVITY_WITH_ATTACHED_EVIDENCE_BUT_NOT_AS_TRUE = ActivityRequestDTO(
            null,
            TIME_NOW,
            TIME_NOW.plusMinutes(75L),
            "New activity evidence empty",
            false,
            PROJECT_ROLE_NO_APPROVAL.id,
            false,
            EVIDENCE,
        )

        private val ACTIVITY_WITH_EVIDENCE_DTO = ActivityRequestDTO(
            null,
            TIME_NOW,
            TIME_NOW.plusMinutes(75L),
            "New activity wit",
            false,
            PROJECT_ROLE_NO_APPROVAL.id,
            true,
            EVIDENCE
        )

        private val ACTIVITY_APPROVAL_REQUEST_BODY_DTO = ActivityRequestDTO(
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
                hasEvidences = hasEvidences,
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
            hasEvidences: Boolean = false,
            projectRoleId: Long = 10L,
            approvalState: ApprovalState = ApprovalState.NA,
            timeUnit: TimeUnit = PROJECT_ROLE_NO_APPROVAL.timeUnit,
        ): ActivityResponseDTO =
            ActivityResponseDTO(
                billable,
                description,
                hasEvidences,
                id,
                projectRoleId,
                IntervalResponseDTO(start, end, duration, timeUnit),
                userId,
                approvalState
            )

    }

}
