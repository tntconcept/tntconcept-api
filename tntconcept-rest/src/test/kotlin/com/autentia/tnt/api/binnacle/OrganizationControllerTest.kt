package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.usecases.ImputableOrganizationsUseCase
import com.autentia.tnt.binnacle.usecases.ImputableProjectsByOrganizationIdUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

internal class OrganizationControllerTest {

    private val imputableOrganizationsUseCase = mock<ImputableOrganizationsUseCase>()
    private val imputableProjectsByOrganizationIdUseCase = mock<ImputableProjectsByOrganizationIdUseCase>()

    private val organizationController = OrganizationController(
        imputableOrganizationsUseCase,
        imputableProjectsByOrganizationIdUseCase,
        ProjectResponseConverter()
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
        val organizationId = 1L
        val project = Project(1, "Dummy Project", true, true, mock(Organization::class.java), listOf())

        doReturn(listOf(project)).whenever(imputableProjectsByOrganizationIdUseCase).get(organizationId)

        val result = organizationController.getOrganizationsProjects(organizationId)

        val expectedProjectDTO = ProjectResponseDTO(1, "Dummy Project", true, true)
        assertEquals(listOf(expectedProjectDTO), result)
    }

}
