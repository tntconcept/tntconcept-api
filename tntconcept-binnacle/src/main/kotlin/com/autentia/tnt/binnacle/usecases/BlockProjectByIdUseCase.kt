package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.validators.ProjectValidator
import com.autentia.tnt.security.application.checkBlockProjectsRole
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class BlockProjectByIdUseCase internal constructor(
    private val securityService: SecurityService,
    private val projectRepository: ProjectRepository,
    private val projectResponseConverter: ProjectResponseConverter,
    private val projectValidator: ProjectValidator,
) {

    fun blockProject(projectId: Long, blockDate: LocalDate): ProjectResponseDTO {
        val authentication = securityService.checkBlockProjectsRole()
        val userId = authentication.id()

        val project = projectRepository.findById(projectId).orElseThrow { ProjectNotFoundException(projectId) }
        projectValidator.checkProjectIsValidForBlocking(project.toDomain(), blockDate)
        project.blockedByUser = userId
        project.blockDate = blockDate
        val projectBlocked = projectRepository.update(project).toDomain()
        return projectResponseConverter.toProjectResponseDTO(projectBlocked)
    }
}