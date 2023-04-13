package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class ActivitiesBetweenDateUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val activityConverter = mock<ActivityResponseConverter>()

    private val activitiesBetweenDateUseCase = ActivitiesBetweenDateUseCase(
        activityService,
        activityConverter
    )

    @Test
    fun `get activities between start and end date`() {
        whenever(activityService.getActivitiesBetweenDates(any())).thenReturn(listOf(activity))
        whenever(activityConverter.mapActivitiesToActivitiesResponseDTO(listOf(activity))).thenReturn(activitiesResponseDTO)

        val actual = activitiesBetweenDateUseCase.getActivities(startDate, endDate)

        assertEquals(activitiesResponseDTO, actual)
    }

    private companion object{
        val startDate: LocalDate = LocalDate.of(2019, 1, 1)
        val endDate: LocalDate = LocalDate.of(2019, 1, 31)
        val approvalState = ApprovalState.PENDING

        val intervalResponseDTO = IntervalResponseDTO(
            start = startDate.atStartOfDay(),
            end = endDate.atStartOfDay(),
            duration = 120,
            timeUnit = TimeUnit.MINUTES
        )

        val organization = Organization(1L, "Dummy Organization", listOf())
        val project = Project(
            1L,
            "Dummy Project",
            open = true,
            billable = false,
            organization,
            listOf()
        )

        val projectRole = ProjectRole(10L, "Dummy Project role", RequireEvidence.NO,
            project, 0, true, false, TimeUnit.MINUTES)


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
                billable= false,
                description = "description",
                hasEvidences = false,
                id = 1L,
                projectRoleId = 1,
                interval = intervalResponseDTO,
                userId = 1,
                approvalState = approvalState
            )
        )

    }
}