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
import com.autentia.tnt.binnacle.services.DateService
import com.autentia.tnt.binnacle.services.EmptyActivitiesReminderMailService
import com.autentia.tnt.binnacle.services.UserService
import io.micronaut.data.jpa.repository.criteria.Specification
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class EmptyActivitiesReminderUseCaseTest {

    companion object {
        private const val yearTest = 2023
        private val firstDayOfYear = LocalDate.ofYearDay(yearTest, 1)
        private val sunday = LocalDate.ofYearDay(yearTest, 15)
        private val saturday = LocalDate.ofYearDay(yearTest, 14)
        private val workableFriday = LocalDate.ofYearDay(yearTest, 20)
        private val holiday = LocalDate.ofYearDay(yearTest, 17)
    }

    private val calendarFactory = mock<CalendarFactory>()
    private val dateService = mock<DateService>()
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
        appProperties,
        dateService
    )

    @Test
    fun `should send reminders if is enabled at holiday`() {
        appProperties.apply { binnacle.emptyActivitiesReminder.enabled = true }
        val calendar = Calendar(
            dateInterval = DateInterval.of(firstDayOfYear, holiday), holidays = listOf(
                Holiday(1, "Holiday description", LocalDate.ofYearDay(yearTest, 17).atStartOfDay()),
                Holiday(1, "Holiday description", LocalDate.ofYearDay(yearTest, 18).atStartOfDay())
            )
        )
        whenever(calendarFactory.create(DateInterval.of(firstDayOfYear, holiday))).thenReturn(calendar)
        whenever(dateService.getLocalDateNow()).thenReturn(holiday)
        whenever(userService.getActiveUsersWithoutSecurity()).thenReturn(
            listOf(createUser(firstDayOfYear, 15))
        )
        whenever(activityRepository.findAll(generateActivitySpecification(firstDayOfYear, 12))).thenReturn(
            listOf(generateActivity())
        )

        sut.sendReminders()

        verify(this.emptyActivitiesReminderMailService).sendEmail(
            listOf(
                LocalDate.ofYearDay(yearTest, 2),
                LocalDate.ofYearDay(yearTest, 3),
                LocalDate.ofYearDay(yearTest, 4),
                LocalDate.ofYearDay(yearTest, 5),
                LocalDate.ofYearDay(yearTest, 6),
                LocalDate.ofYearDay(yearTest, 9),
                LocalDate.ofYearDay(yearTest, 10),
                LocalDate.ofYearDay(yearTest, 11)
            ), "jdoe@doe.com", Locale.forLanguageTag("es")
        )
    }

    @Test
    fun `should send reminders if is enabled at sunday`() {
        appProperties.apply { binnacle.emptyActivitiesReminder.enabled = true }
        val calendar = Calendar(
            dateInterval = DateInterval.of(firstDayOfYear, sunday), holidays = listOf(
                Holiday(1, "Holiday description", LocalDate.ofYearDay(yearTest, 6).atStartOfDay()),
                Holiday(1, "Holiday description", LocalDate.ofYearDay(yearTest, 7).atStartOfDay())
            )
        )
        whenever(calendarFactory.create(DateInterval.of(firstDayOfYear, sunday))).thenReturn(
            calendar
        )
        whenever(dateService.getLocalDateNow()).thenReturn(sunday)
        whenever(userService.getActiveUsersWithoutSecurity()).thenReturn(
            listOf(createUser(firstDayOfYear, 15))
        )
        whenever(activityRepository.findAll(generateActivitySpecification(firstDayOfYear, 10))).thenReturn(
            listOf(generateActivity())
        )
        whenever(
            vacationRepository.findByDatesAndStatesWithoutSecurity(
                firstDayOfYear,
                LocalDate.ofYearDay(yearTest, 10),
                listOf(VacationState.ACCEPT, VacationState.PENDING)
            )
        ).thenReturn(listOf(generateVacation(firstDayOfYear)))

        sut.sendReminders()

        verify(this.emptyActivitiesReminderMailService).sendEmail(
            listOf(LocalDate.ofYearDay(yearTest, 2)), "jdoe@doe.com", Locale.forLanguageTag("es")
        )
    }

    @Test
    fun `should send reminders if is enabled at saturday`() {
        appProperties.apply { binnacle.emptyActivitiesReminder.enabled = true }
        val calendar = Calendar(
            dateInterval = DateInterval.of(firstDayOfYear, saturday), holidays = listOf(
                Holiday(1, "Holiday description", LocalDate.ofYearDay(yearTest, 6).atStartOfDay()),
                Holiday(1, "Holiday description", LocalDate.ofYearDay(yearTest, 7).atStartOfDay())
            )
        )
        whenever(calendarFactory.create(DateInterval.of(firstDayOfYear, saturday))).thenReturn(calendar)
        whenever(dateService.getLocalDateNow()).thenReturn(saturday)
        whenever(userService.getActiveUsersWithoutSecurity()).thenReturn(listOf(createUser(firstDayOfYear, 15)))
        whenever(activityRepository.findAll(generateActivitySpecification(firstDayOfYear, 10))).thenReturn(
            listOf(generateActivity())
        )
        whenever(
            vacationRepository.findByDatesAndStatesWithoutSecurity(
                firstDayOfYear,
                LocalDate.ofYearDay(yearTest, 10),
                listOf(VacationState.ACCEPT, VacationState.PENDING)
            )
        ).thenReturn(listOf(generateVacation(firstDayOfYear)))

        sut.sendReminders()

        verify(this.emptyActivitiesReminderMailService).sendEmail(
            listOf(LocalDate.ofYearDay(yearTest, 2)), "jdoe@doe.com", Locale.forLanguageTag("es")
        )
    }

    @Test
    fun `should send reminders if is enabled at workableFriday`() {
        appProperties.apply { binnacle.emptyActivitiesReminder.enabled = true }
        val calendar = Calendar(
            dateInterval = DateInterval.of(firstDayOfYear, workableFriday), holidays = listOf(
                Holiday(1, "Holiday description", LocalDate.ofYearDay(yearTest, 6).atStartOfDay()),
                Holiday(1, "Holiday description", LocalDate.ofYearDay(yearTest, 7).atStartOfDay())
            )
        )
        whenever(calendarFactory.create(DateInterval.of(firstDayOfYear, workableFriday))).thenReturn(calendar)
        whenever(dateService.getLocalDateNow()).thenReturn(workableFriday)
        whenever(userService.getActiveUsersWithoutSecurity()).thenReturn(listOf(createUser(firstDayOfYear, 15)))
        whenever(activityRepository.findAll(generateActivitySpecification(firstDayOfYear, 16))).thenReturn(
            listOf(generateActivity())
        )
        whenever(
            vacationRepository.findByDatesAndStatesWithoutSecurity(
                firstDayOfYear,
                LocalDate.ofYearDay(yearTest, 16),
                listOf(VacationState.ACCEPT, VacationState.PENDING)
            )
        ).thenReturn(listOf(generateVacation(firstDayOfYear)))

        sut.sendReminders()

        verify(this.emptyActivitiesReminderMailService).sendEmail(
            listOf(
                LocalDate.ofYearDay(yearTest, 2),
                LocalDate.ofYearDay(yearTest, 13),
                LocalDate.ofYearDay(yearTest, 16)
            ), "jdoe@doe.com", Locale.forLanguageTag("es")
        )
    }

    @Test
    fun `should not send reminders if is disabled`() {
        appProperties.apply { binnacle.emptyActivitiesReminder.enabled = false }

        sut.sendReminders()

        verifyNoInteractions(
            calendarFactory,
            activityRepository,
            vacationRepository,
            emptyActivitiesReminderMailService,
            userService
        )
    }

    private fun generateActivitySpecification(firstDayOfYear: LocalDate, dayOfYear: Int): Specification<Activity> {
        var predicate: Specification<Activity> = ActivityPredicates.ALL
        predicate = PredicateBuilder.and(
            predicate, ActivityPredicates.endDateGreaterThanOrEqualTo(firstDayOfYear)
        )
        predicate = PredicateBuilder.and(
            predicate, ActivityPredicates.startDateLessThanOrEqualTo(LocalDate.ofYearDay(yearTest, dayOfYear))
        )
        return predicate
    }

    private fun generateVacation(firstDayOfYear: LocalDate) = Vacation(
        id = 1,
        userId = 15L,
        description = "2 days",
        state = VacationState.ACCEPT,
        startDate = LocalDate.ofYearDay(yearTest, 3),
        endDate = LocalDate.ofYearDay(yearTest, 5),
        chargeYear = firstDayOfYear
    )

    private fun generateActivity(): Activity = Activity(
        billable = false,
        description = "Dummy description",
        id = 1L,
        projectRole = createProjectRole(10L),
        start = LocalDateTime.of(LocalDate.ofYearDay(yearTest, 8), LocalTime.NOON),
        end = LocalDateTime.of(LocalDate.ofYearDay(yearTest, 12), LocalTime.NOON).plusMinutes(60),
        userId = 15L,
        approvalState = ApprovalState.NA,
        duration = 4
    )

}