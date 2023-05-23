package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.ProjectRole
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@MicronautTest
@TestInstance(PER_CLASS)
internal class ProjectRoleDaoIT {

    @Inject
    private lateinit var projectRoleDao: ProjectRoleDao

    @Test
    fun `should get the roles by project id`() {
        val projectId = 1L

        val result = projectRoleDao.getAllByProjectId(projectId)

        assertEquals("vacaciones", result[0].name)
    }

    @Test
    fun `should get the roles by project ids in`() {
        val projectIds = listOf(1L, 3L)

        val result = projectRoleDao.getAllByProjectIdIn(projectIds)

        assertEquals("vacaciones", result[0].name)
        checkProjectRole(result[0], 1L, "vacaciones", projectIds[0])
        checkProjectRole(result[1], 3L, "baja", projectIds[1])
    }

    @Test
    fun `should get the roles by project ids`() {
        val projectRoleIds = listOf(1L, 2L)

        val result = projectRoleDao.getAllByIdIn(projectRoleIds)

        assertEquals(projectRoleIds.size, result.size)
        checkProjectRole(result[0], projectRoleIds[0], "vacaciones", 1L)
        checkProjectRole(result[1], projectRoleIds[1], "permiso", 2L)
    }

    private fun checkProjectRole(
        projectRole: ProjectRole, expectedProjectRoleId: Long, expectedProjectRoleName: String, expectedProjectId: Long
    ) {
        assertEquals(projectRole.id, expectedProjectRoleId)
        assertEquals(projectRole.project.id, expectedProjectId)
        assertEquals(projectRole.name, expectedProjectRoleName)
    }

    @Test
    fun `should get project role with required evidence`() {
        val projectId = 1L

        val result = projectRoleDao.getAllByProjectId(projectId)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `should get project roles with is is working time to false`() {
        // Expected identifiers defined in R__test.sql
        val expectedIdentifiers = listOf(11L, 12L)

        val result = projectRoleDao.findAllByIsWorkingTimeFalse()

        assertThat(result).isNotEmpty()
        assertThat(result.map { it.id }).containsExactlyInAnyOrderElementsOf(expectedIdentifiers)
    }

}
