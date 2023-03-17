package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.RequestVacation
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.Role
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.WorkingAgreement
import com.autentia.tnt.binnacle.repositories.VacationRepository
import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.binnacle.services.VacationService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.given
import java.time.LocalDate
import java.time.Month
import java.util.Optional

internal class VacationValidatorTest {

    private val vacationRepository = mock(VacationRepository::class.java)
    private val vacationService = mock(VacationService::class.java)
    private val holidayService = mock(HolidayService::class.java)
    private val calendarFactory = CalendarFactory(holidayService)
    private val user = mock(User::class.java)

    private val vacationValidator =
        VacationValidator(vacationRepository, vacationService, holidayService, calendarFactory)

    // Characterized use cases objects
    private val today = LocalDate.now()
    private val tomorrow = today.plusDays(1)
    private val yesterday = today.minusDays(1)

    @Test
    fun `should create valid request`() {
        val requestVacation = RequestVacation(null, today, today.plusDays(1), "description")
        val user = createUser()
        val holidays= emptyList<Holiday>()

        given(holidayService.findAllBetweenDate(requestVacation.startDate,requestVacation.endDate)).willReturn(holidays)

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result).isEqualTo(CreateVacationValidation.Success)

    }

    @Test
    fun `should not create invalid dates`() {
        val requestVacation = RequestVacation(null, today, yesterday, "description")

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.INVALID_DATE_RANGE))
    }

    @Test
    fun `should not create on closed years`() {
        val requestVacation = RequestVacation(
            null,
            today.minusYears(2),
            today.minusYears(2).plusDays(1),
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
            LocalDate.of(user.hiringDate.year, user.hiringDate.month.minus(1), 5),
            "description"
        )

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_BEFORE_HIRING_DATE))
    }

    @Test
    fun `should not create overlaps vacations `() {
        val user = createUser()
        val requestVacation = RequestVacation(null, LocalDate.of(today.year, Month.MARCH, 15), LocalDate.of(today.year, Month.MARCH, 20), "description")
        val userId=2L
        val vacations = listOf(
            Vacation(
                id = 1L,
                startDate = LocalDate.of(today.year, Month.MARCH, 15),
                endDate = LocalDate.of(today.year, Month.MARCH, 16),
                state = VacationState.ACCEPT,
                chargeYear = LocalDate.of(today.year, Month.MARCH, 1),
                userId = userId,
                description = ""
            ),
            Vacation(
                id = 2L,
                startDate = LocalDate.of(today.year, Month.MARCH, 18),
                endDate = LocalDate.of(today.year, Month.APRIL, 1),
                state = VacationState.PENDING,
                chargeYear = LocalDate.of(today.year, Month.MARCH, 1),
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
        given(vacationRepository.getVacationsBetweenDate(requestVacation.startDate,requestVacation.endDate,userId)).willReturn(vacations)

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result).isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_REQUEST_OVERLAPS))
    }

    @Test
    fun `should not create when request are zero days`() {
        val user = createUser()
        val requestVacation = RequestVacation(
            null,
            LocalDate.of(2023, Month.MARCH, 11),
            LocalDate.of(2023, Month.MARCH, 11),
            "description"
        )
        val holidays= emptyList<Holiday>()

        given(holidayService.findAllBetweenDate(requestVacation.startDate,requestVacation.endDate)).willReturn(holidays)

        val result = vacationValidator.canCreateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_REQUEST_EMPTY))
    }

    @Test
    fun `should update valid request`() {
        val user = createUser()
        val requestVacation = RequestVacation(1L, today, tomorrow, "description")
        val vacationDb = Vacation(
            requestVacation.id,
            requestVacation.startDate,
            requestVacation.endDate,
            VacationState.PENDING,
            2L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val holidays= emptyList<Holiday>()

        given(holidayService.findAllBetweenDate(requestVacation.startDate,requestVacation.endDate)).willReturn(holidays)

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result).isEqualTo(UpdateVacationValidation.Success(vacationDb))
    }

    @Test
    fun `should not update invalid dates`() {
        val requestVacation = RequestVacation(1L, today, yesterday, "description")

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.INVALID_DATE_RANGE))
    }

    @Test
    fun `should not update not found vacation`() {
        val requestVacation = RequestVacation(1L, today, tomorrow, "description")

        given(vacationRepository.findById(1L)).willReturn(Optional.empty())

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_NOT_FOUND))
    }

    @Test
    fun `should not update not pending vacations`() {
        given(user.id).willReturn(1L)
        val requestVacation = RequestVacation(1L, today, tomorrow, "description")
        val vacationDb = Vacation(
            requestVacation.id,
            requestVacation.startDate,
            requestVacation.endDate,
            VacationState.ACCEPT,
            1L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED))
    }

    @Test
    fun `should not update not user vacations`() {
        given(user.id).willReturn(1L)
        val requestVacation = RequestVacation(1L, today, tomorrow, "description")
        val vacationDb = Vacation(
            requestVacation.id,
            requestVacation.startDate,
            requestVacation.endDate,
            VacationState.PENDING,
            2L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)

        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.USER_UNAUTHORIZED))
    }

    @Test
    fun `should not update on closed years`() {
        val requestVacation = RequestVacation(
            1,
            today.minusYears(2),
            today.minusYears(2).plusDays(1),
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
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

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
            LocalDate.of(user.hiringDate.year, user.hiringDate.month.minus(1), 5),
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
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)
        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_BEFORE_HIRING_DATE))
    }

    @Test
    fun `should not update overlaps vacation`() {
        val user = createUser()
        val userId=2L
        val requestVacation = RequestVacation(3L, LocalDate.of(today.year, Month.MARCH, 15), LocalDate.of(today.year, Month.MARCH, 20), "description")
        val vacationDb = Vacation(
            id = 3L,
            startDate = LocalDate.of(today.year, Month.MARCH, 18),
            endDate = LocalDate.of(today.year, Month.MARCH, 20),
            state = VacationState.PENDING,
            chargeYear = LocalDate.of(today.year, Month.MARCH, 1),
            userId = userId,
            description = ""
        )
        given(vacationRepository.findById(3L)).willReturn(Optional.of(vacationDb))

        val vacations = listOf(
            Vacation(
                id = 1L,
                startDate = LocalDate.of(today.year, Month.MARCH, 15),
                endDate = LocalDate.of(today.year, Month.MARCH, 16),
                state = VacationState.ACCEPT,
                chargeYear = LocalDate.of(today.year, Month.MARCH, 1),
                userId = userId,
                description = ""
            ),
            Vacation(
                id = 2L,
                startDate = LocalDate.of(today.year, Month.MARCH, 20),
                endDate = LocalDate.of(today.year, Month.APRIL, 1),
                state = VacationState.PENDING,
                chargeYear = LocalDate.of(today.year, Month.MARCH, 1),
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
            ,Vacation(
                id = 3L,
                startDate = LocalDate.of(today.year, Month.MARCH, 18),
                endDate = LocalDate.of(today.year, Month.MARCH, 20),
                state = VacationState.PENDING,
                chargeYear = LocalDate.of(today.year, Month.MARCH, 1),
                userId = userId,
                description = ""
            )

        )
        given(vacationRepository.getVacationsBetweenDate(requestVacation.startDate,requestVacation.endDate,userId)).willReturn(vacations)

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)
        Assertions.assertThat(result).isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_REQUEST_OVERLAPS))
    }

    @Test
    fun `should not update when request are zero days`() {
        val user = createUser()
        val requestVacation = RequestVacation(
            1,
            startDate = LocalDate.of(2023, Month.MARCH, 11),
            endDate = LocalDate.of(2023, Month.MARCH, 11),
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
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val holidays= emptyList<Holiday>()

        given(holidayService.findAllBetweenDate(requestVacation.startDate,requestVacation.endDate)).willReturn(holidays)

        val result = vacationValidator.canUpdateVacationPeriod(requestVacation, user)
        Assertions.assertThat(result)
            .isEqualTo(UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_REQUEST_EMPTY))
    }

    @Test
    fun `should delete valid request`() {
        given(user.id).willReturn(1L)
        val vacationDb = Vacation(
            1L,
            tomorrow,
            tomorrow.plusDays(2),
            VacationState.PENDING,
            1L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Success)
    }
    @Test
    fun `should delete valid past and pending vacation between yesterday and today`() {
        given(user.id).willReturn(1L)
        val vacationDb = Vacation(
            1L,
            yesterday,
            yesterday.plusDays(2),
            VacationState.PENDING,
            1L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Success)
    }

    @Test
    fun `should delete valid past and pending vacation before today`() {
        given(user.id).willReturn(1L)
        val vacationDb = Vacation(
            1L,
            yesterday.minusDays(4),
            yesterday.plusDays(2),
            VacationState.PENDING,
            1L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Success)
    }

    @Test
    fun `should delete valid request for a future and accepted vacation period`() {
        given(user.id).willReturn(1L)
        val vacationDb = Vacation(
            1L,
            today.plusMonths(1),
            today.plusMonths(1).plusDays(3),
            VacationState.ACCEPT,
            1L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Success)
    }
    @Test
    fun `should delete valid request for a future and pending vacation period`() {
        given(user.id).willReturn(1L)
        val vacationDb = Vacation(
            1L,
            today.minusMonths(1),
            today.minusMonths(1).plusDays(5),
            VacationState.ACCEPT,
            1L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result)
            .isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD))

    }
    @Test
    fun `should not delete past and accepted vacation period`() {
        given(user.id).willReturn(1L)
        val vacationDb = Vacation(
            1L,
            today.minusMonths(2),
            today.minusMonths(2).plusDays(5),
            VacationState.ACCEPT,
            1L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD))
    }
    @Test
    fun `should delete pending for today`() {
        given(user.id).willReturn(1L)
        val vacationDb = Vacation(
            1L,
            today,
            today,
            VacationState.PENDING,
            1L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Success)
    }
    @Test
    fun `should not delete accepted for today`() {
        given(user.id).willReturn(1L)
        val vacationDb = Vacation(
            1L,
            today,
            today,
            VacationState.ACCEPT,
            1L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result).isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD))

    }


    @Test
    fun `should vacation not found`() {
        given(vacationRepository.findById(1L)).willReturn(Optional.empty())

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result)
            .isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_NOT_FOUND))
    }

    @Test
    fun `should not delete not user vacation`() {
        given(user.id).willReturn(1L)
        val vacationDb = Vacation(
            1L,
            tomorrow,
            tomorrow.plusDays(2),
            VacationState.PENDING,
            2L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result)
            .isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.USER_UNAUTHORIZED))
    }

    @Test
    fun `should not delete on closed period`() {
        given(user.id).willReturn(1L)
        val vacationDb = Vacation(
            1L,
            today.minusYears(2),
            today.minusYears(2).plusDays(1),
            VacationState.PENDING,
            1L,
            description = "description",
            chargeYear = today
        )
        given(vacationRepository.findById(1L)).willReturn(Optional.of(vacationDb))

        val result = vacationValidator.canDeleteVacationPeriod(1L, user)

        Assertions.assertThat(result)
            .isEqualTo(DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_RANGE_CLOSED))
    }

    private fun createUser(): User {
        return User(
            id = 2L,
            hiringDate = LocalDate.of(2022, Month.FEBRUARY, 22),
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

}
