package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ActivityFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import com.autentia.tnt.binnacle.repositories.predicates.*
import com.autentia.tnt.binnacle.services.ActivityService
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class ActivitiesByFilterUseCaseTest {
    private val activityService = mock<ActivityService>()
    private val activityResponseConverter = mock<ActivityResponseConverter>()
    private val activityPredicateBuilder = PredicateBuilder<Activity>()
    private val activitiesByFilterUseCase =
        ActivitiesByFilterUseCase(activityService, activityPredicateBuilder, activityResponseConverter)

    @Test
    fun `test get activities`() {
        whenever(activityService.getActivities(any())).thenReturn(listOf(activity))
        whenever(activityResponseConverter.mapActivitiesToActivitiesResponseDTO(listOf(activity))).thenReturn(
            activitiesResponseDTO
        )

        val activities =
            activitiesByFilterUseCase.getActivities(ActivityFilterDTO(startDate = startDate, endDate = endDate))

        assertEquals(activitiesResponseDTO, activities)
    }

    @Test
    fun `get activities by approval state`() {
        val activityFilterDTO = ActivityFilterDTO(
            approvalState = ApprovalState.PENDING,
        )
        val compositedSpecification =
            activityPredicateBuilder.where(
                ActivityApprovalStateSpecification(activityFilterDTO.approvalState!!)
            )

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityService).getActivities(compositedSpecification)
    }

    @Test
    fun `get activities by approval state and role id`() {
        val activityFilterDTO = ActivityFilterDTO(
            approvalState = ApprovalState.PENDING,
            roleId = 1L
        )
        val compositedSpecification =
            activityPredicateBuilder.and(
                ActivityApprovalStateSpecification(activityFilterDTO.approvalState!!),
                ActivityRoleIdSpecification(activityFilterDTO.roleId!!)
            )

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityService).getActivities(compositedSpecification)
    }

    @Test
    fun `get activities between dates`() {
        val activityFilterDTO = ActivityFilterDTO(
            startDate = startDate,
            endDate = endDate,
        )
        val compositedSpecification =
            activityPredicateBuilder.and(
                ActivityStartDateLessOrEqualSpecification(activityFilterDTO.startDate!!),
                ActivityEndDateGreaterOrEqualSpecification(activityFilterDTO.endDate!!)
            )
        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityService).getActivities(compositedSpecification)

    }

    @Test
    fun `get activities should ignore project id when role id`() {
        val activityFilterDTO = ActivityFilterDTO(
            projectId = 1L,
            roleId = 1L
        )
        val compositedSpecification =
            activityPredicateBuilder.where(
                ActivityRoleIdSpecification(activityFilterDTO.roleId!!)
            )

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityService).getActivities(compositedSpecification)
    }

    @Test
    fun `get activities by organization id`() {
        val activityFilterDTO = ActivityFilterDTO(
            organizationId = 1L,
        )
        val compositedSpecification =
            activityPredicateBuilder.where(
                ActivityOrganizationIdSpecification(activityFilterDTO.organizationId!!)
            )

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityService).getActivities(compositedSpecification)
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
        val compositedSpecification =
            activityPredicateBuilder.where(
                ActivityProjectIdSpecification(activityFilterDTO.projectId!!)
            )

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityService).getActivities(compositedSpecification)
    }

    @Test
    fun `get activities when multiple parameters`() {
        val activityFilterDTO = ActivityFilterDTO(
            startDate = startDate,
            endDate = endDate,
            approvalState = null,
            organizationId = 1L,
            projectId = 1L,
            roleId = null
        )
        val compositedSpecification =
            activityPredicateBuilder.and(
                activityPredicateBuilder.and(
                    ActivityStartDateLessOrEqualSpecification(activityFilterDTO.startDate!!),
                    ActivityEndDateGreaterOrEqualSpecification(activityFilterDTO.endDate!!)
                ), ActivityProjectIdSpecification(activityFilterDTO.projectId!!)
            )

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityService).getActivities(compositedSpecification)
    }

    private companion object {
        val startDate = LocalDate.of(2023, 4, 15)
        val endDate = LocalDate.of(2023, 4, 17)
        val intervalResponseDTO = IntervalResponseDTO(
            start = startDate.atStartOfDay(),
            end = endDate.atStartOfDay(),
            duration = 120,
            timeUnit = TimeUnit.MINUTES
        )

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

        val activitiesResponseDTO = listOf(
            ActivityResponseDTO(
                billable = false,
                description = "description",
                hasEvidences = false,
                id = 1L,
                projectRoleId = 1,
                interval = intervalResponseDTO,
                userId = 1,
                approvalState = ApprovalState.PENDING
            )
        )

    }
}