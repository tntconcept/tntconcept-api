package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime

internal class LatestProjectRolesForAuthenticatedUserUseCaseTest {

    private val userService = mock<UserService>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityService = mock<ActivityService>()
    private val projectRoleResponseConverter= ProjectRoleResponseConverter(activityService)

    private val latestProjectRolesForAuthenticatedUserUseCase = LatestProjectRolesForAuthenticatedUserUseCase(userService, projectRoleRepository, projectRoleResponseConverter)

    @Test
    fun `return the last imputed roles`() {
        val startDate = TODAY.minusMonths(1).atTime(LocalTime.MIN)
        val endDate = TODAY.atTime(23, 59, 59)

        whenever(userService.getAuthenticatedUser()).thenReturn(USER)
        whenever(projectRoleRepository.findDistinctRolesBetweenDate(startDate, endDate, USER.id)).thenReturn(
            PROJECT_ROLES_RECENT)

        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(1L),
            buildProjectRoleUserDTO(2L),
        )

        val result = latestProjectRolesForAuthenticatedUserUseCase.get()

        assertEquals(expectedProjectRoles, result)
    }

    private companion object{
        private val TODAY = LocalDate.now()
        private val USER = createUser()
        private val START_DATE = TODAY.minusDays(1)
        private val END_DATE = TODAY.minusDays(4)

        private fun buildProjectRoleRecent(id: Long, date: LocalDate, projectOpen: Boolean = true): ProjectRoleRecent = ProjectRoleRecent(
                id = id,
                date = date.atTime(LocalTime.MIDNIGHT),
                name = "Role ID $id",
                projectId = 1L,
                organizationId = 1L,
                projectOpen = projectOpen,
                maxAllowed = 0,
                timeUnit = TimeUnit.MINUTES,
                requireEvidence = RequireEvidence.WEEKLY,
                requireApproval = false,
                userId = USER.id
            )

        private fun buildProjectRoleUserDTO(id: Long): ProjectRoleUserDTO = ProjectRoleUserDTO(
            id = id,
            name = "Role ID $id",
            projectId = 1L,
            organizationId = 1L,
            maxAllowed = 0,
            remaining = 0,
            timeUnit = TimeUnit.MINUTES,
            requireEvidence = RequireEvidence.WEEKLY,
            requireApproval = false,
            userId = USER.id
        )


        private val PROJECT_ROLES_RECENT = listOf(
            buildProjectRoleRecent(1L, START_DATE),
            buildProjectRoleRecent(2L, START_DATE.minusDays(2)),
            buildProjectRoleRecent(5L, START_DATE.minusDays(3), false),
            buildProjectRoleRecent(1L, END_DATE),
        )

    }

}
