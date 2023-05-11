package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ProjectRoleServiceTest {

    private val projectRoleRepository = mock<ProjectRoleRepository>()

    private val projectRoleService = ProjectRoleService(projectRoleRepository)

    @Test
    fun `return the expected project roles`() {
        val projectRoles = listOf(createProjectRole())
        val ids = listOf(1L)

        doReturn(projectRoles).whenever(projectRoleRepository).getAllByIdIn(ids)

        val actual = projectRoleService.getAllByIds(ids)

        assertEquals(projectRoles.map { it.toDomain() }, actual)
    }

    @Test
    fun `return the expected project roles by list of project ids`() {
        val projectRoles = listOf(mock<ProjectRole>())
        val projectIds = listOf(1, 2)

        doReturn(projectRoles).whenever(projectRoleRepository).getAllByProjectIdIn(projectIds.map(Int::toLong))

        val actual = projectRoleService.getAllByProjectIds(projectIds)

        assertEquals(projectRoles, actual)
    }

}
