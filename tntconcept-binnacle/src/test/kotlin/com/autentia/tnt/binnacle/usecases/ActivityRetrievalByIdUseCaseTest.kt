package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.services.ActivityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class ActivityRetrievalByIdUseCaseTest {

    private val activityService = mock<ActivityService>()

    private val activityRetrievalByIdUseCase =
        ActivityRetrievalByIdUseCase(
            activityService,
            ActivityResponseConverter(
                ActivityIntervalResponseConverter()
            )
        )

    @Test
    fun `return the activity`() {
        doReturn(yesterdayActivity).whenever(activityService).getActivityById(2L)

        assertEquals(yesterdayActivityResponseDTO, activityRetrievalByIdUseCase.getActivityById(2L))
    }


    private companion object{
        private val USER = createUser()
        private val YESTERDAY = LocalDate.now().minusDays(1)
        private val PROJECT_ROLE = ProjectRole(
            10L,
            "Dummy Project role",
            RequireEvidence.NO,
            Project(
                1L,
                "Dummy Project",
                true,
                false,
                Organization(1L, "Dummy Organization", listOf()),
                listOf(),
            ),
            0,
            true,
            false,
            TimeUnit.MINUTES
        )

        private val PROJECT_ROLE_RESPONSE_DTO = ProjectRoleUserDTO(
            10L,
            "Dummy Project role",
            PROJECT_ROLE.project.organization.id,
            PROJECT_ROLE.project.id,
            PROJECT_ROLE.maxAllowed,
            PROJECT_ROLE.maxAllowed,
            PROJECT_ROLE.timeUnit,
            PROJECT_ROLE.requireEvidence,
            PROJECT_ROLE.isApprovalRequired,
            USER.id
        )

        val yesterdayActivity = createDomainActivity(
            YESTERDAY.atStartOfDay(),
            YESTERDAY.atStartOfDay().plusMinutes(540L),
            540
        ).copy(projectRole = PROJECT_ROLE.toDomain())

        val yesterdayActivityResponseDTO = ActivityResponseDTO(
            true,
            "Description",
            false,
            1L,
            PROJECT_ROLE_RESPONSE_DTO.id,
            IntervalResponseDTO(
                YESTERDAY.atStartOfDay(),
                YESTERDAY.atStartOfDay().plusMinutes(540L), 540, PROJECT_ROLE.timeUnit
            ),
            USER.id,
            approvalState = ApprovalState.NA
        )

    }
}
