package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.converters.OrganizationResponseConverter
import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals

internal class ActivityRetrievalByIdUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val userService = mock<UserService>()

    private val activityRetrievalByIdUseCase =
        ActivityRetrievalByIdUseCase(
            activityService,
            ActivityResponseConverter(
                OrganizationResponseConverter(),
                ProjectResponseConverter(),
                ProjectRoleResponseConverter()
            )
        )

    @Test
    fun `return the activity`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(yesterdayActivity).whenever(activityService).getActivityById(2L)

        assertEquals(yesterdayActivityResponseDTO, activityRetrievalByIdUseCase.getActivityById(2L))
    }


    private companion object{
        private val USER = createUser()
        private val YESTERDAY = LocalDate.now().minusDays(1)
        private val ORGANIZATION_DTO = OrganizationResponseDTO(1L, "Dummy Organization")

        private val PROJECT_RESPONSE_DTO = ProjectResponseDTO(
            1L,
            "Dummy Project",
            open = true,
            billable = false
        )
        private val PROJECT_ROLE = ProjectRole(
            10L,
            "Dummy Project role",
            false,
            Project(
                1L,
                "Dummy Project",
                true,
                false,
                Organization(1L, "Dummy Organization", listOf()),
                listOf(),
            ),
            0
        )

        private val PROJECT_ROLE_RESPONSE_DTO = ProjectRoleResponseDTO(10L, "Dummy Project role", false)

        val yesterdayActivity = Activity(
            2L,
            YESTERDAY.atStartOfDay(),
            540,
            "Dummy description",
            PROJECT_ROLE,
            USER.id,
            true
        )

        val yesterdayActivityResponseDTO = ActivityResponseDTO(
            2L,
            YESTERDAY.atStartOfDay(),
            540,
            "Dummy description",
            PROJECT_ROLE_RESPONSE_DTO,
            USER.id,
            true,
            ORGANIZATION_DTO,
            PROJECT_RESPONSE_DTO,
            false
        )

    }
}
