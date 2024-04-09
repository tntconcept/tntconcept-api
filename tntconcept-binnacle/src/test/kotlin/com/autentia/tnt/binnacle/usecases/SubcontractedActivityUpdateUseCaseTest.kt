package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.RequireEvidence.NO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.validators.SubcontractedActivityValidator
import io.archimedesfw.commons.time.ClockUtils
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

internal class SubcontractedActivityUpdateUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()
    private val subcontractedActivityValidator = mock<SubcontractedActivityValidator>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val userRepository = mock<UserRepository>()
    private val securityService = mock<SecurityService>()
    private val activityEvidenceService = mock<ActivityEvidenceService>()
    private val appProperties = AppProperties()


    private val sut = SubcontractedActivityUpdateUseCase(
        activityRepository,
        projectRoleRepository,
        userRepository,
        subcontractedActivityValidator,
        ActivityRequestBodyConverter(),
        ActivityResponseConverter(
            ActivityIntervalResponseConverter()
        ),
        activityEvidenceService,
        securityService,
        appProperties

    )

    fun authenticate() {
        whenever(securityService.authentication).thenReturn(Optional.of(AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE))
    }

    @BeforeEach
    fun generateSubcontractedUser() {
        appProperties.binnacle.subcontractedUser.username = "subcontracted"
        whenever(userRepository.findByUsername("subcontracted")).thenReturn(USER_ENTITIES_SUBCONTRACTED)
    }

    @BeforeEach
    fun `check that activity is valid`() {
        doNothing().whenever(subcontractedActivityValidator).checkActivityIsValidForUpdate(any(), any())
    }


    @Test
    fun `should not update an activity when user is not authenticated`() {

        // Act, Assert
        assertThatThrownBy { sut.updateSubcontractedActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(
            IllegalStateException::class.java
        )

        // Verify
        verifyNoInteractions(
            activityRepository,
            subcontractedActivityValidator,
            activityEvidenceService,
            projectRoleRepository
        )
    }

    @Test
    fun `should not update an existing activity with a role that is not found`() {
        authenticate()
        // Arrange
        whenever(projectRoleRepository.findById(any())).thenReturn(null)

        // Act, Assert
        assertThatThrownBy { sut.updateSubcontractedActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(
            ProjectRoleNotFoundException::class.java
        )

        // Verify
        verify(projectRoleRepository).findById(SOME_ACTIVITY_REQUEST.projectRoleId)
        verifyNoInteractions(activityRepository, subcontractedActivityValidator, activityEvidenceService)
        verifyNoMoreInteractions(projectRoleRepository)
    }


    @Test
    fun `should not update a non existing activity`() {
        authenticate()
        // Arrange
        val role = PROJECT_ROLE
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)
        whenever(activityRepository.findById(any())).thenReturn(null)

        // Act, Assert
        assertThatThrownBy { sut.updateSubcontractedActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(
            ActivityNotFoundException::class.java
        )

        // Verify
        verify(projectRoleRepository).findById(SOME_ACTIVITY_REQUEST.projectRoleId)
        verify(activityRepository).findById(SOME_ACTIVITY_REQUEST.id!!)
        verifyNoInteractions(subcontractedActivityValidator, activityEvidenceService)
        verifyNoMoreInteractions(projectRoleRepository, activityRepository)
    }

    private fun `get activity updated with request`(
        existingActivity: Activity,
        request: SubcontractedActivityRequestDTO,
        duration: Int
    ): Activity =
        existingActivity.copy(
            duration = duration,
            start = request.month.atDay(1).atTime(0, 0),
            end = request.month.atEndOfMonth().atTime(23, 59),
            description = request.description,
            billable = request.billable
        )


    private fun `get role that does not require evidence`() =
        PROJECT_ROLE.copy(requireEvidence = NO, timeUnit = TimeUnit.MINUTES)


    private fun `get existing activity with no evidence`(role: ProjectRole, approvalState: ApprovalState) =
        Activity.emptyActivity(role).copy(
            id = 1L,
            userId = USER_ENTITIES_SUBCONTRACTED.id,
            duration = 18000,
            hasEvidences = false,
            approvalState = approvalState,
            start = LocalDate.now().atTime(8, 0),
            end = LocalDate.now().atTime(12, 0),
            insertDate = Date.from(Instant.now()),
            departmentId = 1L,
        )

    private fun assertThatUpdatedActivityIsEquivalent(
        result: SubcontractedActivityResponseDTO,
        request: SubcontractedActivityRequestDTO
    ) {
        assertThat(result.month).isEqualTo(request.month)
        assertThat(result.duration).isEqualTo(request.duration)
        assertThat(result.description).isEqualTo(request.description)
        assertThat(result.billable).isEqualTo(request.billable)
        assertThat(result.projectRoleId).isEqualTo(request.projectRoleId)
    }

    private companion object {

        private val USER_ID_1 = 1L

        private val AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE: Authentication =
            ClientAuthentication(USER_ID_1.toString(), mapOf("roles" to listOf("subcontracted-activity-manager")))

        private val USER_ENTITIES_SUBCONTRACTED = createUser(LocalDate.now(), 2, "subcontracted")

        private val TODAY = ClockUtils.nowUtc()
        private val DURATION = 18000

        private val ORGANIZATION = Organization(1L, "Organization", 1, listOf())
        private val PROJECT = Project(
            1L, "Project", open = true, billable = false,
            LocalDate.now(), null, null, projectRoles = listOf(),
            organization = ORGANIZATION
        )
        private val PROJECT_ROLE = ProjectRole(
            10L, "Project role", NO, PROJECT, 5000,
            1000, true, false, TimeUnit.MINUTES
        )
        private val LOCALE = Locale.ENGLISH
        private val SOME_ACTIVITY_REQUEST = SubcontractedActivityRequestDTO(
            1L,
            YearMonth.of(TODAY.year, TODAY.month),
            DURATION,
            "New activity",
            false,
            PROJECT_ROLE.id,
        )
    }

}

