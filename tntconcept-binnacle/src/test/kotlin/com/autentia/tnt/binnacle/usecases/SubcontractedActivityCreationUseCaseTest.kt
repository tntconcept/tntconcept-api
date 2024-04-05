package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.ActivityBeforeProjectCreationDateException
import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.validators.SubcontractedActivityValidator
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


class SubcontractedActivityCreationUseCaseTest {

    private val projectRepository = mock<ProjectRepository>()
    private val activityService = mock<ActivityService>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityRepository = mock<ActivityRepository>()
    private val activityEvidenceService = mock<ActivityEvidenceService>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val userRepository = mock<UserRepository>()
    private val securityService: SecurityService = mock()
    private val appProperties = AppProperties()

    private val subcontractedActivityValidator =
            SubcontractedActivityValidator(
                    activityService,
                    activityCalendarService,
                    projectRepository)

    //subject under test
    private val sut = SubcontractedActivityCreationUseCase(
            projectRoleRepository,
            activityRepository,
            activityEvidenceService,
            subcontractedActivityValidator,
            ActivityRequestBodyConverter(),
            ActivityResponseConverter(
                    ActivityIntervalResponseConverter()
            ),
            userRepository,
            securityService,
            appProperties
            )


    private fun authenticate(){
        whenever(securityService.authentication).thenReturn(Optional.of(AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE))
    }
    private fun generateSubcontractedUser():User{
        appProperties.binnacle.subcontractedUser.username="subcontracted"
        whenever(userRepository.findByUsername("subcontracted")).thenReturn(USER_ENTITIES_SUBCONTRACTED)
        return USER_ENTITIES_SUBCONTRACTED
    }

    @Test
    fun `create activity with a nonexistent projectRol throws an exception`() {
        authenticate()
        generateSubcontractedUser()

        //whenever(appProperties.binnacle.subcontractedUser.username).thenReturn("subcontracted")
        assertThrows<ProjectRoleNotFoundException> {
            sut.createSubcontractedActivity(SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO, Locale.ENGLISH)
        }
    }

    @ParameterizedTest
    @MethodSource("exceptionProvider")
    fun `create activity with incomplete evidence information throws an exception`(
            testDescription: String,
            subcontractedActivityRequest: SubcontractedActivityRequestDTO,
    ) {
        authenticate()
        val subcontractedUser = generateSubcontractedUser()
        val activityEntity = createActivity(userId = subcontractedUser.id)

        whenever(projectRoleRepository.findById(PROJECT_ROLE.id)).thenReturn(PROJECT_ROLE)
        whenever(projectRepository.findById(activityEntity.projectRole.project.id)).thenReturn(Optional.of(activityEntity.projectRole.project))

        assertThrows<NoEvidenceInActivityException> {
            sut.createSubcontractedActivity(subcontractedActivityRequest, Locale.ENGLISH)
        }

    }

    @Test
    fun `create activity before project creation date throws an exception`() {
        authenticate()
        val subcontractedUser = generateSubcontractedUser()

        val activityEntity = createActivity(userId = subcontractedUser.id)
        whenever(projectRoleRepository.findById(PROJECT_ROLE.id)).thenReturn(PROJECT_ROLE)
        whenever(projectRepository.findById(activityEntity.projectRole.project.id)).thenReturn(Optional.of(activityEntity.projectRole.project))

        assertThrows<ActivityBeforeProjectCreationDateException> {
            sut.createSubcontractedActivity(SUBCONTRACTED_ACTIVITY_WITH_DATE_BEFORE_CREATION_PROJECT_DATE, Locale.ENGLISH)
        }

    }


