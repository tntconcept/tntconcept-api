package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.exception.VacationAcceptedPastPeriodStateException
import com.autentia.tnt.binnacle.exception.VacationNotFoundException
import com.autentia.tnt.binnacle.exception.VacationRangeClosedException
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.services.VacationService
import com.autentia.tnt.binnacle.validators.DeleteVacationValidation
import com.autentia.tnt.binnacle.validators.VacationValidator
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class PrivateHolidayPeriodDeleteUseCase internal constructor(
    private val userService: UserService,
    private val vacationValidator: VacationValidator,
    private val vacationService: VacationService
) {
    @Transactional
    fun delete(id: Long) {
        val user = userService.getAuthenticatedUser()

        when (val result = vacationValidator.canDeleteVacationPeriod(id, user)) {
            is DeleteVacationValidation.Success -> vacationService.deleteVacationPeriod(id, user.id)
            is DeleteVacationValidation.Failure ->
                when (result.reason) {
                    DeleteVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD -> throw VacationAcceptedPastPeriodStateException()
                    DeleteVacationValidation.FailureReason.VACATION_NOT_FOUND -> throw VacationNotFoundException(id)
                    DeleteVacationValidation.FailureReason.VACATION_RANGE_CLOSED -> throw VacationRangeClosedException()
                }
        }
    }

}
