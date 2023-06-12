package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.core.domain.Organization
import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.exception.InvalidBlockDateException
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.services.ProjectService
import com.autentia.tnt.binnacle.validators.ProjectValidator
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
    private val projectValidator = ProjectValidator()
    private val blockProjectByIdUseCase = BlockProjectByIdUseCase(
        securityService,
        projectService,
        projectResponseConverter,
        projectValidator
    )

    @Test
    fun `should block a project with a date in the past`() {
        val daysBefore = 2L
        val dateTwoDaysBefore = LocalDate.now().minusDays(daysBefore)

        val expectedProject = unblockedProject.copy(
            blockDate = dateTwoDaysBefore,
            blockedByUser = userId
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithProjectBlock))
        whenever(projectService.findById(projectId)).thenReturn(unblockedProject)
        whenever(projectService.blockProject(projectId, dateTwoDaysBefore, userId)).thenReturn(expectedProject)

        val blockedProject = blockProjectByIdUseCase.blockProject(projectId, dateTwoDaysBefore)

        val expectedProjectResponseDTO = projectResponseConverter.toProjectResponseDTO(
            expectedProject
        )

        assertThat(blockedProject).isEqualTo(expectedProjectResponseDTO)
    }

    @Test
    fun `blocking project with an invalid date should throw exception`() {
        val dateTwoDaysBefore = LocalDate.now().plusDays(2L)

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithProjectBlock))
        whenever(projectService.findById(projectId)).thenReturn(unblockedProject)

        assertThrows<InvalidBlockDateException> {
            blockProjectByIdUseCase.blockProject(projectId, dateTwoDaysBefore)
        }
    }

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
        whenever(projectService.findById(projectId)).thenThrow(
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
        val expectedProject = unblockedProject.copy(
            blockDate = LocalDate.now(),
            blockedByUser = userId
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithProjectBlock))
        whenever(projectService.findById(projectId)).thenReturn(unblockedProject)
        whenever(projectService.blockProject(projectId, LocalDate.now(), userId)).thenReturn(expectedProject)

        val blockedProject = blockProjectByIdUseCase.blockProject(projectId, LocalDate.now())

        val expectedProjectResponseDTO = projectResponseConverter.toProjectResponseDTO(
            expectedProject
        )
        assertThat(blockedProject).isEqualTo(expectedProjectResponseDTO)
    }

    @Test
    fun `throw exception when project is closed for blocking`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithProjectBlock))

        whenever(projectService.findById(projectId)).thenReturn(blockedProject)
        assertThrows<ProjectClosedException> { blockProjectByIdUseCase.blockProject(projectId, LocalDate.now()) }
    }

    private companion object {
        const val projectId = 1L
        const val userId = 1L
        val authenticationWithoutBlockRole: Authentication =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("")))
        val authenticationWithProjectBlock: Authentication =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("project-blocker")))
        val unblockedProject: Project = Project(
            id = 1L,
            name = "Test project",
            billable = true,
            open = true,
            organization = Organization(1L, "Test organization"),
            startDate = LocalDate.of(2023, 5, 1)
        )
        val blockedProject: Project = Project(
            id = 1L,
            name = "Test project",
            billable = true,
            open = false,
            organization = Organization(1L, "Test organization"),
            startDate = LocalDate.of(2023, 5, 1)
        )
    }
}