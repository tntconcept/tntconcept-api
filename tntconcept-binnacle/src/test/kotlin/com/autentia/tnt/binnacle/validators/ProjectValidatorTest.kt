package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.Organization
import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.exception.InvalidBlockDateException
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectValidatorTest {
    private val projectValidator = ProjectValidator()

    @Test
    fun `do nothing when project is valid for block`() {
        projectValidator.checkProjectIsValidForBlocking(unblockedProject, validBlockDate)
    }

    @Test
    fun `do nothing when project is valid for unblock`() {
        projectValidator.checkProjectIsValidForUnblocking(unblockedProject)
    }

    @Test
    fun `throw closed exception when project is invalid for block`() {
        assertThrows<ProjectClosedException> {
            projectValidator.checkProjectIsValidForBlocking(
                    blockedProject,
                    invalidBlockDate
            )
        }
    }

    @Test
    fun `throw closed exception when project is invalid for unblock`() {
        assertThrows<ProjectClosedException> { projectValidator.checkProjectIsValidForUnblocking(blockedProject) }
    }

    @Test
    fun `throw illegal state exception when block date is invalid for block`() {
        assertThrows<InvalidBlockDateException> {
            projectValidator.checkProjectIsValidForBlocking(
                    unblockedProject,
                    invalidBlockDate
            )
        }
    }

    private companion object {

        private val invalidBlockDate = LocalDate.now().plusDays(2L)
        private val validBlockDate = LocalDate.now()
        private val blockedProject =
                Project(
                        1,
                        "TNT",
                        false,
                        false,
                        LocalDate.now(),
                        null,
                        null,
                        Organization(1, "Autentia"),
                )

        private val unblockedProject =
                Project(
                        2,
                        "Vacaciones",
                        true,
                        true,
                        LocalDate.now(),
                        null,
                        null,
                        Organization(1, "Organization"),
                )
    }
}