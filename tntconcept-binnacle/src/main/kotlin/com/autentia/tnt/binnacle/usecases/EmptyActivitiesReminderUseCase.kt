package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.ActivityFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.repositories.VacationRepository
import com.autentia.tnt.binnacle.services.EmptyActivitiesReminderMailService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate
import java.util.*
import javax.transaction.Transactional

private const val REMOVE_LAST_WORKABLE_DAYS = 3

@Singleton
class EmptyActivitiesReminderUseCase @Inject internal constructor(
    private val calendarFactory: CalendarFactory,
    private val activitiesByFilterUseCase: ActivitiesByFilterUseCase,
    private val vacationRepository: VacationRepository,
    private val userRepository: UserRepository,
    private val emptyActivitiesReminderMailService: EmptyActivitiesReminderMailService
) {

    @Transactional
    @ReadOnly
    fun sendReminders() {
        val workableDays = generateWorkableDays()
        val activities =
            activitiesByFilterUseCase.getActivities(ActivityFilterDTO(firstDayOfActualYear(), workableDays.last()))
        val activitiesByUser = generateActivitiesByUser(activities)
        val vacations = vacationRepository.findBetweenChargeYearsAndStatesWithoutSecurity(
            firstDayOfActualYear()!!, workableDays.last(),
            listOf(VacationState.ACCEPT, VacationState.PENDING)
        )
        val vacationsByUser = generateVacationsByUser(vacations)
        val emptyDaysByUser = generateEmptyDaysByUser(workableDays, vacationsByUser, activitiesByUser)
        val mailsByUser = findMailsByUser(emptyDaysByUser.keys.toList())
        sendMails(mailsByUser, emptyDaysByUser)
    }

    private fun sendMails(mailsByUser: Map<Long, String>, emptyDaysByUser: Map<Long, List<LocalDate>>) {
        val locale = Locale.forLanguageTag("es")
        mailsByUser.forEach { (id, email) ->
            emptyActivitiesReminderMailService.sendEmail(emptyDaysByUser[id]!!, email, locale)
        }
    }

    private fun findMailsByUser(ids: List<Long>): Map<Long, String> {
        return userRepository.findByIdsWithoutSecurity(ids).associate { it.id to it.email }
    }

    private fun generateEmptyDaysByUser(
        workableDays: List<LocalDate>,
        vacationsByUser: Map<Long, List<Vacation>>,
        activitiesByUser: Map<Long, List<ActivityResponseDTO>>
    ): Map<Long, List<LocalDate>> {
        //TODO IMPLEMENT
        return emptyMap()
    }

    private fun generateVacationsByUser(vacations: List<Vacation>): Map<Long, List<Vacation>> {
        //TODO IMPLEMENT
        return emptyMap()
    }

    private fun generateActivitiesByUser(activities: List<ActivityResponseDTO>): Map<Long, List<ActivityResponseDTO>> {
        //TODO IMPLEMENT
        return emptyMap()
    }

    private fun generateWorkableDays(): List<LocalDate> {
        val workableDays = calendarFactory.create(DateInterval.of(firstDayOfActualYear()!!, LocalDate.now()))
            .workableDays
        return workableDays.subList(0, workableDays.size - REMOVE_LAST_WORKABLE_DAYS)
    }

    private fun firstDayOfActualYear(): LocalDate? = LocalDate.now().withDayOfYear(1)

}