package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.entities.Organization
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
        whenever(projectRepository.findById(projectId)).thenReturn(Optional.of(projectBlocked))

        val result = projectService.findById(projectId)

        assertEquals(project, result)
    }

    @Test
    fun `throw ProjectNotFoundException when Id doesn't exist`() {
        whenever(projectRepository.findById(projectId)).thenReturn(Optional.empty())

        assertThrows<ProjectNotFoundException> {
            projectService.findById(projectId)
        }
    }

    private companion object {
        private const val projectId = 1L
        private val user = createDomainUser()
        private val projectBlocked = com.autentia.tnt.binnacle.entities.Project(
            1,
            "BlockedProject",
            true,
            LocalDate.now(),
            LocalDate.now(),
            user.id,
            Organization(1, "Organization", 1, emptyList()),
            emptyList(),
            "CLOSED_PRICE"
        )
        private val project = projectBlocked.toDomain()
    }
}