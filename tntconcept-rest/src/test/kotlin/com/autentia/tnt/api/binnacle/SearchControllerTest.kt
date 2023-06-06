package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
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
    fun `return empty lists for unknown role ids for year by default`() {

        val searchedRoles = listOf(UNKNOWN_ROLE_ID)
        val roleDescriptions = SearchResponseDTO(
            emptyList(),
            emptyList(),
            emptyList()
        )
        doReturn(roleDescriptions).whenever(searchUseCase).getDescriptions(searchedRoles, null)

        val result = searchController.searchBy(searchedRoles, null)

        assertEquals(0, result.organizations.size)
        assertEquals(0, result.projects.size)
        assertEquals(0, result.projectRoles.size)
    }

    @Test
    fun `return role, project and organization for one unique roleid for year by default`() {

        // Give a role ID
        val searchedRoles = listOf(TRAINING.id)
        val roleDescriptions = SearchResponseDTO(
            listOf(AUTENTIA),
            listOf(TRAINING),
            listOf(STUDENT)
        )
        doReturn(roleDescriptions).whenever(searchUseCase).getDescriptions(searchedRoles, null)

        // When search for its structure
        val result = searchController.searchBy(searchedRoles, null)

        // Get the ROLE, AUTENTIA FORMACION and AUTENTIA
        assertEquals(listOf(AUTENTIA), result.organizations)
        assertEquals(listOf(TRAINING), result.projects)
        assertEquals(listOf(STUDENT), result.projectRoles)
    }

    @Test
    fun `return role, project and organization for one unique roleid for specific year `() {

        // Give a role ID
        val searchedRoles = listOf(TRAINING.id)
        val roleDescriptions = SearchResponseDTO(
                listOf(AUTENTIA),
                listOf(TRAINING),
                listOf(STUDENT)
        )
        doReturn(roleDescriptions).whenever(searchUseCase).getDescriptions(searchedRoles, YEAR)

        // When search for its structure
        val result = searchController.searchBy(searchedRoles, YEAR)

        // Get the ROLE, AUTENTIA FORMACION and AUTENTIA
        assertEquals(listOf(AUTENTIA), result.organizations)
        assertEquals(listOf(TRAINING), result.projects)
        assertEquals(listOf(STUDENT), result.projectRoles)
    }


    private companion object {
        private const val UNKNOWN_ROLE_ID = -1L
        private const val YEAR = 2023

        private val AUTENTIA = OrganizationResponseDTO(1, "Autentia")
        private val TRAINING = ProjectResponseDTO(1, "Formación Autentia", true, false, AUTENTIA.id)
        private val STUDENT = ProjectRoleUserDTO(
            1,
            "Alumno en formación",
            AUTENTIA.id,
            TRAINING.id,
            1440,
            60,
            TimeUnit.MINUTES,
            RequireEvidence.WEEKLY,
            false,
            1L
        )
    }
}
