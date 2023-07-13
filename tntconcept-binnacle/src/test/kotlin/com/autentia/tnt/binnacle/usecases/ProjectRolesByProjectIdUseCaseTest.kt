package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class ProjectRolesByProjectIdUseCaseTest {

    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val projectRolesByProjectIdUseCase =
        ProjectRolesByProjectIdUseCase(projectRoleRepository, ProjectRoleResponseConverter())

    @Test
    fun `return the expected project role`() {

        doReturn(listOf(PROJECT_ROLE)).whenever(projectRoleRepository).getAllByProjectId(PROJECT_ID)

        assertEquals(
            listOf(
                ProjectRoleDTO(
                    PROJECT_ID,
                    "Dummy Role",
                    ORGANIZATION.id,
                    PROJECT_ID,
                    PROJECT_ROLE.maxAllowed,
                    PROJECT_ROLE.isWorkingTime,
                    PROJECT_ROLE.timeUnit,
                    PROJECT_ROLE.requireEvidence,
                    PROJECT_ROLE.isApprovalRequired
                )
            ), projectRolesByProjectIdUseCase.get(
                PROJECT_ID.toInt()
            )
        )
    }

    private companion object{
        private const val PROJECT_ID = 1L

        private val ORGANIZATION = Organization(1L, "Nuestra empresa", listOf())
        private val PROJECT = Project(1L, "Dummy project", true,  false, LocalDate.now(), null, null, ORGANIZATION, listOf())

        private val PROJECT_ROLE = ProjectRole(PROJECT_ID, "Dummy Role", RequireEvidence.NO, PROJECT, 0, 0, true, false, TimeUnit.MINUTES)


    }
}
