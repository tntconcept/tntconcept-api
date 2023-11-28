package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.Calendar
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.VacationRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.services.EmptyActivitiesReminderMailService
import com.autentia.tnt.binnacle.services.UserService
import io.micronaut.data.jpa.repository.criteria.Specification
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmptyActivitiesReminderUseCaseTest {

    private val calendarFactory = mock<CalendarFactory>()
    private val activityRepository = mock<ActivityRepository>()
    private val vacationRepository = mock<VacationRepository>()
    private val emptyActivitiesReminderMailService = mock<EmptyActivitiesReminderMailService>()
    private val userService = mock<UserService>()
    private val appProperties = AppProperties()
    private val sut = EmptyActivitiesReminderUseCase(
        calendarFactory,
        activityRepository,
        vacationRepository,
        emptyActivitiesReminderMailService,
        userService,
        appProperties
    )

    @Test
    fun `should send reminders if is enabled`() {
        appProperties.apply {
            binnacle.emptyActivitiesReminder.enabled = true
        }
        val calendar = Calendar(
            dateInterval = DateInterval.of(
                LocalDate.now().withDayOfYear(1), LocalDate.now().withDayOfYear(20)
            ),
            holidays = listOf(
                Holiday(1, "Holiday description", LocalDate.now().withDayOfYear(6).atStartOfDay()),
                Holiday(1, "Holiday description", LocalDate.now().withDayOfYear(7).atStartOfDay())
            )
        )
        whenever(calendarFactory.create(DateInterval.of(LocalDate.now().withDayOfYear(1), LocalDate.now())))
            .thenReturn(calendar)
        whenever(userService.getActiveUsersWithoutSecurity()).thenReturn(
            listOf(createUser(LocalDate.now().withDayOfYear(1), 15L))
        )
        whenever(
            activityRepository.findAll(
                generateActivitySpecification()
            )
        ).thenReturn(listOf(generateActivity()))
        whenever(
            vacationRepository.findByDatesAndStatesWithoutSecurity(
                LocalDate.now().withDayOfYear(1), LocalDate.now().withDayOfYear(17),
                listOf(VacationState.ACCEPT, VacationState.PENDING)
            )
        ).thenReturn(listOf(generateVacation()))

        sut.sendReminders()

        verify(this.emptyActivitiesReminderMailService).sendEmail(
            listOf(
                LocalDate.now().withDayOfYear(2),
                LocalDate.now().withDayOfYear(13),
                LocalDate.now().withDayOfYear(16),
                LocalDate.now().withDayOfYear(17)
            ), "jdoe@doe.com", Locale.forLanguageTag("es")
        )
    }

    @Test
    fun `should not send reminders if is disabled`() {
        appProperties.apply {
            binnacle.emptyActivitiesReminder.enabled = false
        }

        sut.sendReminders()

        verifyNoInteractions(
            calendarFactory,
            activityRepository,
            vacationRepository,
            emptyActivitiesReminderMailService,
            userService
        )
    }

    private fun generateActivitySpecification(): Specification<Activity> {
        var predicate: Specification<Activity> = ActivityPredicates.ALL
        predicate = PredicateBuilder.and(
            predicate, ActivityPredicates.endDateGreaterThanOrEqualTo(LocalDate.now().withDayOfYear(1))
        )
        predicate = PredicateBuilder.and(
            predicate, ActivityPredicates.startDateLessThanOrEqualTo(LocalDate.now().withDayOfYear(17))
        )
        return predicate
    }

    private fun generateVacation() = Vacation(
        id = 1,
        userId = 15L,
        description = "2 days",
        state = VacationState.REJECT,
        startDate = LocalDate.now().withDayOfYear(3),
        endDate = LocalDate.now().withDayOfYear(5),
        chargeYear = LocalDate.now().withDayOfYear(1)
    )

    private fun generateActivity(): Activity = Activity(
        billable = false,
        description = "Dummy description",
        hasEvidences = false,
        id = 1L,
        projectRole = createProjectRole(10L),
        start = LocalDateTime.of(LocalDate.now().withDayOfYear(8), LocalTime.NOON),
        end = LocalDateTime.of(LocalDate.now().withDayOfYear(12), LocalTime.NOON).plusMinutes(60),
        userId = 15L,
        approvalState = ApprovalState.NA,
        duration = 4
    )

}