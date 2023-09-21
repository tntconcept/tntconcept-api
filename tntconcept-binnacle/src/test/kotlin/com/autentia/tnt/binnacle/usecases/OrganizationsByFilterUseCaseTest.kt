package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.OrganizationResponseConverter
import com.autentia.tnt.binnacle.core.domain.OrganizationType
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.OrganizationFilterDTO
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.repositories.OrganizationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class OrganizationsByFilterUseCaseTest {

    private val organizationRepository = mock<OrganizationRepository>()

    private val organizationsByFilterUseCase = OrganizationsByFilterUseCase(organizationRepository, OrganizationResponseConverter())

    @Test
    fun `return all organizations if no filters are applied`() {
        val expectedResult = ORGANIZATIONS.map { OrganizationResponseDTO(it.id, it.name) }
        val filter = OrganizationFilterDTO(listOf(), null)

        whenever(organizationRepository.findAll()).thenReturn(ORGANIZATIONS)

        val result = organizationsByFilterUseCase.get(filter)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `return only organizations of type if organization types filter is applied`() {
        val expectedResult = ORGANIZATIONS.map { OrganizationResponseDTO(it.id, it.name) }
        val filter = OrganizationFilterDTO(listOf(OrganizationType.PROVIDER), null)

        whenever(organizationRepository.findAll()).thenReturn(ORGANIZATIONS)

        val result = organizationsByFilterUseCase.get(filter)

        assertEquals(expectedResult, result)
    }

    private companion object {
        private fun buildProjectRole(id: Long): ProjectRole {
            return ProjectRole(id, "Project Role ID $id", RequireEvidence.NO, mock(Project::class.java), 0, 0, true, false, TimeUnit.MINUTES)
        }

        private fun buildProject(id: Long, open: Boolean, roles: List<ProjectRole>): Project {
            return Project(id, "Project ID $id", open, false, LocalDate.now(), null, null, mock(Organization::class.java), roles)
        }

        private val OPEN_PROJECT = buildProject(1, true, listOf(buildProjectRole(id = 1)))
        private val CLOSED_PROJECT = buildProject(2, false, listOf(buildProjectRole(id = 2)))

        private val ORGANIZATIONS = listOf(
            Organization(1, "Open/Closed projects", 1, listOf(OPEN_PROJECT, CLOSED_PROJECT)),
            Organization(2, "Open Project but without roles", 1, listOf(buildProject(3, true, listOf()))),
            Organization(3, "Closed Project", 1, listOf(buildProject(4, false, listOf(buildProjectRole(4)))))
        )
    }

}