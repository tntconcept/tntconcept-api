package com.autentia.tnt.binnacle.repositories

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
        val projectIds = listOf(1L, 2L)

        val result = projectRoleRepository.getAllByProjectIdIn(projectIds)

        assertEquals(projectIds.size, result.size)
        assertTrue(result.map { it.id }.containsAll(projectIds))
    }

    @Test
    fun `should get project role with required evidence`() {
        val projectId = 1L

        val result = projectRoleRepository.getAllByProjectId(projectId)

        assertTrue(result.isNotEmpty())
    }

}
