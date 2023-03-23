package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

import java.time.LocalDate
import java.util.*

internal class ActivitiesBetweenDateUseCaseTest {

    private val user = createUser()
    private val activityService = mock<ActivityService>()
    private val userService = mock<UserService>()
    private val activityDateConverter = mock<ActivityResponseConverter>()

    private val activitiesBetweenDateUseCase = ActivitiesBetweenDateUseCase(
        activityService,
        userService,
        activityDateConverter
    )

    @Test
    fun `get activities between start and end date`() {

        doReturn(user).whenever(userService).getAuthenticatedUser()
        doReturn(listOf(activity)).whenever(activityService).getActivitiesBetweenDates(any(), any())

        doReturn(activitiesResponseDTO).whenever(activityDateConverter).mapActivitiesToActivitiesDateDTO(listOf(activity))

        val actual = activitiesBetweenDateUseCase.getActivities(Optional.of(startDate), Optional.of(endDate), Optional.empty())
        assertEquals(activitiesResponseDTO, actual)

    }

    @Test
    fun `get activities by ApprovalState`() {

        doReturn(user).whenever(userService).getAuthenticatedUser()
        doReturn(listOf(activity)).whenever(activityService).getActivitiesApprovalState(approvalState, user.id)

        doReturn(activitiesResponseDTO).whenever(activityDateConverter).mapActivitiesToActivitiesDateDTO(listOf(activity))

        val actual = activitiesBetweenDateUseCase.getActivities(Optional.empty(), Optional.empty(), Optional.of(approvalState))
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