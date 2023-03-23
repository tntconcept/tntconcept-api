package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

internal class ProjectRoleByIdUseCaseTest {

    private val id = 1L
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityService = mock<ActivityService>()
    private val projectRoleByIdUseCase = ProjectRoleByIdUseCase(projectRoleRepository, ProjectRoleResponseConverter(activityService))

    @Test
    fun `find project role by id`() {
        val projectRole = createProjectRole()

        doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(id)

        assertEquals(ProjectRoleDTO(projectRole.id, projectRole.name, projectRole.requireEvidence), projectRoleByIdUseCase.get(id))
    }

    @Test
    fun `throw role was not found exception`() {
        doReturn(Optional.ofNullable(null)).whenever(projectRoleRepository).findById(id)

        assertThrows<ProjectRoleNotFoundException> { projectRoleByIdUseCase.get(id) }
    }
}
