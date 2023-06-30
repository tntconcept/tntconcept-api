package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ImputableProjectsByOrganizationIdUseCase internal constructor(
    private val projectRepository: ProjectRepository,
    private val projectResponseConverter: ProjectResponseConverter
) {

    @Transactional
    @ReadOnly
    fun get(id: Long): List<ProjectResponseDTO> =
        projectRepository.findAllByOrganizationId(id)
            .filter { it.open && it.projectRoles.isNotEmpty() }
            .map { projectResponseConverter.toProjectResponseDTO(it) }

}
