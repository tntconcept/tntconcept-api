package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createUser
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
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.validators.SubcontractedActivityValidator
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.*
import org.mockito.kotlin.*
import java.time.Instant
import java.time.LocalDate
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

    fun authenticate(){
        whenever(securityService.authentication).thenReturn(Optional.of(AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE))
    }

    @BeforeEach
    fun generateSubcontractedUser(){
        appProperties.binnacle.subcontractedUser.username="subcontracted"
        whenever(userRepository.findByUsername("subcontracted")).thenReturn(USER_ENTITIES_SUBCONTRACTED)
    }

    @BeforeEach
    fun `check that activity is valid`() {
        doNothing().whenever(subcontractedActivityValidator).checkActivityIsValidForUpdate(any(), any())
    }


    @Test
    fun `should update an existing activity with no evidence in a role that does not require evidence`() {
        authenticate()
        // Arrange
        val role = `get role that does not require evidence`()
        whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

        val existingActivity = `get existing activity with no evidence`(role, NA)
        whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

        val duration = 18000

        val request = `get activity update request with no evidence`(existingActivity, duration)
        val updatedActivity = `get activity updated with request`(existingActivity, request, duration)
        whenever(activityRepository.update(any())).thenReturn(updatedActivity)

        // Act
        val result = sut.updateSubcontractedActivity(request, LOCALE)

        // Assert
        assertThatUpdatedActivityIsEquivalent(result, request)
        assertThat(result.approval.state).isEqualTo(NA)

        // Verify
        verify(projectRoleRepository).findById(role.id)
        verify(activityRepository).findById(existingActivity.id!!)
        verify(activityRepository).update(updatedActivity)
    }


        @Test
        fun `should not update an activity when user is not authenticated`() {

            // Act, Assert
            assertThatThrownBy { sut.updateSubcontractedActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(IllegalStateException::class.java)

            // Verify
            verifyNoInteractions(activityRepository, subcontractedActivityValidator, activityEvidenceService, projectRoleRepository)
        }

        @Test
        fun `should not update an existing activity with a role that is not found`() {
            authenticate()
            // Arrange
            whenever(projectRoleRepository.findById(any())).thenReturn(null)

            // Act, Assert
            assertThatThrownBy { sut.updateSubcontractedActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(ProjectRoleNotFoundException::class.java)

            // Verify
            verify(projectRoleRepository).findById(SOME_ACTIVITY_REQUEST.projectRoleId)
            verifyNoInteractions(activityRepository, subcontractedActivityValidator, activityEvidenceService)
            verifyNoMoreInteractions(projectRoleRepository)
        }

        @Test
        fun `should not update an existing activity when activity is not valid for updating`() {
            authenticate()
            // Arrange
            val role = PROJECT_ROLE
            whenever(projectRoleRepository.findById(role.id)).thenReturn(role)

            val existingActivity = `get existing activity with no evidence`(role, NA)
            whenever(activityRepository.findById(existingActivity.id!!)).thenReturn(existingActivity)

            val duration = 60

            val request = `get activity update request with no evidence`(existingActivity, duration)
            val updatedActivity = `get activity updated with request`(existingActivity, request, duration)

            doThrow(IllegalArgumentException::class).whenever(subcontractedActivityValidator)
                    .checkActivityIsValidForUpdate(updatedActivity.toDomain(), existingActivity.toDomain())

            // Act, Assert
            assertThatThrownBy { sut.updateSubcontractedActivity(request, LOCALE) }.isInstanceOf(IllegalArgumentException::class.java)

            // Verify
            verify(projectRoleRepository).findById(request.projectRoleId)
            verify(activityRepository).findById(request.id!!)
            verify(subcontractedActivityValidator).checkActivityIsValidForUpdate(updatedActivity.toDomain(), existingActivity.toDomain())
            verifyNoInteractions(activityEvidenceService)
            verifyNoMoreInteractions(projectRoleRepository, activityRepository)
        }

        @Test
        fun `should not update a non existing activity`() {
            authenticate()
            // Arrange
            val role = PROJECT_ROLE
            whenever(projectRoleRepository.findById(role.id)).thenReturn(role)
            whenever(activityRepository.findById(any())).thenReturn(null)

            // Act, Assert
            assertThatThrownBy { sut.updateSubcontractedActivity(SOME_ACTIVITY_REQUEST, LOCALE) }.isInstanceOf(ActivityNotFoundException::class.java)

            // Verify
            verify(projectRoleRepository).findById(SOME_ACTIVITY_REQUEST.projectRoleId)
            verify(activityRepository).findById(SOME_ACTIVITY_REQUEST.id!!)
            verifyNoInteractions(subcontractedActivityValidator, activityEvidenceService)
            verifyNoMoreInteractions(projectRoleRepository, activityRepository)
        }

    private fun `get activity updated with request`(existingActivity: Activity, request: SubcontractedActivityRequestDTO, duration: Int): Activity =
            existingActivity.copy(
                    duration = duration,
                    start = request.interval.start,
                    end = request.interval.end,
                    description = request.description,
                    billable = request.billable,
                    hasEvidences = request.hasEvidences,
            )


    private fun `get activity update request with no evidence`(existingActivity: Activity, duration: Int) =
            SubcontractedActivityRequestDTO(
                    id = existingActivity.id!!,
                    start = TODAY,
                    end = TODAY.plusDays(30),
                    duration = duration,
                    description = existingActivity.description + " updated",
                    billable = existingActivity.billable,
                    projectRoleId = existingActivity.projectRole.id,
                    hasEvidences = false,
                    evidence = null,
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

    private fun assertThatUpdatedActivityIsEquivalent(result: SubcontractedActivityResponseDTO, request: SubcontractedActivityRequestDTO) {
        assertThat(result.interval.start).isEqualTo(request.interval.start)
        assertThat(result.interval.end).isEqualTo(request.interval.end)
        assertThat(result.duration).isEqualTo(request.duration)
        assertThat(result.description).isEqualTo(request.description)
        assertThat(result.billable).isEqualTo(request.billable)
        assertThat(result.hasEvidences).isEqualTo(request.hasEvidences)
        assertThat(result.projectRoleId).isEqualTo(request.projectRoleId)
    }

    private companion object {

        private val USER_ID_1 = 1L

        private val AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE: Authentication =
                ClientAuthentication(USER_ID_1.toString(), mapOf("roles" to listOf("subcontracted-activity-manager")))

        private val USER_ENTITIES_SUBCONTRACTED = createUser(LocalDate.now(),2,"subcontracted")

        private val TODAY = LocalDate.now().atTime(8, 0)
        private val DURATION = 18000

        private val ORGANIZATION = Organization(1L, "Organization", 1, listOf())
        private val PROJECT = Project(1L, "Project", open = true, billable = false,
                LocalDate.now(), null, null, projectRoles = listOf(),
                organization = ORGANIZATION
        )
        private val PROJECT_ROLE = ProjectRole(10L, "Project role", NO, PROJECT, 5000,
                1000, true, false, TimeUnit.MINUTES)
        private val LOCALE = Locale.ENGLISH
        private val SOME_ACTIVITY_REQUEST = SubcontractedActivityRequestDTO(
                1L,
                TODAY,
                TODAY.plusMinutes(75L),
                DURATION,
                "New activity",
                false,
                PROJECT_ROLE.id,
                false,
        )
    }

}

