package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.validators.ProjectValidator
import com.autentia.tnt.security.application.checkBlockProjectsRole
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton

@Singleton
class UnblockProjectByIdUseCase internal constructor(
    private val securityService: SecurityService,
    private val projectRepository: ProjectRepository,
    private val projectResponseConverter: ProjectResponseConverter,
    private val projectValidator: ProjectValidator,
) {

    fun unblockProject(projectId: Long): ProjectResponseDTO {
        securityService.checkBlockProjectsRole()

        val project = projectRepository.findById(projectId).orElseThrow { throw ProjectNotFoundException(projectId) }
        projectValidator.checkProjectIsValidForUnblocking(project.toDomain())
        project.blockedByUser = null
        project.blockDate = null
        val unblockedProject = projectRepository.update(project).toDomain()
        return projectResponseConverter.toProjectResponseDTO(unblockedProject)
    }
}