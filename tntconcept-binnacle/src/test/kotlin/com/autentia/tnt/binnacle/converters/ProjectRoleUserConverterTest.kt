package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.MaxTimeAllowed
import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import com.autentia.tnt.binnacle.core.domain.RemainingTimeInfo
import com.autentia.tnt.binnacle.core.domain.TimeInfo
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.MaxTimeAllowedDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.entities.dto.TimeInfoDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class ProjectRoleUserConverterTest {

    private val projectRoleResponseConverter = ProjectRoleResponseConverter()

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
            ProjectRole(1, "First Role", RequireEvidence.NO, project, 0, 0, true, false, TimeUnit.MINUTES),
            ProjectRole(2, "Second Role", RequireEvidence.WEEKLY, project, 0, 0, true, false, TimeUnit.MINUTES)
        )

        val projectRoleResponseDTOList = projectRoleList.map { projectRoleResponseConverter.toProjectRoleDTO(it) }

        val expectedProjectRoleDTOLists = listOf(
            ProjectRoleDTO(1, "First Role", 2, 1, 0, true, TimeUnit.MINUTES, RequireEvidence.NO, false),
            ProjectRoleDTO(2, "Second Role", 2, 1, 0, true, TimeUnit.MINUTES, RequireEvidence.WEEKLY, false)
        )
        assertEquals(expectedProjectRoleDTOLists, projectRoleResponseDTOList)
    }

    @Test
    fun `given entity ProjectRoleUser should return ProjectRoleUserDTO with converted values`() {
        val projectRoleUserDTO = projectRoleResponseConverter.toProjectRoleUserDTO(projectRoleUser)

        assertEquals(projectRoleUser.id, projectRoleUserDTO.id)
        assertEquals(projectRoleUser.name, projectRoleUserDTO.name)
        assertEquals(projectRoleUser.organizationId, projectRoleUserDTO.organizationId)
        assertEquals(projectRoleUser.projectId, projectRoleUserDTO.projectId)
        assertEquals(TimeInfoDTO.from(projectRoleUser.timeInfo), projectRoleUserDTO.timeInfo)
        assertEquals(projectRoleUser.requireEvidence, projectRoleUserDTO.requireEvidence)
        assertEquals(projectRoleUser.requireApproval, projectRoleUserDTO.requireApproval)
        assertEquals(projectRoleUser.userId, projectRoleUserDTO.userId)
    }

    @Test
    fun `given ProjectRoleUser list should return ProjectRoleUserDTO list with converted values`() {

        val projectRoleUserList = listOf(
            projectRoleUser,
            projectRoleUser.copy(id = 2L)
        )

        val projectRoleUserDTO =
            ProjectRoleUserDTO(1,
                "Project role",
                2,
                3,
                RequireEvidence.NO,
                false,
                4L,
                TimeInfoDTO(MaxTimeAllowedDTO(250, 0), TimeUnit.MINUTES, 100)
                )

        val projectRoleResponseDTOList =
            projectRoleUserList.map { projectRoleResponseConverter.toProjectRoleUserDTO(it) }

        val expectedProjectRoleDTOLists = listOf(projectRoleUserDTO, projectRoleUserDTO.copy(id = 2L))

        assertEquals(expectedProjectRoleDTOLists, projectRoleResponseDTOList)
    }

    private companion object {
        val project = Project(1, "Dummy project", false, false, LocalDate.now(), null, null, Organization(2, "Organzation", listOf()), listOf())
        val role = ProjectRole(1, "First Role", RequireEvidence.NO, project, 0, 0, true, false, TimeUnit.MINUTES)
        val timeInfo =TimeInfo(MaxTimeAllowed(250, 0), TimeUnit.MINUTES)

        val projectRoleUser =
            ProjectRoleUser(
                1, "Project role", 2L, 3L, RequireEvidence.NO, false, 4L, RemainingTimeInfo.of(
                    timeInfo, 100)
            )
    }
}
