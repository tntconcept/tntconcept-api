package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Calendar
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.repositories.VacationRepository
import com.autentia.tnt.binnacle.services.EmptyActivitiesReminderMailService
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
    private val activitiesByFilterUseCase = mock<ActivitiesByFilterUseCase>()
    private val vacationRepository = mock<VacationRepository>()
    private val emptyActivitiesReminderMailService = mock<EmptyActivitiesReminderMailService>()
    private val usersRetrievalUseCase = mock<UsersRetrievalUseCase>()
    private val appProperties = AppProperties()
    private val sut = EmptyActivitiesReminderUseCase(
        calendarFactory,
        activitiesByFilterUseCase,
        vacationRepository,
        emptyActivitiesReminderMailService,
        usersRetrievalUseCase,
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
        whenever(usersRetrievalUseCase.getUsers(UserFilterDTO(active = true))).thenReturn(
            listOf(generateUserResponseDTO())
        )
        whenever(
            activitiesByFilterUseCase.getActivities(
                ActivityFilterDTO(
                    LocalDate.now().withDayOfYear(1), LocalDate.now().withDayOfYear(17)
                )
            )
        ).thenReturn(listOf(generateActivityResponseDTO()))
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
            ), "user@mail.com", Locale.forLanguageTag("es")
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
            activitiesByFilterUseCase,
            vacationRepository,
            emptyActivitiesReminderMailService,
            usersRetrievalUseCase
        )
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

    private fun generateUserResponseDTO() = UserResponseDTO(
        id = 15L,
        username = "userName",
        name = "User Name",
        email = "user@mail.com"
    )

    private fun generateActivityResponseDTO(): ActivityResponseDTO = ActivityResponseDTO(
        billable = false,
        description = "Dummy description",
        hasEvidences = false,
        id = 1L,
        projectRoleId = 10L,
        interval = IntervalResponseDTO(
            LocalDateTime.of(LocalDate.now().withDayOfYear(8), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now().withDayOfYear(12), LocalTime.NOON).plusMinutes(60),
            60,
            TimeUnit.MINUTES
        ),
        userId = 15L,
        approval = ApprovalDTO(ApprovalState.NA)
    )

}