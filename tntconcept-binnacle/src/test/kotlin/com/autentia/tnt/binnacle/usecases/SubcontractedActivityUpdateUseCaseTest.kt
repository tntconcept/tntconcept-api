package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.utils.toDate
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.RequireEvidence.NO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityRequestDTO
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
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
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
    fun `check that subcontracted activity is valid for update`() {
        doNothing().whenever(subcontractedActivityValidator).checkActivityIsValidForUpdate(any(), any())
    }


    @Test
    fun `should not update a subcontracted activity when user is not authenticated`() {

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
    fun `should not update an existing subcontracted activity with a role that is not found`() {
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
    fun `should not update a non existing subcontracted activity`() {
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

    @Test
    fun `should update a subcontracted activity`(){
        authenticate()

        val activity = createActivity(
            id = SOME_ACTIVITY_REQUEST.id!!,
            month = SOME_ACTIVITY_REQUEST.month,
            duration = 10000,
            description = SOME_ACTIVITY_REQUEST.description,
            projectRole = PROJECT_ROLE
            )

        whenever(projectRoleRepository.findById(PROJECT_ROLE.id)).thenReturn(PROJECT_ROLE)
        whenever(activityRepository.findById(any())).thenReturn(activity)
        whenever(activityRepository.update(any())).thenReturn(activity)
        val activityToUpdate = SOME_ACTIVITY_REQUEST.copy(duration = 1000)

        val result = sut.updateSubcontractedActivity(activityToUpdate, LOCALE)


        Assertions.assertEquals(activity.duration,result.duration)


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
            PROJECT_ROLE.id,
        )

        private fun createActivity(
            id: Long = 1L,
            userId: Long = 2L,
            description: String = "New activity",
            month: YearMonth,
            duration: Int = 18000,
            projectRole: ProjectRole = PROJECT_ROLE,
        ): Activity =
            Activity(
                id = id,
                userId = userId,
                description = description,
                start = month.atDay(1).atTime(0,0),
                end = month.atEndOfMonth().atTime(23,59),
                duration = duration,
                billable = true,
                hasEvidences = false,
                projectRole = projectRole,
                approvalState = ApprovalState.NA,
                insertDate = toDate(month.atDay(1))
            )
    }

}