    @Test
    fun `created activity with no evidence`() {

        authenticate()
        val subcontractedUser = generateSubcontractedUser()
        val activityEntity = createActivity(userId = subcontractedUser.id)

        whenever(projectRoleRepository.findById(PROJECT_ROLE.id)).thenReturn(PROJECT_ROLE)
        whenever(projectRepository.findById(activityEntity.projectRole.project.id)).thenReturn(Optional.of(activityEntity.projectRole.project))
        whenever(activityRepository.save(any())).thenReturn(activityEntity)

        val activityCreated = sut.createSubcontractedActivity(SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO, Locale.ENGLISH)

        verify(activityEvidenceService, times(1)).storeActivityEvidence(
                eq(activityEntity.id!!), any(), eq(activityEntity.insertDate!!)
        )


        val expectedResponseDTO = createActivityResponseDTO(userId = subcontractedUser.id)
        Assertions.assertThat(activityCreated)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseDTO)
    }


    @Test
    fun `created activity with evidence`() {

        authenticate()
        val subcontractedUser = generateSubcontractedUser()

        val activityEntity = createActivity(userId = subcontractedUser.id)

        whenever(projectRoleRepository.findById(PROJECT_ROLE.id)).thenReturn(PROJECT_ROLE)
        whenever(projectRepository.findById(activityEntity.projectRole.project.id)).thenReturn(Optional.of(activityEntity.projectRole.project))
        whenever(activityRepository.save(any())).thenReturn(activityEntity)

        val activityCreated = sut.createSubcontractedActivity(SUBCONTRACTED_ACTIVITY_WITH_EVIDENCE_DTO, Locale.ENGLISH)

        verify(activityEvidenceService, times(1)).storeActivityEvidence(
                eq(activityEntity.id!!), any(), eq(activityEntity.insertDate!!)
        )


        val expectedResponseDTO = createActivityResponseDTO(userId = subcontractedUser.id)

        Assertions.assertThat(activityCreated)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseDTO)
    }

    @Test
    fun `created activity without evidence to role with evidence required, no mail is sent`() {
        authenticate()
        val subcontractedUser = generateSubcontractedUser()

        // Given
        val projectRole = `get role that requires evidence`()
        val activityEntity = `get activity entity without evidence`(projectRole)
        whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(projectRole)
        whenever(projectRepository.findById(activityEntity.projectRole.project.id)).thenReturn(Optional.of(activityEntity.projectRole.project))
        whenever(activityRepository.save(any())).thenReturn(activityEntity)

        // When
        val activityCreateRequest = SUBCONTRACTED_ACTIVITY_WITH_NO_EVIDENCE_DTO.copy(projectRoleId = projectRole.id)

        val activityCreated = sut.createSubcontractedActivity(activityCreateRequest, Locale.ENGLISH)

        // Then
        verifyNoInteractions(activityEvidenceService)
        val expectedResponseDTO = createActivityResponseDTO(userId = subcontractedUser.id, hasEvidences = false, description = activityEntity.description)


        Assertions.assertThat(activityCreated)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseDTO)
    }

    @Test
    fun `try to create a subcontracted activity without permissions`(){
        val authenticationWithoutSubcontractedPermissions: Authentication =
            ClientAuthentication(USER_ID_1.toString(), mapOf("roles" to listOf("")))
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutSubcontractedPermissions))
        assertThrows<IllegalStateException> {
            sut.createSubcontractedActivity(SUBCONTRACTED_ACTIVITY_WITH_EVIDENCE_DTO, Locale.ENGLISH)
        }
    }


    private fun `get activity entity without evidence`(projectRole: ProjectRole) =
            createActivity(description = "This is an activity without evidence", userId = USER_ENTITIES_SUBCONTRACTED.id,
                    projectRole = projectRole, hasEvidences = false,
                    approvalState = ApprovalState.NA)

    private fun `get role that requires evidence`() =
            PROJECT_ROLE.copy(requireEvidence = RequireEvidence.ONCE)


    private companion object {

        private val USER_ID_1 = 1L

        private val AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE: Authentication =
            ClientAuthentication(USER_ID_1.toString(), mapOf("roles" to listOf("subcontracted-activity-manager")))

        private val USER_ENTITIES_SUBCONTRACTED = createUser(LocalDate.now(),2,"subcontracted")

        private val TIME_NOW = LocalDateTime.now()

        private val DURATION = 10000

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
        private val PROJECT_ROLE =
                ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, 0, true, false, TimeUnit.MINUTES)




        private val EVIDENCE = EvidenceDTO.from("data:application/pdf;base64,SGVsbG8gV29ybGQh")

        private val SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO = SubcontractedActivityRequestDTO(
                null,
                TIME_NOW,
                TIME_NOW.plusMinutes(75L),
                DURATION,
                "New activity",
                false,
                PROJECT_ROLE.id,
                true,
                EVIDENCE,
        )

        private val SUBCONTRACTED_ACTIVITY_WITH_EVIDENCE_BUT_NOT_ATTACHED_DTO = SubcontractedActivityRequestDTO(
                null,
                TIME_NOW,
                TIME_NOW.plusMinutes(75L),
                DURATION,
                "New activity evidence empty",
                false,
                PROJECT_ROLE.id,
                true,
                null,
        )

        private val SUBCONTRACTED_ACTIVITY_WITH_ATTACHED_EVIDENCE_BUT_NOT_AS_TRUE = SubcontractedActivityRequestDTO(
                null,
                TIME_NOW,
                TIME_NOW.plusMinutes(75L),
                DURATION,
                "New activity evidence empty",
                false,
                PROJECT_ROLE.id,
                false,
                EVIDENCE,
        )

        private val SUBCONTRACTED_ACTIVITY_WITH_DATE_BEFORE_CREATION_PROJECT_DATE = SubcontractedActivityRequestDTO(
                null,
                TIME_NOW.minusDays(3),
                TIME_NOW.plusMinutes(75L).minusDays(3),
                DURATION,
                "New activity wit",
                false,
                PROJECT_ROLE.id,
                true,
                EVIDENCE
        )

        private val SUBCONTRACTED_ACTIVITY_WITH_EVIDENCE_DTO = SubcontractedActivityRequestDTO(
                null,
                TIME_NOW,
                TIME_NOW.plusMinutes(75L),
                DURATION,
                "New activity wit",
                false,
                PROJECT_ROLE.id,
                true,
                EVIDENCE
        )

        private val SUBCONTRACTED_ACTIVITY_WITH_NO_EVIDENCE_DTO = SubcontractedActivityRequestDTO(
                null,
                TIME_NOW,
                TIME_NOW.plusMinutes(75L),
                DURATION,
                "New activity wit",
                false,
                PROJECT_ROLE.id,
                false
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
                userId: Long = 2L,
                description: String = generateLargeDescription("New activity").substring(0, 2048),
                start: LocalDateTime = TIME_NOW,
                end: LocalDateTime = TIME_NOW.plusMinutes(75L),
                duration: Int = 18000,
                billable: Boolean = false,
                hasEvidences: Boolean = false,
                projectRole: ProjectRole = PROJECT_ROLE,
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
                duration: Int = 18000,
                billable: Boolean = false,
                hasEvidences: Boolean = false,
                projectRoleId: Long = 10L,
                approvalState: ApprovalState = ApprovalState.NA,
                timeUnit: TimeUnit = PROJECT_ROLE.timeUnit,
        ): SubcontractedActivityResponseDTO =
            SubcontractedActivityResponseDTO(
                        billable,
                        duration,
                        description,
                        hasEvidences,
                        id,
                        projectRoleId,
                        IntervalResponseDTO(start, end, duration, timeUnit),
                        userId,
                        ApprovalDTO(state = approvalState)
                )

        @JvmStatic
        fun exceptionProvider() = arrayOf(
            arrayOf(
                "ActivityWithEvidenceButNotAttachedException",
                SUBCONTRACTED_ACTIVITY_WITH_EVIDENCE_BUT_NOT_ATTACHED_DTO
            ),
            arrayOf(
                "ActivityWithAttachedEvidenceButNotTrueException",
                SUBCONTRACTED_ACTIVITY_WITH_ATTACHED_EVIDENCE_BUT_NOT_AS_TRUE
            )
        )

    }

}

