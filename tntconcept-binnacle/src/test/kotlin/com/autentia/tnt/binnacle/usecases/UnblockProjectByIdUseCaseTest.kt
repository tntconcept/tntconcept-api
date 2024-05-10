package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.validators.ProjectValidator
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.*

class UnblockProjectByIdUseCaseTest {
    private val securityService: SecurityService = mock()
    private val projectRepository: ProjectRepository = mock()
    private val projectResponseConverter = ProjectResponseConverter()
    private val projectValidator = ProjectValidator()
    private val unblockProjectByIdUseCase = UnblockProjectByIdUseCase(
        securityService,
        projectRepository,
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
    fun `throw exception when project is closed for blocking`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithProjectBlock))
        whenever(projectRepository.findById(projectId)).thenReturn(Optional.of(unblockedProjectEntity))
        whenever(projectRepository.update(unblockedProjectEntity)).thenReturn(unblockedProjectEntity)

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
        private val organizationEntity: com.autentia.tnt.binnacle.entities.Organization = com.autentia.tnt.binnacle.entities.Organization(
            id = 1L,
            "Test organization",
            1,
            emptyList(),
        )
        private val unblockedProjectEntity: com.autentia.tnt.binnacle.entities.Project = com.autentia.tnt.binnacle.entities.Project(
            id = 1L,
            name = "Test project",
            open = false,
            organization = organizationEntity,
            startDate = LocalDate.of(2023, 5, 1),
            projectRoles = emptyList(),
            billingType = "CLOSED_PRICE"
        )
    }
}