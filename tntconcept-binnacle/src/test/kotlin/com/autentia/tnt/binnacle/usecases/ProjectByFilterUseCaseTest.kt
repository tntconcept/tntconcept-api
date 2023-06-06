package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.dto.ProjectFilterDTO
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOpenSpecification
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOrganizationIdSpecification
import com.autentia.tnt.binnacle.services.ProjectService
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

class ProjectByFilterUseCaseTest {

    private val projectService = mock<ProjectService>()
    private val projectResponseConverter = ProjectResponseConverter()
    private val projectByFilterUseCase = ProjectByFilterUseCase(projectService, projectResponseConverter)

    @Test
    fun `should return filtered projects with implicit open=true filter`() {
        whenever(projectService.getProjects(implicitOpenProjectPredicate)).thenReturn(listOf(project.toDomain()))

        val result = projectByFilterUseCase.getProjects(implicitOpenProjectFilter)

        val expectedResult = listOf(projectResponseConverter.toProjectResponseDTO(project.toDomain()))
        assertEquals(expectedResult, result)
    }

    @Test
    fun `should return filtered projects with explicit open=false filter`() {
        val closedProject = project.copy(open = false)
        whenever(projectService.getProjects(explicitOpenProjectPredicate)).thenReturn(listOf(closedProject.toDomain()))

        val result = projectByFilterUseCase.getProjects(explicitOpenProjectFilter)

        val expectedResult = listOf(projectResponseConverter.toProjectResponseDTO(closedProject.toDomain()))
        assertEquals(expectedResult, result)
    }

    private companion object {
        private const val organizationId = 1L
        private val project = Project(
            1,
            "BlockedProject",
            true,
            true,
            LocalDate.now(),
            null,
            null,
            Organization(1, "Organization", emptyList()),
            emptyList()
        )
        private val implicitOpenProjectFilter = ProjectFilterDTO(
            organizationId,
        )
        private val implicitOpenProjectPredicate = PredicateBuilder.and(
            ProjectOrganizationIdSpecification(organizationId),
            ProjectOpenSpecification(true)
        )
        private val explicitOpenProjectFilter = ProjectFilterDTO(
            organizationId,
            false,
        )
        private val explicitOpenProjectPredicate = PredicateBuilder.and(
            ProjectOrganizationIdSpecification(organizationId),
            ProjectOpenSpecification(false)
        )
    }
}