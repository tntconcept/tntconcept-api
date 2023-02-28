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
internal class ProjectRepositoryIT {

    @Inject
    private lateinit var projectRepository: ProjectRepository

    @Test
    fun `should find the projects by organization id`() {
        val organizationId = 1L
        val result = projectRepository.findAllByOrganizationId(organizationId)

        assertTrue(result.isNotEmpty())
        assertEquals("Vacaciones", result[0].name)
    }

}
