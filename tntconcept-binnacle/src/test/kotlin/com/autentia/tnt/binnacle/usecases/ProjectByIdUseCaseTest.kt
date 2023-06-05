package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.Optional

internal class ProjectByIdUseCaseTest {

    private val projectRepository = mock<ProjectRepository>()

    private val projectByIdUseCase = ProjectByIdUseCase(projectRepository, ProjectResponseConverter())

    @Test
    fun `return the expected project`() {
        doReturn(Optional.of(PROJECT)).whenever(projectRepository).findById(PROJECT_ID)

        assertEquals(PROJECT_DTO, projectByIdUseCase.get(PROJECT_ID))
    }

    @Test
    fun `throw ProjectNotFoundException when the project does not exist`() {
        doReturn(Optional.ofNullable(null)).whenever(projectRepository).findById(OTHER_PROJECT)

        val exception = assertThrows<ProjectNotFoundException> {
            projectByIdUseCase.get(OTHER_PROJECT)
        }

        assertEquals(OTHER_PROJECT, exception.id)
        assertEquals("Project (id: $OTHER_PROJECT) not found", exception.message)
    }

    private companion object {
        private const val PROJECT_ID = 1L
        private const val OTHER_PROJECT = 2L
        private val ORGANIZATION = Organization(PROJECT_ID, "Autentia", emptyList())
        private val PROJECT = Project(
            PROJECT_ID,
            "Dummy Project",
            open = false,
            billable = false,
            LocalDate.now(),
            null,
            null,
            ORGANIZATION,
            listOf()
        )
        private val PROJECT_DTO = ProjectResponseDTO(
            PROJECT.id,
            PROJECT.name,
            PROJECT.open,
            PROJECT.billable,
            1L
        )
    }

}
