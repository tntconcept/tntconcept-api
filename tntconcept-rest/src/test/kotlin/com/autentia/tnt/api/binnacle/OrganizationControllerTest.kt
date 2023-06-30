package com.autentia.tnt.api.binnacle

import com.autentia.tnt.api.binnacle.organization.OrganizationController
import com.autentia.tnt.api.binnacle.project.ProjectResponse
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.usecases.ImputableOrganizationsUseCase
import com.autentia.tnt.binnacle.usecases.ImputableProjectsByOrganizationIdUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class OrganizationControllerTest {

    private val imputableOrganizationsUseCase = mock<ImputableOrganizationsUseCase>()
    private val imputableProjectsByOrganizationIdUseCase = mock<ImputableProjectsByOrganizationIdUseCase>()

    private val organizationController = OrganizationController(
        imputableOrganizationsUseCase,
        imputableProjectsByOrganizationIdUseCase,
    )

    @Test
    fun `return all imputable organizations`() {
        val organization = OrganizationResponseDTO(1, "Dummy Organization")

        doReturn(listOf(organization)).whenever(imputableOrganizationsUseCase).get()

        val result = organizationController.getAllOrganizations()

        assertEquals(1, result.count())
        assertEquals(organization.id, result[0].id)
        assertEquals(organization.name, result[0].name)
    }

    @Test
    fun `return all projects by organization`() {
        val organization = createOrganization()
        val project = ProjectResponseDTO(1, "Dummy Project", true, true, 1L, startDate = LocalDate.now())

        doReturn(listOf(project)).whenever(imputableProjectsByOrganizationIdUseCase).get(organization.id)

        val result = organizationController.getOrganizationsProjects(organization.id)

        val expectedProjectDTO = ProjectResponse(1, "Dummy Project", true, true, 1L, startDate = LocalDate.now())
        assertEquals(listOf(expectedProjectDTO), result)
    }

}
