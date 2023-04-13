package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

internal class ProjectRoleByIdUseCaseTest {

    private val id = 1L
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val projectRoleByIdUseCase = ProjectRoleByIdUseCase(projectRoleRepository, ProjectRoleResponseConverter())

    @Test
    fun `find project role by id`() {
        val projectRole = createProjectRole()

        doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(id)

        assertEquals(ProjectRoleDTO(projectRole.id, projectRole.name, projectRole.project.organization.id, projectRole.project.id, projectRole.maxAllowed, projectRole.timeUnit, projectRole.requireEvidence, projectRole.isApprovalRequired), projectRoleByIdUseCase.get(id))
    }

    @Test
    fun `throw role was not found exception`() {
        doReturn(Optional.ofNullable(null)).whenever(projectRoleRepository).findById(id)

        assertThrows<ProjectRoleNotFoundException> { projectRoleByIdUseCase.get(id) }
    }
}
