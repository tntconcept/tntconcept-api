package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.LocalDate
import java.time.LocalTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

internal class LatestProjectRolesForAuthenticatedUserUseCaseTest {
    private val projectRoleRepository = mock<ProjectRoleRepository>()

    private val latestProjectRolesForAuthenticatedUserUseCase = LatestProjectRolesForAuthenticatedUserUseCase(projectRoleRepository)

    @Test
    fun `return the last imputed roles`() {
        val startDate = TODAY.minusMonths(1).atTime(LocalTime.MIN)
        val endDate = TODAY.atTime(23, 59, 59)

        doReturn(PROJECT_ROLES_RECENT).whenever(projectRoleRepository).findDistinctRolesBetweenDate(startDate, endDate)

        val expectedProjectRoles = listOf(
            buildProjectRoleRecent(1L, START_DATE),
            buildProjectRoleRecent(2L, START_DATE.minusDays(2)),
        )

        assertEquals(expectedProjectRoles, latestProjectRolesForAuthenticatedUserUseCase.get())
    }

    private companion object {
        private val TODAY = LocalDate.now()
        private val START_DATE = TODAY.minusDays(1)
        private val END_DATE = TODAY.minusDays(4)

        private fun buildProjectRoleRecent(id: Long, date: LocalDate, projectOpen: Boolean = true): ProjectRoleRecent {
            return ProjectRoleRecent(
                id = id,
                date = date.atTime(LocalTime.MIDNIGHT),
                name = "Role ID $id",
                projectBillable = false,
                projectOpen = projectOpen,
                projectName = "Project Name of role $id",
                organizationName = "Org Name of role $id",
                requireEvidence = false
            )
        }

        private val PROJECT_ROLES_RECENT = listOf(
            buildProjectRoleRecent(1L, START_DATE),
            buildProjectRoleRecent(2L, START_DATE.minusDays(2)),
            buildProjectRoleRecent(5L, START_DATE.minusDays(3), false),
            buildProjectRoleRecent(1L, END_DATE),
        )
    }
}
