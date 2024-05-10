package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class ImputableProjectsByOrganizationIdUseCaseTest {

    private val projectRepository = mock<ProjectRepository>()
    private val imputableProjectsByOrganizationIdUseCase = ImputableProjectsByOrganizationIdUseCase(projectRepository, ProjectResponseConverter())

    @Test
    fun `return all IMPUTABLE projects by organization id`() {

        doReturn(PROJECTS).whenever(projectRepository).findAllByOrganizationId(ORGANIZATION_ID)

        assertEquals(listOf(PROJECT_DTO), imputableProjectsByOrganizationIdUseCase.get(ORGANIZATION_ID))
    }

    private companion object {
        private const val ORGANIZATION_ID = 1L

        private val ORGANIZATION = Organization(ORGANIZATION_ID, "Dummy Organization", 1, listOf())

        private val PROJECT_ROLE_PROJECT_CLOSED = ProjectRole(
            1L,
            "Dummy role",
            RequireEvidence.NO,
            Project(
                2L,
                " Project is Closed",
                false,
                LocalDate.now(),
                null,
                null,
                ORGANIZATION,
                emptyList(),
                "NO_BILLABLE"
            ),
            0,
            0,
            true,
            false,
            TimeUnit.MINUTES
        )
        private val PROJECT_ROLE_PROJECT_OPEN = ProjectRole(
            1L,
            "Dummy role",
            RequireEvidence.NO,
            Project(
                2L,
                " Project open",
                true,
                LocalDate.now(),
                null,
                null,
                ORGANIZATION,
                emptyList(),
                "NO_BILLABLE"
            ),
            0,
            0,
            true,
            false,
            TimeUnit.MINUTES
        )

        private val projectOpen = Project(1L, "Project is Open", true,  LocalDate.now(), null, null, ORGANIZATION,
            listOf(PROJECT_ROLE_PROJECT_OPEN),"NO_BILLABLE")

        private val projectClosed = Project(2L, " Project is Closed", false,  LocalDate.now(), null, null, ORGANIZATION,
            listOf(PROJECT_ROLE_PROJECT_CLOSED), "NO_BILLABLE")


        private val PROJECTS = listOf(projectClosed, projectOpen)

        private val PROJECT_DTO = ProjectResponseDTO(1, "Project is Open", true, ProjectBillingTypes().getProjectBillingType("NO_BILLABLE"), 1L, startDate = LocalDate.now())
    }
}

