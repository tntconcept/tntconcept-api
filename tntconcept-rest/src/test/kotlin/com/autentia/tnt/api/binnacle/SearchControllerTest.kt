package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.OrganizationDescriptionDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectDescriptionDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDescriptionDTO
import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO
import com.autentia.tnt.binnacle.usecases.SearchByRoleIdUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

internal class SearchControllerTest {

    private val searchUseCase = mock<SearchByRoleIdUseCase>()

    private val searchController = SearchController(searchUseCase)

    @Test
    fun `return empty lists for unknown role ids`() {

        val searchedRoles = listOf(UNKNOWN_ROLE_ID)
        val roleDescriptions = SearchResponseDTO(
            emptyList(),
            emptyList(),
            emptyList()
        )
        doReturn(roleDescriptions).whenever(searchUseCase).getDescriptions(searchedRoles)

        val result = searchController.searchBy(searchedRoles)

        assertEquals(0, result.organizations.size)
        assertEquals(0, result.projects.size)
        assertEquals(0, result.projectRoles.size)
    }

    @Test
    fun `return role, project and organization for one unique roleid`() {

        // Give a role ID
        val searchedRoles = listOf(TRAINING.id)
        val roleDescriptions = SearchResponseDTO(
            listOf(AUTENTIA),
            listOf(TRAINING),
            listOf(STUDENT)
        )
        doReturn(roleDescriptions).whenever(searchUseCase).getDescriptions(searchedRoles)

        // When search for its structure
        val result = searchController.searchBy(searchedRoles)

        // Get the ROLE, AUTENTIA FORMACION and AUTENTIA
        assertEquals(listOf(AUTENTIA), result.organizations)
        assertEquals(listOf(TRAINING), result.projects)
        assertEquals(listOf(STUDENT), result.projectRoles)
    }

    private companion object {
        private val UNKNOWN_ROLE_ID = -1L

        private val AUTENTIA = OrganizationDescriptionDTO(1, "Autentia")
        private val TRAINING = ProjectDescriptionDTO(1, "Formación Autentia", true, false, AUTENTIA.id)
        private val STUDENT = ProjectRoleDescriptionDTO(1, "Alumno en formación", TRAINING.id)
    }
}
