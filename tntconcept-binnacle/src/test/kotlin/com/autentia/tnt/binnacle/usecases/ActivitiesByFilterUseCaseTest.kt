package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.dto.ActivityFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ApprovalStateActivityFilter
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.predicates.*
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.*

internal class ActivitiesByFilterUseCaseTest {
    private val activityRepository = mock<ActivityRepository>()
    private val securityService = mock<SecurityService>()
    private val activityResponseConverter = ActivityResponseConverter(ActivityIntervalResponseConverter())
    private val activitiesByFilterUseCase =
        ActivitiesByFilterUseCase(activityRepository, securityService, activityResponseConverter)

    @BeforeEach
    fun setUp() {
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))
    }

    @Test
    fun `get pending activities by approval state`() {
        val activityFilterDTO = ActivityFilterDTO(
            approvalState = ApprovalStateActivityFilter.PENDING,
        )
        val compositedSpecification = ActivityApprovalStateSpecification(ApprovalState.valueOf(activityFilterDTO.approvalState!!.name))

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityRepository).findAll(compositedSpecification)
    }

    @Test
    fun `get all activities by approval state`() {
        val activityFilterDTO = ActivityFilterDTO(
            approvalState = ApprovalStateActivityFilter.ALL,
        )
            val compositedSpecification =
            PredicateBuilder.or(
                ActivityApprovalStateSpecification(ApprovalState.PENDING),
                ActivityApprovalStateSpecification(ApprovalState.ACCEPTED)
        )

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityRepository).findAll(compositedSpecification)
    }

    @Test
    fun `get activities by approval state and role id`() {
        val activityFilterDTO = ActivityFilterDTO(
            approvalState = ApprovalStateActivityFilter.PENDING,
            roleId = 1L
        )
        val compositedSpecification = PredicateBuilder.and(
            ActivityApprovalStateSpecification(ApprovalState.valueOf(activityFilterDTO.approvalState!!.name)),
            ActivityRoleIdSpecification(activityFilterDTO.roleId!!)
        )

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityRepository).findAll(compositedSpecification)
    }

    @Test
    fun `get activities between dates`() {
        val activityFilterDTO = ActivityFilterDTO(
            startDate = startDate,
            endDate = endDate,
        )
        val compositedSpecification = PredicateBuilder.and(
            ActivityStartDateLessOrEqualSpecification(activityFilterDTO.endDate!!),
            ActivityEndDateGreaterOrEqualSpecification(activityFilterDTO.startDate!!)
        )

        whenever(activityRepository.findAll(compositedSpecification)).thenReturn(listOf(activity))

        val activities = activitiesByFilterUseCase.getActivities(activityFilterDTO)

        val expectedActivity = activityResponseConverter.toActivityResponseDTO(activity.toDomain())

        verify(activityRepository).findAll(compositedSpecification)
        assertThat(activities).containsExactly(expectedActivity)
    }

    @Test
    fun `get activities should ignore project id when role id`() {
        val activityFilterDTO = ActivityFilterDTO(
            projectId = 1L,
            roleId = 1L
        )
        val compositedSpecification = ActivityRoleIdSpecification(activityFilterDTO.roleId!!)

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityRepository).findAll(compositedSpecification)
    }

    @Test
    fun `get activities by organization id`() {
        val activityFilterDTO = ActivityFilterDTO(
            organizationId = 1L,
        )
        val compositedSpecification = ActivityOrganizationIdSpecification(activityFilterDTO.organizationId!!)

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityRepository).findAll(compositedSpecification)
    }

    @Test
    fun `get activities should ignore organization id when project id`() {
        val activityFilterDTO = ActivityFilterDTO(
            startDate = null,
            endDate = null,
            approvalState = null,
            organizationId = 1L,
            projectId = 1L,
            roleId = null
        )
        val compositedSpecification = ActivityProjectIdSpecification(activityFilterDTO.projectId!!)

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityRepository).findAll(compositedSpecification)
    }

    @Test
    fun `get activities when multiple parameters`() {
        val activityFilterDTO = ActivityFilterDTO(
            startDate,
            endDate,
            null,
            1L,
            1L,
            null,
            3L,
        )
        val compositedSpecification =
            PredicateBuilder.and(
                PredicateBuilder.and(
                    PredicateBuilder.and(
                        ActivityStartDateLessOrEqualSpecification(activityFilterDTO.endDate!!),
                        ActivityEndDateGreaterOrEqualSpecification(activityFilterDTO.startDate!!)
                    ), ActivityProjectIdSpecification(activityFilterDTO.projectId!!)
                ), ActivityUserIdSpecification(activityFilterDTO.userId!!)
            )

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityRepository).findAll(compositedSpecification)
    }

    private companion object {
        const val userId = 4L
        val startDate: LocalDate = LocalDate.of(2023, 4, 15)
        val endDate: LocalDate = LocalDate.of(2023, 4, 17)

        private val authentication =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("activity-approval")))

        val projectRole = createProjectRole(1L)
        val activity =
            Activity(
                1,
                startDate.atStartOfDay(),
                endDate.atStartOfDay(),
                45,
                "New activity",
                projectRole,
                1,
                false,
                null,
                null,
                false,
                approvalState = ApprovalState.PENDING
            )
    }
}