package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.usecases.ProjectByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRolesByProjectIdUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.doThrow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ProjectControllerTest {

    private val projectRolesByProjectIdUseCase = mock<ProjectRolesByProjectIdUseCase>()
    private val projectByIdUseCase = mock<ProjectByIdUseCase>()

    private val projectController = ProjectController(projectByIdUseCase, projectRolesByProjectIdUseCase)

    @Test
    fun `return the correct project`() {
        //Given
        val projectId = 1L
        val projectDTO = createProjectResponseDTO()

        doReturn(projectDTO).whenever(projectByIdUseCase).get(projectId)

        //When
        val actualProjectDTO = projectController.getProjectById(projectId)

        //Then
        assertEquals(projectDTO, actualProjectDTO)
    }

    @Test
    fun `return all project roles by project id`() {
        //Given
        val projectId = 1

        val rolesDTO = listOf(
            createProjectRoleUserDTO()
        )

        doReturn(rolesDTO).whenever(projectRolesByProjectIdUseCase).get(projectId)

        //When
        val actualRolesDTO = projectController.getProjectRolesByProjectId(projectId)

        //Then
        assertEquals(actualRolesDTO, rolesDTO)
    }

    @Test
    fun `FAIL with not found when the project to retrieve is not found in the database`() {
        //Given
        val projectId = 3L

        doThrow(ProjectNotFoundException(projectId)).whenever(projectByIdUseCase).get(projectId)

        //When
        val exception = assertThrows<ProjectNotFoundException> {
            projectController.getProjectById(projectId)
        }

        //Then
        assertEquals(exception.id, projectId)
        assertEquals(exception.message, "Project (id: $projectId) not found")
    }
}
