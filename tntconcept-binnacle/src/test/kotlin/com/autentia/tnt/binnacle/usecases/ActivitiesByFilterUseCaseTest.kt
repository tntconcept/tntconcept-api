package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.dto.ActivityFilterDTO
import com.autentia.tnt.binnacle.services.ActivityService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class ActivitiesByFilterUseCaseTest {
    private val activityService = mock<ActivityService>()
    private val activityResponseConverter = mock<ActivityResponseConverter>()

    private val activitiesByFilterUseCase = ActivitiesByFilterUseCase(activityService, activityResponseConverter)

    @Test
    fun `get activities by filter`() {
        val activityFilterDTO = ActivityFilterDTO(
            startDate = startDate,
            endDate = endDate,
            approvalState = ApprovalState.PENDING,
            organizationId = 1L,
            projectId = 1L,
            roleId = 1L
        )
        whenever(activityService.getActivities(any())).thenReturn(listOf(activity))

        activitiesByFilterUseCase.getActivities(activityFilterDTO)

        verify(activityService).getActivities(any())
    }

    private companion object {
        val startDate = LocalDate.of(2023, 4, 15)
        val endDate = LocalDate.of(2023, 4, 17)
        val activity =
            Activity(
                1,
                startDate.atStartOfDay(),
                endDate.atStartOfDay(),
                45,
                "New activity",
                createProjectRole(1L),
                1,
                false,
                null,
                null,
                false,
                approvalState = ApprovalState.PENDING
            )
    }
}