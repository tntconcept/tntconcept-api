package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Project
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.data.repository.CrudRepository

@Repository
internal interface ProjectRepository : CrudRepository<Project, Long> {

    fun findAll(projectSpecification: Specification<Project>): List<Project>
    fun findAllByOrganizationId(id: Long): List<Project>
}
