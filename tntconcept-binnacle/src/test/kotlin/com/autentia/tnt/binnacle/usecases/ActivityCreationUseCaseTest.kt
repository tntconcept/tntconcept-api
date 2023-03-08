package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.converters.OrganizationResponseConverter
import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Optional

internal class ActivityCreationUseCaseTest {

    private val user = createUser()
    private val activityService = mock<ActivityService>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityRepository = mock<ActivityRepository>()
    private val activityValidator = ActivityValidator(activityRepository, projectRoleRepository)
    private val userService = mock<UserService>()

    private val activityCreationUseCase = ActivityCreationUseCase(
        activityService,
        userService,
        activityValidator,
        ActivityRequestBodyConverter(),
        ActivityResponseConverter(
            OrganizationResponseConverter(),
            ProjectResponseConverter(),
            ProjectRoleResponseConverter()
        )
    )

    @Test
    fun `created activity`() {
        doReturn(user).whenever(userService).getAuthenticatedUser()

        val activity = createActivity(userId = user.id)

        val expectedResponseDTO = createActivityResponseDTO(userId = user.id)

        doReturn(activity).whenever(activityService).createActivity(any(), eq(user))

        doReturn(Optional.of(activity.projectRole)).whenever(projectRoleRepository).findById(any())

        val result = activityCreationUseCase.createActivity(ACTIVITY_REQUEST_BODY_DTO)

        assertEquals(expectedResponseDTO, result)
    }

    private companion object {
        private val TIME_NOW = LocalDateTime.now()

        private val ORGANIZATION = Organization(1L, "Dummy Organization", listOf())
        private val ORGANIZATION_DTO = OrganizationResponseDTO(1L, "Dummy Organization")

        private val PROJECT = Project(
            1L,
            "Dummy Project",
            open = true,
            billable = false,
            ORGANIZATION,
            listOf()
        )
        private val PROJECT_RESPONSE_DTO = ProjectResponseDTO(
            1L,
            "Dummy Project",
            open = true,
            billable = false,
        )
        private val PROJECT_ROLE = ProjectRole(10L, "Dummy Project role", false, PROJECT, 0)

        private val PROJECT_ROLE_RESPONSE_DTO = ProjectRoleResponseDTO(10L, "Dummy Project role", false)

        private val ACTIVITY_REQUEST_BODY_DTO = ActivityRequestBodyDTO(
            null,
            TIME_NOW,
            75,
            "New activity",
            false,
            PROJECT_ROLE.id,
            false
        )

        private fun generateLargeDescription(mainMessage: String): String {
            var description = mainMessage
            for (i in 1..2048) {
                description += "A"
            }
            return description
        }

        private fun createActivity(
            id: Long = 1L,
            userId: Long = 1L,
            description: String = generateLargeDescription("New activity").substring(0, 2048),
            startDate: LocalDateTime = TIME_NOW,
            duration: Int = 75,
            billable: Boolean = false,
            hasImage: Boolean = false,
            projectRole: ProjectRole = PROJECT_ROLE
        ): Activity =
            Activity(
                id = id,
                userId = userId,
                description = description,
                startDate = startDate,
                duration = duration,
                billable = billable,
                hasEvidences = hasImage,
                projectRole = projectRole
            )

        private fun createActivityResponseDTO(
            id: Long = 1L,
            userId: Long = 0L,
            description: String = generateLargeDescription("New activity").substring(0, 2048),
            startDate: LocalDateTime = TIME_NOW,
            duration: Int = 75,
            billable: Boolean = false,
            hasImage: Boolean = false,
            projectRole: ProjectRoleResponseDTO = PROJECT_ROLE_RESPONSE_DTO,
            organization: OrganizationResponseDTO = ORGANIZATION_DTO,
            project: ProjectResponseDTO = PROJECT_RESPONSE_DTO
        ): ActivityResponseDTO =
            ActivityResponseDTO(
                id,
                startDate,
                duration,
                description,
                projectRole,
                userId,
                billable,
                organization,
                project,
                hasImage,
            )

    }

}
