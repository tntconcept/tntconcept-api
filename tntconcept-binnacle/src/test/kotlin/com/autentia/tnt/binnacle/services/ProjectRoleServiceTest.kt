package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ProjectRoleServiceTest {

    private val projectRoleRepository = mock<ProjectRoleRepository>()

    private val projectRoleService = ProjectRoleService(projectRoleRepository)


    @Test
    fun `getAllByProjectId should call repository`() {
        val projectRoles = listOf(
            createProjectRole(),
            createProjectRole().copy(id = 5L).copy(isWorkingTime = false),
        )
        val expectedResult = projectRoles.map(ProjectRole::toDomain)

        whenever(projectRoleRepository.getAllByProjectId(1L)).thenReturn(projectRoles)

        val result = projectRoleService.getAllByProjectId(1L)

        assertEquals(expectedResult, result)
    }

}
