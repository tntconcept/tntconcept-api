package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.services.ProjectRoleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ProjectRoleByIdUseCaseTest {

    private val id = 1L
    private val projectRoleService = mock<ProjectRoleService>()
    private val projectRoleByIdUseCase =
        ProjectRoleByIdUseCase(projectRoleService, ProjectRoleResponseConverter())

    @Test
    fun `find project role by id`() {
        val projectRole = createProjectRole().toDomain()
        whenever(projectRoleService.getByProjectRoleId(id)).thenReturn(projectRole)

        assertEquals(
            ProjectRoleDTO(
                projectRole.id,
                projectRole.name,
                projectRole.project.organization.id,
                projectRole.project.id,
                projectRole.maxAllowed,
                projectRole.isWorkingTime,
                projectRole.timeUnit,
                projectRole.requireEvidence,
                projectRole.isApprovalRequired
            ), projectRoleByIdUseCase.get(id)
        )
    }
}
