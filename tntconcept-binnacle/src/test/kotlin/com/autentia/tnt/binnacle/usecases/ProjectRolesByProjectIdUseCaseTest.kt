package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ProjectRolesByProjectIdUseCaseTest {

    private val projectRoleRepository = mock<ProjectRoleRepository>()

    private val projectRolesByProjectIdUseCase = ProjectRolesByProjectIdUseCase(projectRoleRepository, ProjectRoleResponseConverter())

    @Test
    fun `return the expected project role`() {

        doReturn(listOf(PROJECT_ROLE)).whenever(projectRoleRepository).getAllByProjectId(PROJECT_ID)

        assertEquals(listOf(ProjectRoleResponseDTO( PROJECT_ID, "Dummy Role", false)), projectRolesByProjectIdUseCase.get(
            PROJECT_ID.toInt()))
    }

    private companion object{
        private const val PROJECT_ID = 1L

        private val ORGANIZATION = Organization(1L, "Nuestra empresa", listOf())
        private val PROJECT = Project(1L, "Dummy project", true,  false, ORGANIZATION, listOf())

        private val PROJECT_ROLE = ProjectRole(PROJECT_ID, "Dummy Role", false, PROJECT, 0)


    }
}
