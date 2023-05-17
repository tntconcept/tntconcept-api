package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.belongsToUsers
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.hasNotEvidence
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.projectRoleRequiresEvidence
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.services.ActivityEvidenceMissingMailService
import com.autentia.tnt.binnacle.services.UserService
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional

@Singleton
class ActivityEvidenceMissingReminderUseCase @Inject internal constructor(
    @param:Named("Internal") private val activityRepository: ActivityRepository,
    private val activityEvidenceMissingMailService: ActivityEvidenceMissingMailService,
    private val userService: UserService,
) {

    @Transactional
    @ReadOnly
    fun sendReminders() {
        val rolesMissingEvidenceByUser: Map<User, List<ProjectRole>> = getProjectRolesMissingEvidenceByUser()

        rolesMissingEvidenceByUser.forEach { (user, rolesMissingEvidence) ->
            notifyMissingEvidencesToUser(user, rolesMissingEvidence)
        }
    }

    private fun getProjectRolesMissingEvidenceByUser(): Map<User, List<ProjectRole>> {
        val activeUsers: List<User> = userService.findActive()

        val activitiesMissingEvidence: List<Activity> = activityRepository.findAll(
            getActivitiesMissingEvidenceOncePredicate(activeUsers.map { it.id }.toList())
        )

        val activitiesMissingEvidenceByUser = mutableMapOf<User, List<Activity>>()
        for (user in activeUsers) {
            activitiesMissingEvidenceByUser[user] = activitiesMissingEvidence.filter { it.userId == user.id }
        }

        return activitiesMissingEvidenceByUser.mapValues { activitiesByUser ->
            activitiesByUser.value.map { it.projectRole }.distinct()
        }
    }

    private fun getActivitiesMissingEvidenceOncePredicate(listOfUserIds: List<Long>): Specification<Activity> {
        return PredicateBuilder.and(
            PredicateBuilder.and(hasNotEvidence(), projectRoleRequiresEvidence(RequireEvidence.ONCE)),
            belongsToUsers(listOfUserIds)
        )
    }

    private fun notifyMissingEvidencesToUser(user: User, rolesMissingEvidence: List<ProjectRole>) {
        val rolesGroupedByProject: Map<Project, List<ProjectRole>> = rolesMissingEvidence.groupBy { it.project }
        rolesGroupedByProject.forEach { notifyMissingProjectEvidenceToUser(user, it.key, it.value) }
    }

    private fun notifyMissingProjectEvidenceToUser(user: User, project: Project, projectRoleList: List<ProjectRole>) {
        val locale = Locale.forLanguageTag("es")

        projectRoleList.forEach {
            activityEvidenceMissingMailService.sendEmail(
                project.organization.name,
                project.name,
                it.name,
                it.requireEvidence,
                user.email,
                locale
            )
        }
    }
}