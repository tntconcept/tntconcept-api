package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.exception.BinnacleException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.exception.VacationAcceptedPastPeriodStateException
import com.autentia.tnt.binnacle.exception.VacationNotFoundException
import com.autentia.tnt.binnacle.exception.VacationRangeClosedException
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.services.VacationService
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation.Failure
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation.Success
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation.FailureReason.VACATION_NOT_FOUND
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation.FailureReason.VACATION_RANGE_CLOSED
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation.FailureReason.USER_UNAUTHORIZED
import com.autentia.tnt.binnacle.validators.VacationValidator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@TestInstance(Lifecycle.PER_CLASS)
internal class PrivateHolidayPeriodDeleteUseCaseTest {

    private val vacationService = mock<VacationService>()
    private val userService = mock<UserService>()
    private val vacationValidator = mock<VacationValidator>()

    private val privateHolidayPeriodDeleteUseCase = PrivateHolidayPeriodDeleteUseCase(userService, vacationValidator, vacationService)

    @Test
    fun `delete a vacation period`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(Success).whenever(vacationValidator).canDeleteVacationPeriod(vacationID, USER)

        doNothing().whenever(vacationService).deleteVacationPeriod(vacationID, USER.id)

        privateHolidayPeriodDeleteUseCase.delete(vacationID)

        verify(vacationService, times(1)).deleteVacationPeriod(vacationID, USER.id)
    }

    @Test
    fun `FAIL when the vacation period to delete is not found in the database`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(Failure(VACATION_NOT_FOUND)).whenever(vacationValidator).canDeleteVacationPeriod(vacationID, USER)

        assertThrows<VacationNotFoundException> {
            privateHolidayPeriodDeleteUseCase.delete(vacationID)
        }
    }

    private fun deleteVacationFailProvider() = arrayOf(
        arrayOf("User permission exception", UserPermissionException(), Failure(USER_UNAUTHORIZED), vacationID, UserPermissionException().message),
        arrayOf("Vacation range closed exception", VacationRangeClosedException(), Failure(VACATION_RANGE_CLOSED), vacationID, VacationRangeClosedException().message),
        arrayOf("Vacation accepted for past period", VacationAcceptedPastPeriodStateException(), Failure(VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD), vacationID, VacationAcceptedPastPeriodStateException().message),
    )
    @ParameterizedTest
    @MethodSource("deleteVacationFailProvider")
    fun `fail if try to delete a vacation and an exception is thrown`(
        testDescription: String,
        expectedException: BinnacleException,
        failureReason: DeleteVacationValidation,
        vacationId: Long,
        expectedExceptionMessage: String
    ) {
        doReturn(USER).whenever(userService).getAuthenticatedUser()
        doReturn(failureReason).whenever(vacationValidator).canDeleteVacationPeriod(vacationId, USER)

        val exception = assertThrows<BinnacleException> {
            privateHolidayPeriodDeleteUseCase.delete(vacationId)
        }
        Assertions.assertInstanceOf(expectedException.javaClass, exception)
        Assertions.assertEquals(expectedExceptionMessage, exception.message)
    }

    private companion object{
        private val USER = createUser()
        private const val vacationID = 10L
    }

}
