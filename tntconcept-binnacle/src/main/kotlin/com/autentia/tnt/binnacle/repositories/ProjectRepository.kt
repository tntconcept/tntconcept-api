package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Project
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

@Repository
internal interface ProjectRepository : CrudRepository<Project, Long> {

    fun findAllByOrganizationId(id: Long): List<Project>

}
