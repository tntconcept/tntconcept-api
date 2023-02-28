package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ImputableProjectsByOrganizationIdUseCaseTest {

    private val projectRepository = mock<ProjectRepository>()
    private val imputableProjectsByOrganizationIdUseCase = ImputableProjectsByOrganizationIdUseCase(projectRepository)

    @Test
    fun `return all IMPUTABLE projects by organization id`() {

        doReturn(PROJECTS).whenever(projectRepository).findAllByOrganizationId(ORGANIZATION_ID)

        assertEquals(PROJECTS.filter { it.open }, imputableProjectsByOrganizationIdUseCase.get(ORGANIZATION_ID))
    }

    private companion object {
        private const val ORGANIZATION_ID = 1L

        private val ORGANIZATION = Organization(ORGANIZATION_ID, "Dummy Organization", listOf())

        private val PROJECT_ROLE_PROJECT_CLOSED = ProjectRole(
            1L,
            "Dummy role",
            false,
            Project(
                2L,
                " Project is Closed",
                false,
                false,
                ORGANIZATION,
                emptyList()
            ),
            0
        )
        private val PROJECT_ROLE_PROJECT_OPEN = ProjectRole(
            1L,
            "Dummy role",
            false,
            Project(
                2L,
                " Project open",
                true,
                false,
                ORGANIZATION,
                emptyList()
            ),
            0
        )

        private val projectOpen = Project(1L, "Project is Open", true,  false,  ORGANIZATION,
            listOf(PROJECT_ROLE_PROJECT_OPEN))

        private val projectClosed = Project(2L, " Project is Closed", false,  false,  ORGANIZATION,
            listOf(PROJECT_ROLE_PROJECT_CLOSED)
        )

        private val PROJECTS = listOf(projectClosed, projectOpen)
    }
}

