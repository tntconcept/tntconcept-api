package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.dto.ProjectFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.repositories.predicates.*
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
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

        if(projectFilter.organizationId !== null) {
            predicate =
                PredicateBuilder.and(predicate, ProjectOrganizationIdSpecification(projectFilter.organizationId))
        }

        if(projectFilter.organizationIds.isNotEmpty()){
            predicate =
                PredicateBuilder.and(predicate, ProjectOrganizationIdsSpecification(projectFilter.organizationIds))
        }

        if (projectFilter.open !== null) {
            predicate = PredicateBuilder.and(predicate, ProjectOpenSpecification(projectFilter.open))
        }

        return predicate
    }
}