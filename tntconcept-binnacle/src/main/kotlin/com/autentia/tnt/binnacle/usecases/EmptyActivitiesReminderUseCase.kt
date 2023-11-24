package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.ActivityFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.UserFilterDTO
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import com.autentia.tnt.binnacle.repositories.VacationRepository
import com.autentia.tnt.binnacle.services.EmptyActivitiesReminderMailService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*
import javax.transaction.Transactional

@Singleton
class EmptyActivitiesReminderUseCase @Inject internal constructor(
    private val calendarFactory: CalendarFactory,
    private val activitiesByFilterUseCase: ActivitiesByFilterUseCase,
    private val vacationRepository: VacationRepository,
    private val emptyActivitiesReminderMailService: EmptyActivitiesReminderMailService,
    private val usersRetrievalUseCase: UsersRetrievalUseCase,
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
        val workableDays = generateWorkableDays()
        val activeUsers = usersRetrievalUseCase.getUsers(UserFilterDTO(active = true))
        val allActivities = activitiesByFilterUseCase.getActivities(
            ActivityFilterDTO(
                firstDayOfActualYear(),
                workableDays.last()
            )
        )
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
    }

    private fun sendMails(users: List<UserResponseDTO>, emptyDaysByUser: Map<Long, List<LocalDate>>) {
        val locale = Locale.forLanguageTag("es")
        users.forEach {
            emptyActivitiesReminderMailService.sendEmail(emptyDaysByUser[it.id]!!, it.email, locale)
        }
    }

    private fun generateEmptyDaysByUser(
        userIds: List<Long>,
        workableDays: List<LocalDate>,
        vacationsByUser: Map<Long, List<Vacation>>,
        activitiesByUser: Map<Long, List<ActivityResponseDTO>>
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
        activitiesByUser: Map<Long, List<ActivityResponseDTO>>,
        userId: Long,
        vacationsByUser: Map<Long, List<Vacation>>
    ): MutableList<LocalDate> {
        val workableDaysToUser = workableDays.toMutableList()
        activitiesByUser[userId]?.forEach {
            removeDays(workableDaysToUser, it.interval.start.toLocalDate(), it.interval.end.toLocalDate())
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
        while (!dateToDelete.isAfter(dateLimitToDelete)) {
            workableDaysToUser.remove(dateToDelete)
        }
    }

    private fun generateWorkableDays(): List<LocalDate> {
        val workableDays = calendarFactory.create(DateInterval.of(firstDayOfActualYear()!!, LocalDate.now()))
            .workableDays
        return workableDays.subList(0, workableDays.size - REMOVE_LAST_WORKABLE_DAYS)
    }

    private fun firstDayOfActualYear(): LocalDate? = LocalDate.now().withDayOfYear(1)

}