package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.exception.BinnacleException
import com.autentia.tnt.binnacle.exception.VacationAcceptedPastPeriodStateException
import com.autentia.tnt.binnacle.exception.VacationNotFoundException
import com.autentia.tnt.binnacle.exception.VacationRangeClosedException
import com.autentia.tnt.binnacle.repositories.VacationRepository
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation.Failure
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation.FailureReason.*
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation.Success
import com.autentia.tnt.binnacle.validators.VacationValidator
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.*

@TestInstance(Lifecycle.PER_CLASS)
internal class PrivateHolidayPeriodDeleteUseCaseTest {

    private val vacationRepository = mock<VacationRepository>()
    private val securityService = mock<SecurityService>()
    private val vacationValidator = mock<VacationValidator>()

    private val privateHolidayPeriodDeleteUseCase =
        PrivateHolidayPeriodDeleteUseCase(securityService, vacationValidator, vacationRepository)

    @Test
    fun `delete a vacation period`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))
        whenever(vacationValidator.canDeleteVacationPeriod(vacationID)).thenReturn(Success)

        privateHolidayPeriodDeleteUseCase.delete(vacationID)

        verify(vacationRepository, times(1)).deleteById(vacationID)
    }

    @Test
    fun `FAIL when the vacation period to delete is not found in the database`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))
        whenever(vacationValidator.canDeleteVacationPeriod(vacationID)).thenReturn(Failure(VACATION_NOT_FOUND))

        assertThrows<VacationNotFoundException> {
            privateHolidayPeriodDeleteUseCase.delete(vacationID)
        }
    }

    private fun deleteVacationFailProvider() = arrayOf(
        arrayOf(
            "Vacation range closed exception",
            VacationRangeClosedException(),
            Failure(VACATION_RANGE_CLOSED),
            vacationID,
            VacationRangeClosedException().message
        ),
        arrayOf(
            "Vacation accepted for past period",
            VacationAcceptedPastPeriodStateException(),
            Failure(VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD),
            vacationID,
            VacationAcceptedPastPeriodStateException().message
        ),
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
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))
        whenever(vacationValidator.canDeleteVacationPeriod(vacationId)).thenReturn(failureReason)

        val exception = assertThrows<BinnacleException> {
            privateHolidayPeriodDeleteUseCase.delete(vacationId)
        }
        Assertions.assertInstanceOf(expectedException.javaClass, exception)
        Assertions.assertEquals(expectedExceptionMessage, exception.message)
    }

    private companion object {
        private const val userId = 5L
        private const val vacationID = 10L
        private val authentication =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))
    }

}
