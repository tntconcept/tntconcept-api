package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@MicronautTest
@TestInstance(PER_CLASS)
internal class ProjectRoleRepositoryIT {

    @Inject
    private lateinit var projectRoleRepository: ProjectRoleRepository

    @Test
    fun `should get the roles by project id`() {
        val projectId = 1L

        val result = projectRoleRepository.getAllByProjectId(projectId)

        assertEquals("vacaciones", result[0].name)
    }

    @Test
    fun `should get the roles by project ids`() {
        val projectRoleIds = listOf(1L, 2L)

        val result = projectRoleRepository.getAllByIdIn(projectRoleIds)

        assertEquals(projectRoleIds.size, result.size)
        checkProjectRole(result[0], projectRoleIds[0], "vacaciones", 1L)
        checkProjectRole(result[1], projectRoleIds[1], "permiso", 2L)

    }

    private fun checkProjectRole(projectRole: ProjectRole, expectedProjectRoleId: Long, expectedProjectRoleName: String, expectedProjectId: Long) {
        assertEquals(projectRole.id, expectedProjectRoleId)
        assertEquals(projectRole.project.id, expectedProjectId)
        assertEquals(projectRole.name, expectedProjectRoleName)
    }

    @Test
    fun `should get project role with required evidence`() {
        val projectId = 1L

        val result = projectRoleRepository.getAllByProjectId(projectId)

        assertTrue(result.isNotEmpty())
    }

}
