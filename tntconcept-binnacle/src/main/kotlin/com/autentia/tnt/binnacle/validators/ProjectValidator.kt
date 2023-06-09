package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class ProjectValidator {
    fun checkProjectIsValidForBlocking(project: Project, blockDate: LocalDate) {
        when {
            isClosedProject(project) -> throw ProjectClosedException()
            isNewBlockDateInvalid(blockDate) -> throw IllegalStateException("Invalid blocked date. It can't be a future date.")
        }
    }

    fun checkProjectIsValidForUnblocking(project: Project) {
        when {
            isClosedProject(project) -> throw ProjectClosedException()
        }
    }

    private fun isClosedProject(project: Project): Boolean {
        return !project.open
    }

    private fun isNewBlockDateInvalid(blockDate: LocalDate): Boolean {
        return blockDate > LocalDate.now()
    }
}
