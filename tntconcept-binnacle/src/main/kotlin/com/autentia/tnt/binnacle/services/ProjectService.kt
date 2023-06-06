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
    fun blockProject(projectId: Long, blockUntil: LocalDate): Project {
        TODO("Not implemented")
    }

    fun unblockProject(projectId: Long): Project {
        TODO("Not implemented")
    }

    @Transactional
    @ReadOnly
    fun getProjectById(id: Long): Project {
        val activity = projectRepository.findById(id)
        return if (activity.isPresent) {
            activity.get().toDomain()
        }else{
            throw ProjectNotFoundException(id)
        }
    }

}