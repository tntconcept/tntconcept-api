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
    fun getAllByIds(ids: List<Long>) = projectRoleRepository.getAllByIdIn(ids).map { it.toDomain() }

    @Transactional
    @ReadOnly
    fun getAllByProjectId(projectId: Long) =
        projectRoleRepository.getAllByProjectId(projectId).map(ProjectRole::toDomain)

}