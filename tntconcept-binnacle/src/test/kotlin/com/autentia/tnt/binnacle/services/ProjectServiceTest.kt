package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.*

internal class ProjectServiceTest {
    private val projectRepository = mock<ProjectRepository>()

    private val projectService = ProjectService(projectRepository)

    @Test
    fun `get by Id should return Project`() {
        val expected = project.toDomain()
        whenever(projectRepository.findById(1)).thenReturn(Optional.of(project))

        val result = projectService.findById(1)

        assertEquals(expected, result)
    }

    @Test
    fun `throw ProjectNotFoundException when Id doesnt exists`() {
        whenever(projectRepository.findById(1)).thenReturn(Optional.empty())

        assertThrows<ProjectNotFoundException> {
            projectService.findById(1)
        }
    }

    private companion object {
        private val user = createDomainUser()
        private val project = Project(
            1,
            "BlockedProject",
            true,
            true,
            LocalDate.now(),
            LocalDate.now(),
            user.id,
            Organization(1, "Organization", emptyList()),
            emptyList()
        )
    }
}