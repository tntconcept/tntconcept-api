package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import javax.transaction.Transactional

@Singleton
internal class ProjectService(
    private val projectRepository: ProjectRepository
) {
    @Transactional
    @ReadOnly
    fun findById(projectId: Long): Project {
        val project = projectRepository.findById(projectId).orElseThrow { ProjectNotFoundException(projectId) }
        return project.toDomain()
    }


    fun blockProject(projectId: Long, blockUntil: LocalDate): Project {
        TODO("Not implemented")
    }

    fun unblockProject(projectId: Long): Project {
        TODO("Not implemented")
    }
}