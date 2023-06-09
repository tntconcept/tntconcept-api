package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
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

    fun blockProject(projectId: Long, blockDate: LocalDate, userId: Long): Project {
        val project = projectRepository.findById(projectId).orElseThrow { ProjectNotFoundException(projectId) }
        project.blockedByUser = userId
        project.blockDate = blockDate
        return projectRepository.update(project).toDomain()
    }

    @Transactional
    @ReadOnly
    fun getProjects(projectSpecification: Specification<com.autentia.tnt.binnacle.entities.Project>): List<Project> {
        return projectRepository.findAll(projectSpecification).map { it.toDomain() }
    }

    fun unblockProject(projectId: Long): Project {
        val project = projectRepository.findById(projectId).orElseThrow { ProjectNotFoundException(projectId) }
        project.blockedByUser = null
        project.blockDate = null
        return projectRepository.update(project).toDomain()
    }
}