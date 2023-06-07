package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOpenSpecification
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOrganizationIdSpecification
import org.assertj.core.api.Assertions.assertThat
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
    fun `get filtered projects should return projects`() {
        val predicate = PredicateBuilder.and(ProjectOrganizationIdSpecification(1), ProjectOpenSpecification(true))
        whenever(projectRepository.findAll(predicate)).thenReturn(listOf(projectBlocked))

        val projects = projectService.getProjects(predicate)

        assertEquals(listOf(project), projects)
    }

    @Test
    fun `get by Id should return Project`() {
        whenever(projectRepository.findById(projectId)).thenReturn(Optional.of(projectBlocked))

        val result = projectService.findById(projectId)

        assertEquals(project, result)
    }

    @Test
    fun `throw ProjectNotFoundException when Id doesnt exists`() {
        whenever(projectRepository.findById(projectId)).thenReturn(Optional.empty())

        assertThrows<ProjectNotFoundException> {
            projectService.findById(projectId)
        }
    }

    @Test
    fun `block project when project does not exist should throw`() {
        whenever(projectRepository.findById(projectId)).thenReturn(Optional.empty())

        assertThrows<ProjectNotFoundException> {
            projectService.blockProject(projectId, LocalDate.now(), userId)
        }
    }

    @Test
    fun `block project should update project block values`() {
        whenever(projectRepository.findById(1)).thenReturn(Optional.of(projectNotBlocked))
        val expectedBlockedProjectEntity = projectNotBlocked.copy(blockedByUser = userId, blockDate = LocalDate.now())
        whenever(projectRepository.update(expectedBlockedProjectEntity)).thenReturn(expectedBlockedProjectEntity)

        val result = projectService.blockProject(projectId, LocalDate.now(), userId)

        val expectedBlockedProject = expectedBlockedProjectEntity.toDomain()
        assertThat(result).isEqualTo(expectedBlockedProject)
    }

    @Test
    fun `unblock project when project does not exist should throw`() {
        whenever(projectRepository.findById(projectId)).thenReturn(Optional.empty())

        assertThrows<ProjectNotFoundException> {
            projectService.unblockProject(projectId)
        }
    }

    @Test
    fun `unblock project should remove block values`() {
        whenever(projectRepository.findById(projectId)).thenReturn(Optional.of(projectBlocked))
        val expectedUnblockedProject = projectBlocked.copy(blockedByUser = null, blockDate = null)
        whenever(projectRepository.update(expectedUnblockedProject)).thenReturn(expectedUnblockedProject)

        val result = projectService.unblockProject(projectId)

        val expectedBlockedProject = expectedUnblockedProject.toDomain()
        assertThat(result).isEqualTo(expectedBlockedProject)
    }

    private companion object {
        private const val projectId = 1L
        private const val userId = 1L
        private val user = createDomainUser()
        private val projectNotBlocked = com.autentia.tnt.binnacle.entities.Project(
            1,
            "NotBlockedProject",
            open = true,
            billable = true,
            startDate = LocalDate.now(),
            blockDate = null,
            blockedByUser = null,
            Organization(1, "Organization", emptyList()),
            emptyList()
        )
        private val projectBlocked = com.autentia.tnt.binnacle.entities.Project(
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
        private val project = projectBlocked.toDomain()
    }
}