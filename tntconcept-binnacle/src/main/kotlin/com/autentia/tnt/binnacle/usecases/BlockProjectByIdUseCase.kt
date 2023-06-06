package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.services.ProjectService
import com.autentia.tnt.security.application.checkBlockProjectsRole
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class BlockProjectByIdUseCase internal constructor(
    private val securityService: SecurityService,
    private val projectService: ProjectService,
    private val projectResponseConverter: ProjectResponseConverter
) {

    fun blockProject(projectId: Long, blockDate: LocalDate): ProjectResponseDTO {
        val authentication = securityService.checkBlockProjectsRole()
        val projectBlocked = projectService.blockProject(projectId, blockDate, authentication.id())
        return projectResponseConverter.toProjectResponseDTO(projectBlocked)
    }
}