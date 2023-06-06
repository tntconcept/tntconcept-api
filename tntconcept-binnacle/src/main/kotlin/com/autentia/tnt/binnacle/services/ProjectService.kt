package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import jakarta.inject.Singleton
import java.time.LocalDate

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
}