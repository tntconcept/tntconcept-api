package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ImputableProjectsByOrganizationIdUseCase internal constructor(
    private val projectRepository: ProjectRepository
) {

    @Transactional
    @ReadOnly
    fun get(id: Long): List<Project> =
        projectRepository.findAllByOrganizationId(id)
            .filter { it.open && it.projectRoles.isNotEmpty() }
}
