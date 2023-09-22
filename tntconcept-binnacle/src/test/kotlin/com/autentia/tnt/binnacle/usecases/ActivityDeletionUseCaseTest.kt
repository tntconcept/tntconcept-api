package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
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
    private val activityEvidenceService = mock<ActivityEvidenceService>()
    private val securityService = mock<SecurityService>()

    private val useCase = ActivityDeletionUseCase(activityRepository, activityValidator,
            activityEvidenceService, securityService)

    @AfterEach
    fun resetMocks() {
        reset(activityRepository, activityValidator, activityEvidenceService)
    }

    @BeforeAll
    fun setAuthMock() {
        val authenticatedUser = ClientAuthentication("id", mapOf("roles" to listOf("user")))
        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
    }

    @Test
    fun `call the repository to delete the activity`() {
        whenever(activityRepository.findById(1L)).thenReturn(entityActivity)

        useCase.deleteActivityById(1L)

        verify(activityRepository).deleteById(1L)
    }

    @Test
    fun `throw not found exception from the validator`() {
        doThrow(ActivityNotFoundException(1L)).whenever(activityRepository).findById(1L)

        assertThrows<ActivityNotFoundException> {
            useCase.deleteActivityById(1L)
        }
    }

    private companion object {
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
                1L,
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
