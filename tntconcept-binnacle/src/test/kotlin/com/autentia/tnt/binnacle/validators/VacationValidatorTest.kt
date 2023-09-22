package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.RequestVacation
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.Role
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.WorkingAgreement
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.VacationRepository
import com.autentia.tnt.binnacle.services.RemainingVacationService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.temporal.TemporalAdjusters

internal class VacationValidatorTest {

    private val vacationRepository = mock<VacationRepository>()
    private val holidayRepository = mock<HolidayRepository>()
    private val calendarFactory = CalendarFactory(holidayRepository)
    private val remainingVacationService = mock<RemainingVacationService>()
    private val user = mock<User>()

    private val vacationValidator =
        VacationValidator(vacationRepository, calendarFactory, remainingVacationService)

    // Characterized use cases objects
    private val today = LocalDate.now()
    private val tomorrow = today.plusDays(1)
    private val yesterday = today.minusDays(1)

    @Test
    fun `should create valid request`() {
        val requestVacation = RequestVacation(null, FIRST_MONDAY, FIRST_TUESDAY, CURRENT_YEAR, "description")
        val user = createUser()

        doReturn(emptyList<Holiday>()).whenever(holidayRepository).findAllByDateBetween(
            requestVacation.startDate.atTime(LocalTime.MIN),
            requestVacation.endDate.atTime(23, 59, 59)
        )

        doReturn(23).whenever(remainingVacationService).getRemainingVacations(eq(requestVacation.chargeYear), eq(user))

        mockRequestVacation(requestVacation)

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result).isEqualTo(CreateVacationValidation.Success)
    }

    @Test
    fun `should not create invalid dates`() {
        val requestVacation = RequestVacation(null, FIRST_MONDAY, PREVIOUS_SUNDAY, CURRENT_YEAR, "description")

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.INVALID_DATE_RANGE))
    }

    @Test
    fun `should not create due to empty period on weekend days`() {
        val requestVacation = RequestVacation(null, FIRST_SATURDAY, FIRST_SUNDAY, CURRENT_YEAR, "description")
        val user = createUser()

        doReturn(emptyList<Holiday>()).whenever(holidayRepository).findAllByDateBetween(
            requestVacation.startDate.atTime(LocalTime.MIN),
            requestVacation.endDate.atTime(23, 59, 59)
        )

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result).isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_REQUEST_EMPTY))
    }

    @Test
    fun `should not create due to empty period with weekend and holiday days`() {
        val requestVacation = RequestVacation(null, FIRST_SATURDAY, FIRST_SUNDAY, CURRENT_YEAR, "description")
        val user = createUser()
        val secondMonday = FIRST_MONDAY.plusDays(7)
        val holidays = listOf(
            Holiday(1, "Holiday", secondMonday.atStartOfDay())
        )

        doReturn(holidays).whenever(holidayRepository).findAllByDateBetween(
            requestVacation.startDate.atTime(LocalTime.MIN),
            requestVacation.endDate.atTime(23, 59, 59)
        )

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result).isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_REQUEST_EMPTY))
    }

    @Test
    fun `should not create on closed years`() {
        val requestVacation = RequestVacation(
            null,
            FIRST_MONDAY.minusYears(2),
            FIRST_TUESDAY.minusYears(2).plusDays(1),
            CURRENT_YEAR,
            "description"
        )

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_RANGE_CLOSED))
    }

    @Test
    fun `should not create when start date is before user hiring date`() {
        val user = createUser()
        val requestVacation = RequestVacation(
            null,
            LocalDate.of(user.hiringDate.year, user.hiringDate.month.minus(1), 4),
            LocalDate.of(user.hiringDate.year, user.hiringDate.month.minus(1), 8),
            CURRENT_YEAR,
            "description"
        )

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_BEFORE_HIRING_DATE))
    }

    @Test
    fun `should not create overlaps vacations `() {
        val user = createUser()
        val requestVacation = RequestVacation(null, FIRST_MONDAY, FIRST_FRIDAY, CURRENT_YEAR, "description")
        val userId = 2L
        val vacations = listOf(
            Vacation(
                id = 1L,
                startDate = FIRST_MONDAY,
                endDate = FIRST_TUESDAY,
                state = VacationState.ACCEPT,
                chargeYear = LocalDate.of(CURRENT_YEAR, 1, 1),
                userId = userId,
                description = ""
            ),
            Vacation(
                id = 2L,
                startDate = FIRST_FRIDAY,
                endDate = FIRST_FRIDAY,
                state = VacationState.PENDING,
                chargeYear = LocalDate.of(CURRENT_YEAR, 1, 1),
                userId = userId,
                description = ""
            ),
            Vacation(
                id = 4L,
                startDate = LocalDate.of(today.year, Month.MARCH, 18),
                endDate = LocalDate.of(today.year, Month.APRIL, 1),
                state = VacationState.CANCELLED,
                chargeYear = LocalDate.of(today.year, Month.MARCH, 1),
                userId = userId,
                description = ""
            ),
            Vacation(
                id = 5L,
                startDate = LocalDate.of(today.year, Month.MARCH, 15),
                endDate = LocalDate.of(today.year, Month.APRIL, 17),
                state = VacationState.REJECT,
                chargeYear = LocalDate.of(today.year, Month.MARCH, 1),
                userId = userId,
                description = ""
            )
        )
        given(vacationRepository.find(requestVacation.startDate, requestVacation.endDate)).willReturn(vacations)

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_REQUEST_OVERLAPS))
    }

    @Test
    fun `should create vacation with cancelled or rejected vacations overlapped`() {
        val user = createUser()
        val requestVacation = RequestVacation(null, FIRST_MONDAY, FIRST_FRIDAY, CURRENT_YEAR, "description")
        val userId = 2L
        val vacations = listOf(
            Vacation(
                id = 4L,
                startDate = FIRST_MONDAY,
                endDate = FIRST_TUESDAY,
                state = VacationState.CANCELLED,
                chargeYear = LocalDate.of(CURRENT_YEAR, 1, 1),
                userId = userId,
                description = ""
            ),
            Vacation(
                id = 5L,
                startDate = FIRST_FRIDAY,
                endDate = FIRST_FRIDAY,
                state = VacationState.REJECT,
                chargeYear = LocalDate.of(CURRENT_YEAR, 1, 1),
                userId = userId,
                description = ""
            )
        )
        doReturn(vacations).whenever(vacationRepository).find(eq(requestVacation.startDate), eq(requestVacation.endDate))

        doReturn(23).whenever(remainingVacationService).getRemainingVacations(eq(requestVacation.chargeYear), eq(user))

        mockRequestVacation(requestVacation)

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result).isEqualTo(CreateVacationValidation.Success)
    }

    @Test
    fun `should not create when request are zero days`() {
        val user = createUser()
        val requestVacation = RequestVacation(null, FIRST_SATURDAY, FIRST_SATURDAY, CURRENT_YEAR, "description")

        doReturn(emptyList<Holiday>()).whenever(holidayRepository).findAllByDateBetween(
            requestVacation.startDate.atTime(LocalTime.MIN),
            requestVacation.endDate.atTime(23, 59, 59)
        )

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_REQUEST_EMPTY))
    }

    @Test
    fun `should not create where user request more days than days left`() {
        val requestVacation = RequestVacation(null, FIRST_MONDAY, FIRST_THURSDAY, CURRENT_YEAR, "description")
        val user = createUser()

        doReturn(emptyList<Holiday>()).whenever(holidayRepository).findAllByDateBetween(
            requestVacation.startDate.atTime(LocalTime.MIN),
            requestVacation.endDate.atTime(23, 59, 59)
        )

        doReturn(2).whenever(remainingVacationService)
            .getRemainingVacations(eq(CURRENT_YEAR), eq(user))

        mockRequestVacation(requestVacation)

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result).isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.NO_MORE_DAYS_LEFT_IN_YEAR))
    }


    @Test
    fun `should update valid request`() {
        val user = createUser()
        val requestVacation = RequestVacation(1L, FIRST_MONDAY, FIRST_TUESDAY, CURRENT_YEAR, "description")
        val vacationDb = Vacation(
            requestVacation.id,
            requestVacation.startDate,
            requestVacation.endDate,
            VacationState.PENDING,
            2L,
            description = "description",
            chargeYear = LocalDate.of(CURRENT_YEAR, 1, 1)
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        doReturn(emptyList<Holiday>()).whenever(holidayRepository).findAllByDateBetween(
            requestVacation.startDate.atTime(LocalTime.MIN),
            requestVacation.endDate.atTime(23, 59, 59)
        )

        doReturn(10).whenever(remainingVacationService)
            .getRemainingVacations(eq(today.year), eq(user))

        mockRequestVacation(requestVacation)

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result).isEqualTo(UpdateVacationValidation.Success(vacationDb))
    }

    @Test
    fun `should not update invalid dates`() {
        val requestVacation = RequestVacation(1L, FIRST_MONDAY, PREVIOUS_SUNDAY, CURRENT_YEAR, "description")

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.INVALID_DATE_RANGE))
    }

    @Test
    fun `should not update not found vacation`() {
        val requestVacation = RequestVacation(1L, FIRST_MONDAY, FIRST_TUESDAY, CURRENT_YEAR, "description")

        given(vacationRepository.findById(1L)).willReturn(null)

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_NOT_FOUND))
    }

    @Test
    fun `should not update not pending vacations`() {
        given(user.id).willReturn(1L)
        val requestVacation = RequestVacation(1L, FIRST_MONDAY, FIRST_TUESDAY, CURRENT_YEAR, "description")
        val vacationDb = Vacation(
            requestVacation.id,
            requestVacation.startDate,
            requestVacation.endDate,
            VacationState.ACCEPT,
            1L,
            description = "description",
            chargeYear = LocalDate.of(CURRENT_YEAR, 1, 1)
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED))
    }

    @Test
    fun `should not update on closed years`() {
        val requestVacation = RequestVacation(
            1,
            FIRST_MONDAY.minusYears(2),
            FIRST_MONDAY.minusYears(2).plusDays(1),
            CURRENT_YEAR,
            "description"
        )
        val vacationDb = Vacation(
            requestVacation.id,
            requestVacation.startDate,
            requestVacation.endDate,
            VacationState.PENDING,
            2L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)
        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_RANGE_CLOSED))
    }

    @Test
    fun `should not update when start date is before user hiring date`() {
        val user = createUser()
        val requestVacation = RequestVacation(
            1,
            LocalDate.of(user.hiringDate.year, user.hiringDate.month.minus(1), 4),
            LocalDate.of(user.hiringDate.year, user.hiringDate.month.minus(1), 10),
            CURRENT_YEAR,
            "description"
        )
        val vacationDb = Vacation(
            requestVacation.id,
            requestVacation.startDate,
            requestVacation.endDate,
            VacationState.PENDING,
            2L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)
        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_BEFORE_HIRING_DATE))
    }

    @Test
    fun `should not update overlaps vacation`() {
        val user = createUser()
        val userId = 2L
        val requestVacation = RequestVacation(
            1L,
            FIRST_MONDAY,
            FIRST_FRIDAY,
            CURRENT_YEAR,
            "description"
        )
        val vacationDb = Vacation(
            id = 1L,
            startDate = FIRST_MONDAY,
            endDate = FIRST_FRIDAY,
            state = VacationState.PENDING,
            chargeYear = LocalDate.of(CURRENT_YEAR, 1, 1),
            userId = userId,
            description = ""
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val vacations = listOf(
            Vacation(
                id = 1L,
                startDate = FIRST_MONDAY,
                endDate = FIRST_TUESDAY,
                state = VacationState.PENDING,
                chargeYear = LocalDate.of(CURRENT_YEAR, 1, 1),
                userId = userId,
                description = ""
            ),
            Vacation(
                id = 2L,
                startDate = FIRST_THURSDAY,
                endDate = FIRST_FRIDAY,
                state = VacationState.ACCEPT,
                chargeYear = LocalDate.of(CURRENT_YEAR, 1, 1),
                userId = userId,
                description = ""
            )

        )
        doReturn(vacations).whenever(vacationRepository).find(eq(requestVacation.startDate), eq(requestVacation.endDate))

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_REQUEST_OVERLAPS))
    }

    @Test
    fun `should not update when request are zero days`() {
        val user = createUser()
        val requestVacation = RequestVacation(
            1,
            startDate = FIRST_SATURDAY,
            endDate = FIRST_SUNDAY,
            CURRENT_YEAR,
            "description"
        )
        val vacationDb = Vacation(
            requestVacation.id,
            requestVacation.startDate,
            requestVacation.endDate,
            VacationState.PENDING,
            2L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        doReturn(emptyList<Holiday>()).whenever(holidayRepository).findAllByDateBetween(
            requestVacation.startDate.atTime(LocalTime.MIN),
            requestVacation.endDate.atTime(23, 59, 59)
        )

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_REQUEST_EMPTY))
    }

    @Test
    fun `should delete valid request`() {
        doReturn(1L).whenever(user).id
        val vacationDb = Vacation(
            1L,
            tomorrow,
            tomorrow.plusDays(2),
            VacationState.PENDING,
            1L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canDeleteVacationPeriod(1L)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Success)
    }

    @Test
    fun `should delete valid past and pending vacation between yesterday and today`() {
       doReturn(1L).whenever(user).id
       val vacationDb = Vacation(
            1L,
            yesterday,
            yesterday.plusDays(2),
            VacationState.PENDING,
            1L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canDeleteVacationPeriod(1L)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Success)
    }

    @Test
    fun `should delete valid past and pending vacation before today`() {
        doReturn(1L).whenever(user).id
        val vacationDb = Vacation(
            1L,
            yesterday.minusDays(4),
            yesterday.plusDays(2),
            VacationState.PENDING,
            1L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canDeleteVacationPeriod(1L)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Success)
    }

    @Test
    fun `should delete valid request for a future and accepted vacation period`() {
        doReturn(1L).whenever(user).id
        val vacationDb = Vacation(
            1L,
            today.plusMonths(1),
            today.plusMonths(1).plusDays(3),
            VacationState.ACCEPT,
            1L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canDeleteVacationPeriod(1L)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Success)
    }

    @Test
    fun `should delete valid request for a future and pending vacation period`() {
        doReturn(1L).whenever(user).id
        val vacationDb = Vacation(
            1L,
            today.minusMonths(1),
            today.minusMonths(1).plusDays(5),
            VacationState.ACCEPT,
            1L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canDeleteVacationPeriod(1L)

        Assertions.assertThat(result)
            .isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD))

    }

    @Test
    fun `should not delete past and accepted vacation period`() {
        doReturn(1L).whenever(user).id
        val vacationDb = Vacation(
            1L,
            today.minusMonths(2),
            today.minusMonths(2).plusDays(5),
            VacationState.ACCEPT,
            1L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canDeleteVacationPeriod(1L)

        Assertions.assertThat(result)
            .isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD))
    }

    @Test
    fun `should delete pending for today`() {
        doReturn(1L).whenever(user).id
        val vacationDb = Vacation(
            1L,
            today,
            today,
            VacationState.PENDING,
            1L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canDeleteVacationPeriod(1L)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Success)
    }

    @Test
    fun `should not delete accepted for today`() {
        doReturn(1L).whenever(user).id
        val vacationDb = Vacation(
            1L,
            today,
            today,
            VacationState.ACCEPT,
            1L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canDeleteVacationPeriod(1L)

        Assertions.assertThat(result)
            .isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD))

    }

    @Test
    fun `should not delete on vacation not found`() {
        doReturn(null).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canDeleteVacationPeriod(1L)

        Assertions.assertThat(result)
            .isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_NOT_FOUND))
    }

    @Test
    fun `should not delete on closed period`() {
        doReturn(1L).whenever(user).id
        val vacationDb = Vacation(
            1L,
            today.minusYears(2),
            today.minusYears(2).plusDays(1),
            VacationState.PENDING,
            1L,
            description = "description",
            chargeYear = today
        )
        doReturn(vacationDb).whenever(vacationRepository).findById(eq(1L))

        val result = vacationValidator.canDeleteVacationPeriod(1L)

        Assertions.assertThat(result)
            .isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_RANGE_CLOSED))
    }

    private fun mockRequestVacation(requestVacation: RequestVacation) {
        val selectedDays = getSelectedDaysFrom(
            requestVacation.startDate,
            requestVacation.endDate
        )

        doReturn(selectedDays).whenever(remainingVacationService)
            .getRequestedVacationsSelectedYear(eq(requestVacation))
    }

    private fun createUser(): User {
        return User(
            id = 2L,
            hiringDate = LocalDate.of(CURRENT_YEAR-1, Month.FEBRUARY, 22),
            username = "jdoe",
            password = "secret",
            name = "John Doe",
            email = "jdoe@doe.com",
            dayDuration = 480,
            photoUrl = "",
            departmentId = 1L,
            role = Role(1L, "user"),
            agreementYearDuration = null,
            agreement = WorkingAgreement(1L, emptySet()),
            active = true
        )
    }

    private fun getSelectedDaysFrom(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val dateInterval = DateInterval.of(startDate, endDate)
        val calendar = calendarFactory.create(dateInterval)
        return calendar.getWorkableDays(dateInterval)
    }

    companion object {
        private val CURRENT_YEAR = LocalDate.now().year
        private val FIRST_MONDAY = LocalDate.of(CURRENT_YEAR, Month.JANUARY, 1)
            .with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))
        private val FIRST_TUESDAY = FIRST_MONDAY.plusDays(1)
        private val FIRST_WEDNESDAY = FIRST_MONDAY.plusDays(2)
        private val FIRST_THURSDAY = FIRST_MONDAY.plusDays(3)
        private val FIRST_FRIDAY = FIRST_MONDAY.plusDays(4)
        private val FIRST_SATURDAY = FIRST_MONDAY.plusDays(5)
        private val FIRST_SUNDAY = FIRST_MONDAY.plusDays(6)
        private val PREVIOUS_SUNDAY = FIRST_MONDAY.minusDays(1)
    }

}
