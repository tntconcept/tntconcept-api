package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
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
    fun getAllByIds(ids: List<Int>): List<ProjectRole> =
        projectRoleRepository.getAllByIdIn(ids.map(Int::toLong))

    @Transactional
    @ReadOnly
    fun getByProjectRoleId(projectRoleId: Long): ProjectRole =
        projectRoleRepository.findById(projectRoleId).orElseThrow { ProjectRoleNotFoundException(projectRoleId) }

    @Transactional
    @ReadOnly
    fun getByUserIds(userIds: List<Long>): List<ProjectRoleUser> =
        projectRoleRepository.findDistinctRolesByUserIds(userIds)
}