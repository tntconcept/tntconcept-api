package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProject
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.services.ActivityEvidenceMailService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class ActivityEvidenceOnceMissingReminderUseCaseTest {

    private val activityService: ActivityService = mock()
    private val activityEvidenceMailService: ActivityEvidenceMailService = mock()
    private val userService: UserService = mock()
    private val activityEvidenceOnceMissingReminderUseCase = ActivityEvidenceOnceMissingReminderUseCase(
        activityService, activityEvidenceMailService, userService
    )

    @Test
    fun `should call ActivityEvidenceMailService`() {
        whenever(userService.findActive()).thenReturn(listOf(user, otherUser))
        whenever(activityService.getActivitiesMissingEvidenceOnce()).thenReturn(
            listOf(
                createActivity().copy(projectRole = projectRole, userId = user.id),
                createActivity().copy(projectRole = projectRole2, userId = user.id),
                createActivity().copy(projectRole = projectRole2, userId = otherUser.id),
                createActivity().copy(projectRole = projectRoleFromOtherProject, userId = user.id),
                createActivity().copy(projectRole = projectRoleFromOtherProject, userId = otherUser.id)
            )
        )

        activityEvidenceOnceMissingReminderUseCase.sendReminders()

        verify(activityEvidenceMailService).sendEmail(
            eq(project.organization.name),
            eq(project.name),
            eq(setOf(projectRole.name, projectRole2.name)),
            eq(user.email),
            any()
        )
        verify(activityEvidenceMailService).sendEmail(
            eq(project.organization.name),
            eq(project.name),
            eq(setOf(projectRole2.name)),
            eq(otherUser.email),
            any()
        )
        verify(activityEvidenceMailService).sendEmail(
            eq(otherProject.organization.name),
            eq(otherProject.name),
            eq(setOf(projectRoleFromOtherProject.name)),
            eq(user.email),
            any()
        )
        verify(activityEvidenceMailService).sendEmail(
            eq(otherProject.organization.name),
            eq(otherProject.name),
            eq(setOf(projectRoleFromOtherProject.name)),
            eq(otherUser.email),
            any()
        )
    }

    companion object {
        private val project = createProject()
        private val otherProject = createProject().copy(id = 4L, name = "MyProjectRole")
        private val projectRole = ProjectRole(
            id = 2L,
            name = "MyProjectRole",
            requireEvidence = RequireEvidence.ONCE,
            project = project,
            isApprovalRequired = true,
            maxAllowed = 2,
            timeUnit = TimeUnit.DAYS,
            isWorkingTime = true
        )
        private val projectRole2 = projectRole.copy(id = 3, name = "MyOtherProjectRole", project = project)
        private val projectRoleFromOtherProject = projectRole.copy(id = 4, project = otherProject)
        private val user = createUser().copy(id = 2)
        private val otherUser = createUser().copy(id = 3, email = "other.user@example.com")
    }
}