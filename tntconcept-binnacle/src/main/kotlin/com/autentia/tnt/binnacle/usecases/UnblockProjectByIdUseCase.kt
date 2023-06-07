package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.services.ProjectService
import com.autentia.tnt.security.application.checkBlockProjectsRole
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton

@Singleton
class UnblockProjectByIdUseCase internal constructor(
    private val securityService: SecurityService,
    private val projectService: ProjectService,
    private val projectResponseConverter: ProjectResponseConverter
) {

    fun unblockProject(projectId: Long): ProjectResponseDTO {
        securityService.checkBlockProjectsRole()
        val unblockedProject = projectService.unblockProject(projectId)
        return projectResponseConverter.toProjectResponseDTO(unblockedProject)
    }
}