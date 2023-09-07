package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.belongsToUsers
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.missingEvidenceOnce
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.missingEvidenceWeekly
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.startDateBetweenDates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder.and
import com.autentia.tnt.binnacle.services.ActivityEvidenceMissingMailService
import com.autentia.tnt.binnacle.services.UserService
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.time.LocalDate
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
    fun sendReminders(type: NotificationType) {
        val rolesMissingEvidenceByUser: Map<User, List<Activity>> = getActivitiesMissingEvidenceByUser(type)

        rolesMissingEvidenceByUser.forEach { (user, activitiesMissingEvidence) ->
            notifyMissingEvidencesToUser(user, activitiesMissingEvidence)
        }
    }

    private fun getActivitiesMissingEvidenceByUser(type: NotificationType): Map<User, List<Activity>> {
        val activeUsers: List<User> = userService.getActiveUsersWithoutSecurity()

        val activitiesMissingEvidence: List<Activity> = activityRepository.findAll(
            getActivitiesMissingEvidencePredicate(activeUsers.map { it.id }.toList(), type)
        )

        val activitiesMissingEvidenceByUser = mutableMapOf<User, List<Activity>>()
        for (user in activeUsers) {
            activitiesMissingEvidenceByUser[user] = activitiesMissingEvidence.filter { it.userId == user.id }
        }

        return activitiesMissingEvidenceByUser.mapValues { activitiesByUser ->
            activitiesByUser.value.distinctBy { it -> it.projectRole }
        }
    }

    private fun getActivitiesMissingEvidencePredicate(listOfUserIds: List<Long>, type: NotificationType): Specification<Activity> {
        val dateInterval = DateInterval.of(LocalDate.now().minusDays(7), LocalDate.now())

        return when(type) {
            NotificationType.WEEKLY -> and(
                (and(missingEvidenceWeekly(), startDateBetweenDates(dateInterval))), belongsToUsers(listOfUserIds))
            NotificationType.ONCE -> and(missingEvidenceOnce(), belongsToUsers(listOfUserIds))
        }
    }

    private fun notifyMissingEvidencesToUser(user: User, activitiesMissingEvidence: List<Activity>) {
        val locale = Locale.forLanguageTag("es")
        activitiesMissingEvidence.forEach {
            activityEvidenceMissingMailService.sendEmail(
                it.projectRole.project.organization.name,
                it.projectRole.project.name,
                it.projectRole.name,
                it.projectRole.requireEvidence,
                it.start,
                user.email,
                locale
            )
        }
    }

}

enum class NotificationType {
    WEEKLY, ONCE
}