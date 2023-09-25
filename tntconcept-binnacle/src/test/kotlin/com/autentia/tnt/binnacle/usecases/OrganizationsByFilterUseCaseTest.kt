package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.OrganizationResponseConverter
import com.autentia.tnt.binnacle.core.domain.OrganizationType
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.OrganizationFilterDTO
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.repositories.OrganizationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class OrganizationsByFilterUseCaseTest {

    private val organizationRepository = mock<OrganizationRepository>()

    private val organizationsByFilterUseCase =
        OrganizationsByFilterUseCase(organizationRepository, OrganizationResponseConverter())

    @Test
    fun `return all organizations if no filters are applied`() {
        val expectedResult = ORGANIZATIONS.map { OrganizationResponseDTO(it.id, it.name) }
        val filter = OrganizationFilterDTO(listOf(), null)

        whenever(organizationRepository.findAll()).thenReturn(ORGANIZATIONS)

        val result = organizationsByFilterUseCase.get(filter)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `return only provider organizations of type if organization types filter is applied`() {
        val organizationResponseDTOS = ORGANIZATIONS.map { OrganizationResponseDTO(it.id, it.name) }
        val filter = OrganizationFilterDTO(listOf(OrganizationType.PROVIDER), null)

        whenever(organizationRepository.findAll()).thenReturn(ORGANIZATIONS)

        val result = organizationsByFilterUseCase.get(filter)

        assertEquals(2, result.size)
        assertEquals(organizationResponseDTOS[1], result[0])
        assertEquals(organizationResponseDTOS[2], result[1])
    }

    @Test
    fun `return only client organizations of type if organization types filter is applied`() {
        val organizationResponseDTOS = ORGANIZATIONS.map { OrganizationResponseDTO(it.id, it.name) }
        val filter = OrganizationFilterDTO(listOf(OrganizationType.CLIENT), null)

        whenever(organizationRepository.findAll()).thenReturn(ORGANIZATIONS)

        val result = organizationsByFilterUseCase.get(filter)

        assertEquals(2, result.size)
        assertEquals(organizationResponseDTOS[0], result[0])
        assertEquals(organizationResponseDTOS[1], result[1])
    }

    @Test
    fun `return only prospect organizations of type if organization types filter is applied`() {
        val organizationResponseDTOS = ORGANIZATIONS.map { OrganizationResponseDTO(it.id, it.name) }
        val filter = OrganizationFilterDTO(listOf(OrganizationType.PROSPECT), null)

        whenever(organizationRepository.findAll()).thenReturn(ORGANIZATIONS)

        val result = organizationsByFilterUseCase.get(filter)

        assertEquals(2, result.size)
        assertEquals(organizationResponseDTOS[3], result[0])
        assertEquals(organizationResponseDTOS[4], result[1])
    }

    @Test
    fun `return prospect and client organizations if organization types filter is applied`() {
        val organizationResponseDTOS = ORGANIZATIONS.map { OrganizationResponseDTO(it.id, it.name) }
        val filter = OrganizationFilterDTO(listOf(OrganizationType.CLIENT, OrganizationType.PROVIDER), null)

        whenever(organizationRepository.findAll()).thenReturn(ORGANIZATIONS)

        val result = organizationsByFilterUseCase.get(filter)

        assertEquals(3, result.size)
        assertEquals(organizationResponseDTOS[0], result[0])
        assertEquals(organizationResponseDTOS[1], result[1])
        assertEquals(organizationResponseDTOS[2], result[2])
    }

    @Test
    fun `return only imputable organizations`(){
        val organizationResponseDTOS = ORGANIZATIONS.map { OrganizationResponseDTO(it.id, it.name) }
        val filter = OrganizationFilterDTO(listOf(), true)

        whenever(organizationRepository.findAll()).thenReturn(ORGANIZATIONS)

        val result = organizationsByFilterUseCase.get(filter)

        assertEquals(2, result.size)
        assertEquals(organizationResponseDTOS[0], result[0])
        assertEquals(organizationResponseDTOS[4], result[1])
    }

    @Test
    fun `return not imputable organizations`(){
        val organizationResponseDTOS = ORGANIZATIONS.map { OrganizationResponseDTO(it.id, it.name) }
        val filter = OrganizationFilterDTO(listOf(), false)

        whenever(organizationRepository.findAll()).thenReturn(ORGANIZATIONS)

        val result = organizationsByFilterUseCase.get(filter)

        assertEquals(3, result.size)
        assertEquals(organizationResponseDTOS[1], result[0])
        assertEquals(organizationResponseDTOS[2], result[1])
        assertEquals(organizationResponseDTOS[3], result[2])
    }

    @Test
    fun `return only imputable prospect and client organizations`(){
        val organizationResponseDTOS = ORGANIZATIONS.map { OrganizationResponseDTO(it.id, it.name) }
        val filter = OrganizationFilterDTO(listOf(OrganizationType.CLIENT, OrganizationType.PROSPECT), true)

        whenever(organizationRepository.findAll()).thenReturn(ORGANIZATIONS)

        val result = organizationsByFilterUseCase.get(filter)
        assertEquals(2, result.size)
        assertEquals(organizationResponseDTOS[0], result[0])
        assertEquals(organizationResponseDTOS[4], result[1])
    }

    private companion object {
        private const val CLIENT_ORGANIZATION_TYPE_ID = 1L
        private const val PROVIDER_ORGANIZATION_TYPE_ID = 2L
        private const val CLIENT_PROVIDER_ORGANIZATION_TYPE_ID = 3L
        private const val PROSPECT_ORGANIZATION_TYPE_ID = 4L


        private fun buildProjectRole(id: Long): ProjectRole {
            return ProjectRole(
                id,
                "Project Role ID $id",
                RequireEvidence.NO,
                mock(Project::class.java),
                0,
                0,
                true,
                false,
                TimeUnit.MINUTES
            )
        }

        private fun buildProject(id: Long, open: Boolean, roles: List<ProjectRole>): Project {
            return Project(
                id,
                "Project ID $id",
                open,
                false,
                LocalDate.now(),
                null,
                null,
                mock(Organization::class.java),
                roles
            )
        }

        private val OPEN_PROJECT = buildProject(1, true, listOf(buildProjectRole(id = 1)))
        private val CLOSED_PROJECT = buildProject(2, false, listOf(buildProjectRole(id = 2)))

        private val ORGANIZATIONS = listOf(
            Organization(1, "Open/Closed projects", CLIENT_ORGANIZATION_TYPE_ID, listOf(OPEN_PROJECT, CLOSED_PROJECT)),
            Organization(2, "Open Project but without roles", CLIENT_PROVIDER_ORGANIZATION_TYPE_ID, listOf(buildProject(3, true, listOf()))),
            Organization(3, "Closed Project", PROVIDER_ORGANIZATION_TYPE_ID, listOf(buildProject(4, false, listOf(buildProjectRole(4))))),
            Organization(4, "Closed Project", PROSPECT_ORGANIZATION_TYPE_ID, listOf(buildProject(4, false, listOf(buildProjectRole(4))))),
            Organization(5, "Open/Closed projects", PROSPECT_ORGANIZATION_TYPE_ID, listOf(OPEN_PROJECT, CLOSED_PROJECT)),
        )
    }

}