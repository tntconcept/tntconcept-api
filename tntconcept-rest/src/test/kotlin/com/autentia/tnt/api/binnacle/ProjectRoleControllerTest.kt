package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.converters.ProjectRoleRecentConverter
import com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleRecentDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.usecases.LatestProjectRolesForAuthenticatedUserUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByUserIdsUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.Month

internal class ProjectRoleControllerTest {
    private val projectRoleByIdUseCase = mock<ProjectRoleByIdUseCase>()
    private val latestProjectRolesForAuthenticatedUserUseCase = mock<LatestProjectRolesForAuthenticatedUserUseCase>()
    private val projectRoleRecentConverter = ProjectRoleRecentConverter()
    private val projectRoleByUserIdsUseCase = mock<ProjectRoleByUserIdsUseCase>()

    private val projectRoleController = ProjectRoleController(
        projectRoleByIdUseCase,
        latestProjectRolesForAuthenticatedUserUseCase,
        projectRoleByUserIdsUseCase,
        projectRoleRecentConverter
    )

    @Test
    fun `find the project role by id`() {

        val roleId = 1
        val role = ProjectRoleDTO(
            1,
            "Dummy Project Role",
            RequireEvidence.WEEKLY,
        )

        doReturn(role).whenever(projectRoleByIdUseCase).get(roleId.toLong())

        val projectRoleResponseDTO = projectRoleController.getProjectRoleById(roleId.toLong())

        assertEquals(role, projectRoleResponseDTO)

    }

    @Test
    fun `find the project roles in the last 30 days`() {

        doReturn(listOf(PROJECT_ROLE_RECENT)).whenever(latestProjectRolesForAuthenticatedUserUseCase).get()

        val projectRolesRecentResponseDTO = projectRoleController.getLatestRoles()

        assertEquals(listOf(PROJECT_ROLE_RECENT_DTO), projectRolesRecentResponseDTO)
    }

    private companion object {

        private val PROJECT_ROLE_RECENT = ProjectRoleRecent(
            1L,
            "Dummy Project Role",
            "Dummy Project",
            "Dummy Organization",
            false,
            true,
            LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0),
            RequireEvidence.NO
        )

        private val PROJECT_ROLE_RECENT_DTO = ProjectRoleRecentDTO(
            PROJECT_ROLE_RECENT.id,
            PROJECT_ROLE_RECENT.name,
            PROJECT_ROLE_RECENT.projectName,
            PROJECT_ROLE_RECENT.organizationName,
            PROJECT_ROLE_RECENT.projectBillable,
            PROJECT_ROLE_RECENT.projectOpen,
            PROJECT_ROLE_RECENT.date,
            PROJECT_ROLE_RECENT.requireEvidence
        )

    }

}
