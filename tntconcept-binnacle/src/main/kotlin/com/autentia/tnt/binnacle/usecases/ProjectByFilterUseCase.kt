package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.dto.ProjectFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.repositories.predicates.EmptySpecification
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOpenSpecification
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOrganizationIdSpecification
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ProjectByFilterUseCase internal constructor(
    private val projectRepository: ProjectRepository,
    private val projectResponseConverter: ProjectResponseConverter,
) {
    @Transactional
    @ReadOnly
    fun getProjects(projectFilter: ProjectFilterDTO): List<ProjectResponseDTO> {
        val predicate = getPredicateFromFilter(projectFilter)
        val projects = projectRepository.findAll(predicate).map { it.toDomain() }
        return projects.map {
            projectResponseConverter.toProjectResponseDTO(it)
        }
    }

    private fun getPredicateFromFilter(projectFilter: ProjectFilterDTO): Specification<Project> {
        var predicate: Specification<Project> = EmptySpecification()

        predicate = PredicateBuilder.and(predicate, ProjectOrganizationIdSpecification(projectFilter.organizationId))

        predicate = if (projectFilter.open !== null) {
            PredicateBuilder.and(predicate, ProjectOpenSpecification(projectFilter.open))
        } else {
            PredicateBuilder.and(predicate, ProjectOpenSpecification(true))
        }

        return predicate
    }
}