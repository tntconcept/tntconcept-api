package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.VacationRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.services.EmptyActivitiesReminderMailService
import com.autentia.tnt.binnacle.services.UserService
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*
import javax.transaction.Transactional

@Singleton
class EmptyActivitiesReminderUseCase @Inject internal constructor(
    private val calendarFactory: CalendarFactory,
    @param:Named("Internal") private val activityRepository: ActivityRepository,
    private val vacationRepository: VacationRepository,
    private val emptyActivitiesReminderMailService: EmptyActivitiesReminderMailService,
    private val userService: UserService,
    private val appProperties: AppProperties
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(EmptyActivitiesReminderUseCase::class.java)
        private const val REMOVE_LAST_WORKABLE_DAYS = 3
    }

    @Transactional
    @ReadOnly
    fun sendReminders() {
        if (!appProperties.binnacle.emptyActivitiesReminder.enabled) {
            logger.info("Mailing of empty activities reminder is disabled")
            return
        }
        logger.info("Start EmptyActivitiesReminderUseCase")
        val workableDays = generateWorkableDays()
        val activeUsers = userService.getActiveUsersWithoutSecurity()
        val predicate: Specification<Activity> = getPredicateFromActivityFilter(workableDays.last())
        val allActivities = activityRepository.findAll(predicate).map { it.toDomain() }
        val activitiesByUser = activeUsers.associate {
            it.id to allActivities.filter { activity -> it.id == activity.userId }
        }
        val allVacations = vacationRepository.findByDatesAndStatesWithoutSecurity(
            firstDayOfActualYear()!!, workableDays.last(),
            listOf(VacationState.ACCEPT, VacationState.PENDING)
        )
        val vacationsByUser = activeUsers.associate {
            it.id to allVacations.filter { activity -> it.id == activity.userId }
        }
        val emptyDaysByUser = generateEmptyDaysByUser(
            activeUsers.map { it.id },
            workableDays, vacationsByUser, activitiesByUser
        )
        val userIdsToSendEmail = emptyDaysByUser.keys
        val usersToSendEmail = activeUsers.filter { userIdsToSendEmail.contains(it.id) }
        sendMails(usersToSendEmail, emptyDaysByUser)
        logger.info("End EmptyActivitiesReminderUseCase")
    }

    private fun getPredicateFromActivityFilter(endDate: LocalDate): Specification<Activity> {
        var predicate: Specification<Activity> = ActivityPredicates.ALL
        predicate = PredicateBuilder.and(
            predicate, ActivityPredicates.startDateLessThanOrEqualTo(endDate)
        )
        predicate = PredicateBuilder.and(
            predicate, ActivityPredicates.endDateGreaterThanOrEqualTo(firstDayOfActualYear()!!)
        )
        return predicate
    }

    private fun sendMails(users: List<User>?, emptyDaysByUser: Map<Long, List<LocalDate>>) {
        val locale = Locale.forLanguageTag("es")
        users?.forEach {
            emptyActivitiesReminderMailService.sendEmail(emptyDaysByUser[it.id]!!, it.email, locale)
        }
    }

    private fun generateEmptyDaysByUser(
        userIds: List<Long>,
        workableDays: List<LocalDate>,
        vacationsByUser: Map<Long, List<Vacation>>,
        activitiesByUser: Map<Long, List<com.autentia.tnt.binnacle.core.domain.Activity>>
    ): Map<Long, List<LocalDate>> {
        return userIds.associateWith { userId ->
            findEmptyDays(
                workableDays,
                activitiesByUser,
                userId,
                vacationsByUser
            )
        }
    }

    private fun findEmptyDays(
        workableDays: List<LocalDate>,
        activitiesByUser: Map<Long, List<com.autentia.tnt.binnacle.core.domain.Activity>>,
        userId: Long,
        vacationsByUser: Map<Long, List<Vacation>>
    ): MutableList<LocalDate> {
        val workableDaysToUser = workableDays.toMutableList()
        activitiesByUser[userId]?.forEach {
            removeDays(workableDaysToUser, it.getStart().toLocalDate(), it.getEnd().toLocalDate())
        }
        vacationsByUser[userId]?.forEach {
            removeDays(workableDaysToUser, it.startDate, it.endDate)
        }
        return workableDaysToUser
    }

    private fun removeDays(
        workableDaysToUser: MutableList<LocalDate>,
        dateToDelete: LocalDate,
        dateLimitToDelete: LocalDate
    ) {
        var dateIteration = dateToDelete
        while (!dateIteration.isAfter(dateLimitToDelete)) {
            workableDaysToUser.remove(dateIteration)
            dateIteration = dateIteration.plusDays(1)
        }
    }

    private fun generateWorkableDays(): List<LocalDate> {
        val workableDays = calendarFactory.create(DateInterval.of(firstDayOfActualYear()!!, LocalDate.now()))
            .workableDays
        return workableDays.subList(0, workableDays.size - REMOVE_LAST_WORKABLE_DAYS)
    }

    private fun firstDayOfActualYear(): LocalDate? = LocalDate.now().withDayOfYear(1)

}