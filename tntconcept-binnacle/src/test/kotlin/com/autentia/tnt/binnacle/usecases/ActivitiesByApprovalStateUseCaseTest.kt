package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime

internal class ActivitiesByApprovalStateUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val activityConverter = org.mockito.kotlin.mock<ActivityResponseConverter>()

    private val activitiesByApprovalStateUseCase = ActivitiesByApprovalStateUseCase(activityService, activityConverter)

    @Test
    fun `get activities by approval state`() {
        whenever(activityService.getActivitiesApprovalState(approvalState)).thenReturn(listOf(activity))
        whenever(activityConverter.mapActivitiesToActivitiesResponseDTO(listOf(activity))).thenReturn(
            activitiesResponseDTO
        )

        val actual = activitiesByApprovalStateUseCase.getActivities(
            approvalState
        )

        assertEquals(activitiesResponseDTO, actual)
    }

    private companion object {
        val startDate: LocalDate = LocalDate.of(2019, 1, 1)
        val endDate: LocalDate = LocalDate.of(2019, 1, 31)
        val approvalState = ApprovalState.PENDING

        val activity = createActivity()

        val intervalResponseDTO = IntervalResponseDTO(
            start = startDate.atStartOfDay(),
            end = endDate.atTime(LocalTime.MAX),
            duration = 120,
            timeUnit = TimeUnit.MINUTES
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
                approvalState = approvalState
            )
        )
    }
}