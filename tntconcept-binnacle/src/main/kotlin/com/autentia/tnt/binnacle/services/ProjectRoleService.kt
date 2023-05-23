package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
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
    fun getAllByProjectIds(projectIds: List<Int>): List<ProjectRole> =
        projectRoleRepository.getAllByProjectIdIn(projectIds.map(Int::toLong))

    @Transactional
    @ReadOnly
    fun getByProjectRoleId(projectRoleId: Long) =
        projectRoleRepository.findById(projectRoleId)?.toDomain() ?: throw ProjectRoleNotFoundException(projectRoleId)


    @Transactional
    @ReadOnly
    fun getAllNotWorkable(): List<ProjectRole> = projectRoleRepository.getAllNotWorkable()
}