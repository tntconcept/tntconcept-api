package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.validators.SubcontractedActivityValidator
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class SubcontractedActivityDeletionUseCaseTest {
    private val activityRepository = mock<ActivityRepository>()
    private val subcontractedActivityValidator = mock<SubcontractedActivityValidator>()
    private val activityEvidenceService = mock<ActivityEvidenceService>()
    private val securityService = mock<SecurityService>()
    private val userRepository = mock<UserRepository>()
    private val appProperties = AppProperties()

    private val useCase = SubcontractedActivityDeletionUseCase(activityRepository, subcontractedActivityValidator,
            activityEvidenceService, securityService, userRepository)

    @AfterEach
    fun resetMocks() {
        reset(activityRepository, subcontractedActivityValidator, activityEvidenceService, userRepository)
    }

    @BeforeEach
    fun authenticate(){
        whenever(securityService.authentication).thenReturn(Optional.of(AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE))
    }

    private fun generateSubcontractedUser():User{
        appProperties.binnacle.subcontractedUser.username="subcontracted"
        whenever(userRepository.findByUsername("subcontracted")).thenReturn(USER_ENTITIES_SUBCONTRACTED)
        return USER_ENTITIES_SUBCONTRACTED
    }
    @Test
    fun `call the repository to delete the activity`() {
        whenever(activityRepository.findById(1L)).thenReturn(entityActivity)
        generateSubcontractedUser()

        useCase.deleteSubcontractedActivityById(1L)

        verify(activityRepository).deleteById(1L)
    }

    @Test
    fun `throw not found exception from the validator`() {
        doThrow(ActivityNotFoundException(1L)).whenever(activityRepository).findById(1L)

        assertThrows<ActivityNotFoundException> {
            useCase.deleteSubcontractedActivityById(1L)
        }
    }

    private companion object {
        private val USER_ID_1 = 1L

        private val AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE: Authentication =
                ClientAuthentication(USER_ID_1.toString(), mapOf("roles" to listOf("subcontracted-activity-manager")))

        private val USER_ENTITIES_SUBCONTRACTED = createUser(LocalDate.now(),2,"subcontracted")

        val ORGANIZATION = Organization(1L, "Dummy Organization", 1, listOf())
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
                2L,
                false,
                null,
                LocalDateTime.now(),
                false,
                ApprovalState.NA,
                null
        )

        private val entityActivity = Activity.of(activity, PROJECT_ROLE)
    }

}