package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.usecases.LatestProjectRolesForAuthenticatedUserUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByUserIdsUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ProjectRoleControllerTest {
    private val projectRoleByIdUseCase = mock<ProjectRoleByIdUseCase>()
    private val latestProjectRolesForAuthenticatedUserUseCase = mock<LatestProjectRolesForAuthenticatedUserUseCase>()
    private val projectRoleByUserIdsUseCase = mock<ProjectRoleByUserIdsUseCase>()

    private val projectRoleController = ProjectRoleController(
        projectRoleByIdUseCase,
        latestProjectRolesForAuthenticatedUserUseCase,
        projectRoleByUserIdsUseCase
    )

    @Test
    fun `find the project role by id`() {

        val roleId = 1
        val role = ProjectRoleDTO(
            1,
            "Dummy Project Role",
            1,
            1,
            10,
            true,
            TimeUnit.DAYS,
            RequireEvidence.WEEKLY,
            true,
        )

        doReturn(role).whenever(projectRoleByIdUseCase).get(roleId.toLong())

        val projectRoleResponseDTO = projectRoleController.getProjectRoleById(roleId.toLong())

        assertEquals(role, projectRoleResponseDTO)

    }

    @Test
    fun `find the project roles in the last 30 days`() {

        doReturn(listOf(PROJECT_ROLE_RECENT)).whenever(latestProjectRolesForAuthenticatedUserUseCase).get()

        val projectRolesRecentResponseDTO = projectRoleController.getLatestRoles()

        assertEquals(listOf(PROJECT_ROLE_USER_DTO), projectRolesRecentResponseDTO)
    }

    private companion object {

        private val PROJECT_ROLE_RECENT = ProjectRoleUserDTO(
            1L,
            "Dummy Project Role",
            1L,
            1L,
            10,
            5,
            TimeUnit.DAYS,
            RequireEvidence.NO,
            true,
            1L
        )

        private val PROJECT_ROLE_USER_DTO = ProjectRoleUserDTO(
            PROJECT_ROLE_RECENT.id,
            PROJECT_ROLE_RECENT.name,
            PROJECT_ROLE_RECENT.organizationId,
            PROJECT_ROLE_RECENT.projectId,
            PROJECT_ROLE_RECENT.maxAllowed,
            PROJECT_ROLE_RECENT.remaining,
            PROJECT_ROLE_RECENT.timeUnit,
            PROJECT_ROLE_RECENT.requireEvidence,
            PROJECT_ROLE_RECENT.requireApproval,
            PROJECT_ROLE_RECENT.userId
        )

    }

}
