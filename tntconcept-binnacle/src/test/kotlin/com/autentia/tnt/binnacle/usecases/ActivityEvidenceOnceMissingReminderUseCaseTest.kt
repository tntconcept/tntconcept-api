package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProject
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.services.ActivityEvidenceMissingMailService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class ActivityEvidenceOnceMissingReminderUseCaseTest {

    private val activityService: ActivityService = mock()
    private val activityEvidenceMissingMailService: ActivityEvidenceMissingMailService = mock()
    private val userService: UserService = mock()
    private val activityEvidenceOnceMissingReminderUseCase = ActivityEvidenceOnceMissingReminderUseCase(
            activityService, activityEvidenceMissingMailService, userService
    )

    @Test
    fun `Should call ActivityEvidenceMailService`() {
        // Given: A set of active users with activities for a role that require ONCE evidence
        val allUsers = listOf(user, otherUser)
        val allUserIds = allUsers.map { it.id }.toList()
        val listOfActivities = listOf(
                createActivity().copy(projectRole = projectRole, userId = user.id),
                createActivity().copy(projectRole = projectRole2, userId = user.id),
                createActivity().copy(projectRole = projectRole2, userId = otherUser.id),
                createActivity().copy(projectRole = projectRoleFromOtherProject, userId = user.id),
                createActivity().copy(projectRole = projectRoleFromOtherProject, userId = otherUser.id)
        )
        whenever(userService.findActive()).thenReturn(allUsers)
        val predicate = buildActivitiesPredicate(allUserIds);
        doNothing().whenever(activityEvidenceMissingMailService).sendEmail(any(), any(), any(), any(), any())
        whenever(activityService.getActivities(predicate)).thenReturn(listOfActivities)

        // When: Use case is called
        activityEvidenceOnceMissingReminderUseCase.sendReminders()

        // Then: Verify email is sent for role, user and project
        verify(activityEvidenceMissingMailService).sendEmail(
                eq(project.organization.name),
                eq(project.name),
                eq(setOf(projectRole.name, projectRole2.name)),
                eq(user.email),
                any()
        )
        verify(activityEvidenceMissingMailService).sendEmail(
                eq(project.organization.name),
                eq(project.name),
                eq(setOf(projectRole2.name)),
                eq(otherUser.email),
                any()
        )
        verify(activityEvidenceMissingMailService).sendEmail(
                eq(otherProject.organization.name),
                eq(otherProject.name),
                eq(setOf(projectRoleFromOtherProject.name)),
                eq(user.email),
                any()
        )
        verify(activityEvidenceMissingMailService).sendEmail(
                eq(otherProject.organization.name),
                eq(otherProject.name),
                eq(setOf(projectRoleFromOtherProject.name)),
                eq(otherUser.email),
                any()
        )
    }

    @Test
    fun `Should not call ActivityEvidenceMailService service when no activities are found`() {
        // Given: A set of active users with no activities for a role that require ONCE evidence
        val allUsers = listOf(user, otherUser)
        val allUserIds = allUsers.map { it.id }.toList()
        whenever(userService.findActive()).thenReturn(allUsers)
        val predicate = buildActivitiesPredicate(allUserIds);
        whenever(activityService.getActivities(predicate)).thenReturn(listOf())

        // When: Use cas is called
        activityEvidenceOnceMissingReminderUseCase.sendReminders()

        // Then: The email services is not called
        verifyNoInteractions(activityEvidenceMissingMailService)
    }
    
    companion object {
        private fun buildActivitiesPredicate(allUserIds: List<Long>) = PredicateBuilder.and(
                PredicateBuilder.and(ActivityPredicates.hasNotEvidence(),
                        ActivityPredicates.projectRoleRequiresEvidence(RequireEvidence.ONCE)),
                ActivityPredicates.belongsToUsers(allUserIds))

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