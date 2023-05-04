package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.services.ActivityEvidenceMailService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import jakarta.inject.Singleton
import java.util.*

@Singleton
class ActivityEvidenceOnceMissingReminderUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityEvidenceMailService: ActivityEvidenceMailService,
    private val userService: UserService,
) {
    fun sendReminders() {
        val rolesMissingEvidenceByUser: Map<User, List<ProjectRole>> = getProjectRolesMissingEvidenceByUser()
        rolesMissingEvidenceByUser.forEach { (user, rolesMissingEvidence) ->
            notifyMissingEvidencesToUser(
                user,
                rolesMissingEvidence
            )
        }
    }

    private fun getProjectRolesMissingEvidenceByUser(): Map<User, List<ProjectRole>> {
        val activeUsers: List<User> = userService.findActive()
        //TODO: migrate to find by predicate
        val activitiesMissingEvidence: List<Activity> = activityService.getActivitiesMissingEvidenceOnce()
        val activitiesMissingEvidenceByUser = groupActivitiesByUser(activitiesMissingEvidence, activeUsers)
        return getProjectRolesByUserFromActivities(activitiesMissingEvidenceByUser)
    }

    private fun notifyMissingEvidencesToUser(user: User, rolesMissingEvidence: List<ProjectRole>) {
        val rolesGroupedByProject: Map<Project, List<ProjectRole>> = rolesMissingEvidence.groupBy { it.project }
        rolesGroupedByProject.forEach { notifyMissingProjectEvidenceToUser(user, it.key, it.value) }
    }


    private fun groupActivitiesByUser(
        activitiesMissingEvidence: List<Activity>,
        users: List<User>,
    ): Map<User, List<Activity>> {
        val activitiesMissingEvidenceByUser = mutableMapOf<User, List<Activity>>()
        for (user: User in users) {
            activitiesMissingEvidenceByUser[user] = activitiesMissingEvidence.filter { it.userId == user.id }
        }
        return activitiesMissingEvidenceByUser
    }

    private fun getProjectRolesByUserFromActivities(activitiesMissingEvidenceByUser: Map<User, List<Activity>>)
            : Map<User, List<ProjectRole>> {
        return activitiesMissingEvidenceByUser.mapValues { activitiesByUser ->
            activitiesByUser.value.map { it.projectRole }.distinct()
        }
    }

    private fun notifyMissingProjectEvidenceToUser(user: User, project: Project, projectRoleList: List<ProjectRole>) {
        val projectRoleNames = projectRoleList.map { it.name }.toSet()
        activityEvidenceMailService.sendEmail(
            project.organization.name,
            project.name,
            projectRoleNames,
            user.email,
            Locale.ENGLISH
        )
    }

}