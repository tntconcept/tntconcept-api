package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.services.ActivityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class ProjectRoleUserConverterTest {

    private val activityService = mock<ActivityService>()

    private val projectRoleResponseConverter  =  ProjectRoleResponseConverter(activityService)

    @Test
    fun `given entity ProjectRole should return ProjectRoleResponseDTO with converted values`() {
        val projectRoleResponseDTO = projectRoleResponseConverter.toProjectRoleDTO(role)

        assertEquals(role.id, projectRoleResponseDTO.id)
        assertEquals(role.name, projectRoleResponseDTO.name)
        assertEquals(role.requireEvidence, projectRoleResponseDTO.requireEvidence)
    }

    @Test
    fun `given ProjectRole list should return ProjectRoleResponseDTO list with converted values`() {
        val projectRoleList = listOf(
            ProjectRole(1, "First Role", RequireEvidence.NO, project, 0, true, false, TimeUnit.MINUTES),
            ProjectRole(2, "Second Role", RequireEvidence.WEEKLY, project, 0, true, false, TimeUnit.MINUTES)
        )

        val projectRoleResponseDTOList = projectRoleList.map { projectRoleResponseConverter.toProjectRoleDTO(it) }

        val expectedProjectRoleDTOLists = listOf(
            ProjectRoleDTO(1, "First Role", 2, 1, 0, true, TimeUnit.MINUTES, RequireEvidence.NO, false),
            ProjectRoleDTO(2, "Second Role", 2, 1, 0, true, TimeUnit.MINUTES, RequireEvidence.WEEKLY, false)
        )
        assertEquals(expectedProjectRoleDTOLists, projectRoleResponseDTOList)
    }

    private companion object {
        val project = Project(1, "Dummy project", false, false, Organization(2, "Organzation", listOf()), listOf())
        val role = ProjectRole(1, "First Role", RequireEvidence.NO, project, 0, true, false, TimeUnit.MINUTES)

    }
}
