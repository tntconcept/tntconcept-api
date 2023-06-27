package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
internal class ProjectService(
    private val projectRepository: ProjectRepository,
) {
    @Transactional
    @ReadOnly
    fun findById(id: Long): Project {
        return projectRepository
            .findById(id).map { it.toDomain() }.orElseThrow { throw ProjectNotFoundException(id) }
    }

}