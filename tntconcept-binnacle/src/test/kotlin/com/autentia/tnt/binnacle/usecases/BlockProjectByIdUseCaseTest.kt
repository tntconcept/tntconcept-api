package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.core.domain.Organization
import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.services.ProjectService
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.*

class BlockProjectByIdUseCaseTest {

    private val projectService: ProjectService = mock()
    private val securityService: SecurityService = mock()
    private val projectResponseConverter = ProjectResponseConverter()
    private val blockProjectByIdUseCase = BlockProjectByIdUseCase(
        securityService,
        projectService,
        projectResponseConverter
    )

    @Test
    fun `block project without required role should throw exception`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutBlockRole))

        assertThrows<IllegalStateException> {
            blockProjectByIdUseCase.blockProject(projectId, LocalDate.now())
        }

    }

    @Test
    fun `block project when project does not exist should throw exception`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithProjectBlock))
        whenever(projectService.blockProject(projectId, LocalDate.now(), userId)).thenThrow(
            ProjectNotFoundException(
                projectId
            )
        )

        assertThrows<ProjectNotFoundException> {
            blockProjectByIdUseCase.blockProject(projectId, LocalDate.now())
        }
    }

    @Test
    fun `block project with required role should return blocked project response dto`() {
        val expectedProject = project.copy(
            blockDate = LocalDate.now(),
            blockedByUser = userId
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithProjectBlock))
        whenever(projectService.blockProject(projectId, LocalDate.now(), userId)).thenReturn(expectedProject)

        val blockedProject = blockProjectByIdUseCase.blockProject(projectId, LocalDate.now())

        val expectedProjectResponseDTO = projectResponseConverter.toProjectResponseDTO(
            expectedProject
        )
        assertThat(blockedProject).isEqualTo(expectedProjectResponseDTO)
    }

    private companion object {
        const val projectId = 1L
        const val userId = 1L
        val authenticationWithoutBlockRole: Authentication =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("")))
        val authenticationWithProjectBlock: Authentication =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("project-blocker")))
        val project: Project = Project(
            id = 1L,
            name = "Test project",
            billable = true,
            open = true,
            organization = Organization(1L, "Test organization"),
            startDate = LocalDate.of(2023, 5, 1)
        )
    }
}