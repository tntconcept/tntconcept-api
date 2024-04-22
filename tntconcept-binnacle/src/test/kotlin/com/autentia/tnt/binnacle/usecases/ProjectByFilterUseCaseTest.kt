package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.dto.ProjectFilterDTO
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOpenSpecification
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOrganizationIdSpecification
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOrganizationIdsSpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

class ProjectByFilterUseCaseTest {

    private val projectRepository = mock<ProjectRepository>()
    private val projectResponseConverter = ProjectResponseConverter()
    private val projectByFilterUseCase = ProjectByFilterUseCase(projectRepository, projectResponseConverter)

    @Test
    fun `should return filtered projects with implicit open=true filter`() {
        whenever(projectRepository.findAll(organizationIdProjectPredicate)).thenReturn(listOf(project))

        val result = projectByFilterUseCase.getProjects(organizationIdProjectFilter)

        val expectedResult = listOf(projectResponseConverter.toProjectResponseDTO(project.toDomain()))
        assertEquals(expectedResult, result)
    }

    @Test
    fun `should return filtered projects with explicit open=false filter`() {
        val closedProject = project.copy(open = false)
        whenever(projectRepository.findAll(explicitOpenProjectPredicate)).thenReturn(listOf(closedProject))

        val result = projectByFilterUseCase.getProjects(explicitOpenProjectFilter)

        val expectedResult = listOf(projectResponseConverter.toProjectResponseDTO(closedProject.toDomain()))
        assertEquals(expectedResult, result)
    }

    @Test
    fun `should return filtered projects without organizationId filter`() {
        val closedProject = project.copy(open = false)
        whenever(projectRepository.findAll(organizationsAndClosedProjectPredicate)).thenReturn(listOf(closedProject))

        val result = projectByFilterUseCase.getProjects(organizationsAndClosedProjectFilter)

        val expectedResult = listOf(projectResponseConverter.toProjectResponseDTO(closedProject.toDomain()))
        assertEquals(expectedResult, result)
    }

    private companion object {
        private const val organizationId = 1L
        private const val otherOrganizationId = 1L

        private val project = Project(
            1,
            "BlockedProject",
            true,
            true,
            LocalDate.now(),
            null,
            null,
            Organization(1, "Organization", 1, emptyList()),
            emptyList(),
            "CLOSED_PRICE"
        )
        private val organizationIdProjectFilter = ProjectFilterDTO(
            organizationId,
            listOf()
        )
        private val organizationIdProjectPredicate = ProjectOrganizationIdSpecification(organizationId)

        private val explicitOpenProjectFilter = ProjectFilterDTO(
            organizationId,
            listOf(),
            false,
        )
        private val organizationsAndClosedProjectFilter = ProjectFilterDTO(
            null,
            listOf(organizationId, otherOrganizationId),
            false,
        )
        private val explicitOpenProjectPredicate = PredicateBuilder.and(
            ProjectOrganizationIdSpecification(organizationId),
            ProjectOpenSpecification(false)
        )
        private val organizationsAndClosedProjectPredicate = PredicateBuilder.and(
            ProjectOrganizationIdsSpecification(listOf(organizationId, otherOrganizationId)),
            ProjectOpenSpecification(false)
        )
    }
}