package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.core.domain.Organization
import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.exception.ProjectClosedException
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

class UnblockProjectByIdUseCaseTest {
    private val securityService: SecurityService = mock()
    private val projectService: ProjectService = mock()
    private val projectResponseConverter = ProjectResponseConverter()
    private val projectValidator = ProjectValidator()
    private val unblockProjectByIdUseCase = UnblockProjectByIdUseCase(
        securityService,
        projectService,
        projectResponseConverter,
        projectValidator
    )

    @Test
    fun `test unblock when not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            unblockProjectByIdUseCase.unblockProject(projectId)
        }
    }

    @Test
    fun `test unblock when not project block role`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutProjectBlock))

        assertThrows<IllegalStateException> {
            unblockProjectByIdUseCase.unblockProject(projectId)
        }
    }

    @Test
    fun `test unblock should call service`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithProjectBlock))
        whenever(projectService.findById(projectId)).thenReturn(unblockedProject)
        whenever(projectService.unblockProject(projectId)).thenReturn(unblockedProject)

        val result = unblockProjectByIdUseCase.unblockProject(projectId)

        val expected = projectResponseConverter.toProjectResponseDTO(unblockedProject)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `throw exception when project is closed for blocking`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithProjectBlock))
        whenever(projectService.findById(projectId)).thenReturn(blockedProject)

        assertThrows<ProjectClosedException> { unblockProjectByIdUseCase.unblockProject(projectId) }
    }

    private companion object {
        private const val userId = 1L
        private const val projectId = 1L
        val authenticationWithoutProjectBlock: Authentication =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("")))
        val authenticationWithProjectBlock: Authentication =
            ClientAuthentication(
                userId.toString(),
                mapOf("roles" to listOf("project-blocker"))
            )
        val unblockedProject: Project = Project(
            id = 1L,
            name = "Test project",
            billable = true,
            open = true,
            organization = Organization(1L, "Test organization"),
            startDate = LocalDate.of(2023, 5, 1),
        )
        val blockedProject: Project = Project(
            id = 1L,
            name = "Test project",
            billable = true,
            open = false,
            organization = Organization(1L, "Test organization"),
            startDate = LocalDate.of(2023, 5, 1),
        )
    }
}