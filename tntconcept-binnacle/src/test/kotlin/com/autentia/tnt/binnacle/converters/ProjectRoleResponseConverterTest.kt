package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ProjectRoleResponseConverterTest {

    private val projectRoleResponseConverter  =  ProjectRoleResponseConverter()

    @Test
    fun `given entity ProjectRole should return ProjectRoleResponseDTO with converted values`() {
        val projectRoleResponseDTO = projectRoleResponseConverter.toProjectRoleResponseDTO(role)

        assertEquals(role.id, projectRoleResponseDTO.id)
        assertEquals(role.name, projectRoleResponseDTO.name)
        assertEquals(role.requireEvidence, projectRoleResponseDTO.requireEvidence)
    }

    @Test
    fun `given ProjectRole list should return ProjectRoleResponseDTO list with converted values`() {
        val projectRoleList = listOf(
            ProjectRole(1, "First Role", false, project, 0),
            ProjectRole(2, "Second Role", true, project, 0)
        )

        val projectRoleResponseDTOList = projectRoleList.map { projectRoleResponseConverter.toProjectRoleResponseDTO(it) }

        val expectedProjectRoleResponseDTOList = listOf(
            ProjectRoleResponseDTO(1, "First Role", false),
            ProjectRoleResponseDTO(2, "Second Role", true)
        )
        assertEquals(expectedProjectRoleResponseDTOList, projectRoleResponseDTOList)
    }

    private companion object{
        val project = Project(1,"Dummy project", false, false, Organization(2, "Organzation", listOf()), listOf())
        val role = ProjectRole(1, "First Role", false, project, 0)

    }
}
