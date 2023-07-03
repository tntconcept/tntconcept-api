package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.config.createVacationRequestDTO
import com.autentia.tnt.binnacle.converters.CreateVacationResponseConverter
import com.autentia.tnt.binnacle.converters.RequestVacationConverter
import com.autentia.tnt.binnacle.core.domain.CreateVacationResponse
import com.autentia.tnt.binnacle.entities.dto.RequestVacationDTO
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.services.VacationMailService
import com.autentia.tnt.binnacle.services.VacationService
import com.autentia.tnt.binnacle.validators.CreateVacationValidation
import com.autentia.tnt.binnacle.validators.CreateVacationValidation.Failure
import com.autentia.tnt.binnacle.validators.CreateVacationValidation.FailureReason.*
import com.autentia.tnt.binnacle.validators.CreateVacationValidation.Success
import com.autentia.tnt.binnacle.validators.VacationValidator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.LocalDate
import java.util.*
import java.util.Locale.ENGLISH

@TestInstance(PER_CLASS)
internal class PrivateHolidayPeriodCreateUseCaseTest {

    private val vacationService = mock<VacationService>()
    private val userService = mock<UserService>()
    private val vacationValidator = mock<VacationValidator>()
    private val vacationMailService = mock<VacationMailService>()

    private val privateHolidayPeriodCreateUseCase =
        PrivateHolidayPeriodCreateUseCase(
            vacationService,
            userService,
            vacationValidator,
            CreateVacationResponseConverter(),
            RequestVacationConverter(),
            vacationMailService
        )

    @Test
    fun `create new vacation period sending the email`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(Success).whenever(vacationValidator).canCreateVacationPeriod(any(), eq(USER))

        doReturn(holidaysTodayTomorrow).whenever(vacationService).createVacationPeriod(any(), eq(USER))

        privateHolidayPeriodCreateUseCase.create(createVacationRequestDTO(TODAY, TOMORROW), ENGLISH)

        verify(vacationMailService, times(1)).sendRequestVacationsMail(
            USER.username,
            TODAY,
            TOMORROW,
            "Lorem ipsum...",
            ENGLISH
        )
    }

    @Test
    fun `FAIL when more than 5 days of NEXT year's vacation are requested for the current year`() {

        doReturn(USER).whenever(userService)
            .getAuthenticatedUser()
        doReturn(Success).whenever(vacationValidator).canCreateVacationPeriod(any(), eq(USER))

        doThrow(MaxNextYearRequestVacationException("Invalid vacation request for more than 5 days for next year"))
            .whenever(vacationService).createVacationPeriod(any(), eq(USER))
        assertThrows<MaxNextYearRequestVacationException> {
            privateHolidayPeriodCreateUseCase.create(moreThanFiveDaysNextYearVacation, ENGLISH)
        }
    }

    private fun createVacationFailProvider() = arrayOf(
        arrayOf(
            "Invalid date range exception",
            DateRangeException(AFTER_TOMORROW, TODAY),
            Failure(INVALID_DATE_RANGE),
            invalidVacationRange,
            DateRangeException(
                AFTER_TOMORROW, TODAY
            ).message
        ),
        arrayOf(
            "Vacation range closed exception",
            VacationRangeClosedException(),
            Failure(VACATION_RANGE_CLOSED),
            vacationForRangeClosed,
            VacationRangeClosedException().message
        ),
        arrayOf(
            "Vacation before hiring date exception",
            VacationBeforeHiringDateException(),
            Failure(VACATION_BEFORE_HIRING_DATE),
            vacationRequestBeforeHiringDate,
            VacationBeforeHiringDateException().message
        ),
        arrayOf(
            "Vacation overlaps exception",
            VacationRequestOverlapsException(),
            Failure(VACATION_REQUEST_OVERLAPS),
            overlappingVacation,
            VacationRequestOverlapsException().message
        ),
        arrayOf(
            "Vacation empty exception",
            VacationRequestEmptyException(),
            Failure(VACATION_REQUEST_EMPTY),
            emptyVacation,
            VacationRequestEmptyException().message
        ),
    )

    @ParameterizedTest
    @MethodSource("createVacationFailProvider")
    fun `fail if try to create a vacation and an exception is thrown`(
        testDescription: String,
        expectedException: BinnacleException,
        failureReason: CreateVacationValidation,
        vacationDTO: RequestVacationDTO,
        expectedExceptionMessage: String,
    ) {
        doReturn(USER).whenever(userService).getAuthenticatedUser()
        doReturn(failureReason).whenever(vacationValidator).canCreateVacationPeriod(any(), eq(USER))

        val exception = assertThrows<BinnacleException> {
            privateHolidayPeriodCreateUseCase.create(vacationDTO, ENGLISH)
        }
        Assertions.assertInstanceOf(expectedException.javaClass, exception)
        Assertions.assertEquals(expectedExceptionMessage, exception.message)
    }

    private companion object {
        private val USER = createUser()
        private val TODAY = LocalDate.now()
        private val TODAY_NEXT_YEAR = LocalDate.now().plusYears(1)
        private val SIX_DAYS_NEXT_YEAR = LocalDate.now().plusYears(1).plusDays(6)
        private val TODAY_TWO_YEARS_AGO = LocalDate.now().minusYears(2)
        private val TOMORROW = LocalDate.now().plusDays(1)
        private val AFTER_TOMORROW = LocalDate.now().plusDays(1)
        private val TOMORROW_TWO_YEARS_AGO = LocalDate.now().minusYears(2).plusDays(1)

        private val CURRENT_YEAR = LocalDate.now().year
        val holidaysTodayTomorrow = listOf(
            CreateVacationResponse(
                startDate = TODAY,
                endDate = TOMORROW,
                days = 1,
                chargeYear = CURRENT_YEAR
            )
        ).toMutableList()

        private val invalidVacationRange = RequestVacationDTO(startDate = AFTER_TOMORROW, endDate = TODAY)
        private val vacationForRangeClosed =
            RequestVacationDTO(startDate = TODAY_TWO_YEARS_AGO, endDate = TOMORROW_TWO_YEARS_AGO)
        private val vacationRequestBeforeHiringDate =
            RequestVacationDTO(startDate = USER.hiringDate.minusDays(3), endDate = USER.hiringDate.minusDays(2))
        private val emptyVacation = RequestVacationDTO(startDate = TODAY, endDate = TODAY)
        private val overlappingVacation = RequestVacationDTO(startDate = TODAY, endDate = TOMORROW)
        private val moreThanFiveDaysNextYearVacation =
            RequestVacationDTO(startDate = TODAY_NEXT_YEAR, endDate = SIX_DAYS_NEXT_YEAR)

    }

}
