package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityBeforeProjectCreationDateException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.validators.SubcontractedActivityValidator
import io.archimedesfw.commons.time.ClockUtils
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.*


class SubcontractedActivityCreationUseCaseTest {

    private val projectRepository = mock<ProjectRepository>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityRepository = mock<ActivityRepository>()
    private val activityEvidenceService = mock<ActivityEvidenceService>()
    private val userRepository = mock<UserRepository>()
    private val securityService: SecurityService = mock()
    private val appProperties = AppProperties()
    private val activityService = mock<ActivityService>()
    private val calendarService = mock<ActivityCalendarService>()

    private val subcontractedActivityValidator =
        SubcontractedActivityValidator(
            activityService,
            calendarService,
            projectRepository
        )

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


    private fun authenticate() {
        whenever(securityService.authentication).thenReturn(Optional.of(AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE))
    }

    private fun generateSubcontractedUser(): User {
        appProperties.binnacle.subcontractedUser.username = "subcontracted"
        whenever(userRepository.findByUsername("subcontracted")).thenReturn(USER_ENTITIES_SUBCONTRACTED)
        return USER_ENTITIES_SUBCONTRACTED
    }

    @Test
    fun `create subcontracted activity with a nonexistent projectRol throws an exception`() {
        authenticate()
        generateSubcontractedUser()

        assertThrows<ProjectRoleNotFoundException> {
            sut.createSubcontractedActivity(SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO, Locale.ENGLISH)
        }
    }

    @Test
    fun `create subcontracted activity before project creation date throws an exception`() {
        authenticate()
        val subcontractedUser = generateSubcontractedUser()

        val activityEntity = createActivity(userId = subcontractedUser.id)
        whenever(projectRoleRepository.findById(PROJECT_ROLE.id)).thenReturn(PROJECT_ROLE)
        whenever(projectRepository.findById(activityEntity.projectRole.project.id)).thenReturn(
            Optional.of(
                activityEntity.projectRole.project
            )
        )

        assertThrows<ActivityBeforeProjectCreationDateException> {
            sut.createSubcontractedActivity(
                SUBCONTRACTED_ACTIVITY_WITH_DATE_BEFORE_CREATION_PROJECT_DATE,
                Locale.ENGLISH
            )
        }

    }


    @Test
    fun `create subcontracted activity`() {

        authenticate()
        val subcontractedUser = generateSubcontractedUser()
        val activityEntity = createActivity(userId = subcontractedUser.id)

        whenever(projectRoleRepository.findById(PROJECT_ROLE.id)).thenReturn(PROJECT_ROLE)
        whenever(projectRepository.findById(activityEntity.projectRole.project.id)).thenReturn(
            Optional.of(
                activityEntity.projectRole.project
            )
        )
        whenever(activityRepository.saveSubcontracted(any())).thenReturn(activityEntity)

        val activityCreated = sut.createSubcontractedActivity(SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO, Locale.ENGLISH)


        val expectedResponseDTO = createActivityResponseDTO(userId = subcontractedUser.id)
        Assertions.assertThat(activityCreated)
            .usingRecursiveComparison()
            .isEqualTo(expectedResponseDTO)
    }


    @Test
    fun `try to create a subcontracted activity without subcontracted manager role permission`() {
        val authenticationWithoutSubcontractedPermissions: Authentication =
            ClientAuthentication(USER_ID_1.toString(), mapOf("roles" to listOf("")))
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutSubcontractedPermissions))
        assertThrows<IllegalStateException> {
            sut.createSubcontractedActivity(SUBCONTRACTED_ACTIVITY_DTO, Locale.ENGLISH)
        }
    }


    private companion object {

        private val USER_ID_1 = 1L

        private val AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE: Authentication =
            ClientAuthentication(USER_ID_1.toString(), mapOf("roles" to listOf("subcontracted-activity-manager")))

        private val USER_ENTITIES_SUBCONTRACTED = createUser(LocalDate.now(), 2, "subcontracted")

        private val TIME_NOW = YearMonth.of(ClockUtils.nowUtc().year,ClockUtils.nowUtc().month)

        private val DURATION = 10000

        private val TODAY = Date()

        private val ORGANIZATION = Organization(1L, "Dummy Organization", 1, listOf())

        private val PROJECT = Project(
            1L,
            "Dummy Project",
            open = true,
            billable = false,
            ClockUtils.nowUtc().toLocalDate().minusMonths(1),
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
            DURATION,
            "New activity",
            PROJECT_ROLE.id
        )

        private val SUBCONTRACTED_ACTIVITY_WITH_DATE_BEFORE_CREATION_PROJECT_DATE = SubcontractedActivityRequestDTO(
            null,
            TIME_NOW.minusMonths(4),
            DURATION,
            "New activity wit",
            PROJECT_ROLE.id,
        )

        private val SUBCONTRACTED_ACTIVITY_DTO = SubcontractedActivityRequestDTO(
            null,
            TIME_NOW,
            DURATION,
            "New activity wit",
            PROJECT_ROLE.id,
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
            start: LocalDateTime = TIME_NOW.atDay(1).atTime(0,0),
            end: LocalDateTime = TIME_NOW.atEndOfMonth().atTime(23,59),
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
            month: YearMonth = TIME_NOW,
            duration: Int = 18000,
            billable: Boolean = false,
            projectRoleId: Long = 10L,
        ): SubcontractedActivityResponseDTO =
            SubcontractedActivityResponseDTO(
                duration,
                description,
                id,
                projectRoleId,
                month,
                userId
            )

    }

}

