package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
internal class ProjectRoleService(private val projectRoleRepository: ProjectRoleRepository) {

    @Transactional
    @ReadOnly
    fun getAllByProjectIds(ids: List<Int>): List<ProjectRole> =
        projectRoleRepository.getAllByProjectIdIn(ids.map(Int::toLong))
}
