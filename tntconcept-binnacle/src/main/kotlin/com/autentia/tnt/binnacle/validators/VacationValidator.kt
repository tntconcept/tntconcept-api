package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.RequestVacation
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.VacationState.ACCEPT
import com.autentia.tnt.binnacle.entities.VacationState.PENDING
import com.autentia.tnt.binnacle.repositories.VacationRepository
import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.binnacle.services.VacationService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import javax.transaction.Transactional

@Singleton
internal class VacationValidator(
    private val vacationRepositorySecured: VacationRepository,
    private val vacationService: VacationService,
    private val holidayService: HolidayService,
) {

    fun canCreateVacationPeriod(requestVacation: RequestVacation, user: User): CreateVacationValidation {
        return when {
            !requestVacation.isDateRangeValid() -> CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.INVALID_DATE_RANGE)
            !isDateRangeOpen(requestVacation.startDate) -> CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_RANGE_CLOSED)
            isDateBeforeHiringDate(requestVacation.startDate, user.hiringDate) -> CreateVacationValidation.Failure(
                CreateVacationValidation.FailureReason.VACATION_BEFORE_HIRING_DATE
            )

            isVacationOverlaps(
                requestVacation.startDate,
                requestVacation.endDate,
                requestVacation.id
            ) -> CreateVacationValidation.Failure(CreateVacationValidation.FailureReason.VACATION_REQUEST_OVERLAPS)

            isRequestEmpty(requestVacation.startDate, requestVacation.endDate) -> CreateVacationValidation.Failure(
                CreateVacationValidation.FailureReason.VACATION_REQUEST_EMPTY
            )

            else -> CreateVacationValidation.Success
        }
    }

    private fun isRequestEmpty(startDate: LocalDate, endDate: LocalDate): Boolean {
        val publicHolidays = holidayService.findAllBetweenDate(startDate, endDate)
        return vacationService.getVacationPeriodDays(startDate, endDate, publicHolidays).isEmpty()
    }

    private fun isDateBeforeHiringDate(startDate: LocalDate, hiringDate: LocalDate): Boolean {
        return startDate.isBefore(hiringDate)
    }

    private fun isDateRangeOpen(startDate: LocalDate): Boolean {
        return startDate.year >= LocalDate.now().year - 1
    }

    private fun isPastAndAcceptedVacation(startDate: LocalDate, state: VacationState): Boolean {
        return ((startDate <= LocalDate.now()) && (state == ACCEPT))
    }


    private fun isVacationOverlaps(startDate: LocalDate, endDate: LocalDate, id: Long?): Boolean {
        return vacationRepositorySecured.findVacationsBetweenDate(startDate, endDate)
            .any { (it.state === ACCEPT || it.state === PENDING) && (id != it.id) }
    }

    @Transactional
    @ReadOnly
    fun canUpdateVacationPeriod(requestVacation: RequestVacation, user: User): UpdateVacationValidation {
        return when (requestVacation.isDateRangeValid()) {
            true -> {
                val vacationDb = vacationRepositorySecured.findById(requestVacation.id!!)
                return when {
                    vacationDb === null -> UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_NOT_FOUND)
                    !isDateRangeOpen(requestVacation.startDate) -> UpdateVacationValidation.Failure(
                        UpdateVacationValidation.FailureReason.VACATION_RANGE_CLOSED
                    )

                    !isVacationPending(vacationDb) -> UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED)

                    isDateBeforeHiringDate(
                        requestVacation.startDate,
                        user.hiringDate
                    ) -> UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_BEFORE_HIRING_DATE)

                    isVacationOverlaps(
                        requestVacation.startDate,
                        requestVacation.endDate,
                        requestVacation.id
                    ) -> UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_REQUEST_OVERLAPS)

                    isRequestEmpty(
                        requestVacation.startDate,
                        requestVacation.endDate
                    ) -> UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.VACATION_REQUEST_EMPTY)

                    else -> UpdateVacationValidation.Success(vacationDb)
                }
            }

            false -> UpdateVacationValidation.Failure(UpdateVacationValidation.FailureReason.INVALID_DATE_RANGE)
        }
    }

    @Transactional
    @ReadOnly
    fun canDeleteVacationPeriod(id: Long, user: User): DeleteVacationValidation {
        val vacationDb = vacationRepositorySecured.findById(id)
        return when {
            vacationDb === null -> DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_NOT_FOUND)
            !isDateRangeOpen(vacationDb.startDate) -> DeleteVacationValidation.Failure(DeleteVacationValidation.FailureReason.VACATION_RANGE_CLOSED)
            isPastAndAcceptedVacation(vacationDb.startDate, vacationDb.state) -> DeleteVacationValidation.Failure(
                DeleteVacationValidation.FailureReason.VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD
            )
            else -> DeleteVacationValidation.Success
        }
    }

    fun isVacationPending(vacation: Vacation): Boolean {
        return vacation.state === PENDING
    }
}

sealed class CreateVacationValidation {
    object Success : CreateVacationValidation()
    data class Failure(val reason: FailureReason) : CreateVacationValidation()

    enum class FailureReason {
        INVALID_DATE_RANGE,
        VACATION_RANGE_CLOSED,
        VACATION_BEFORE_HIRING_DATE,
        VACATION_REQUEST_OVERLAPS,
        VACATION_REQUEST_EMPTY
    }
}

sealed class UpdateVacationValidation {
    data class Success(val vacationDb: Vacation) : UpdateVacationValidation()
    data class Failure(val reason: FailureReason) : UpdateVacationValidation()

    enum class FailureReason {
        INVALID_DATE_RANGE,
        VACATION_ALREADY_ACCEPTED,
        VACATION_NOT_FOUND,
        VACATION_RANGE_CLOSED,
        VACATION_BEFORE_HIRING_DATE,
        VACATION_REQUEST_OVERLAPS,
        VACATION_REQUEST_EMPTY
    }
}

sealed class DeleteVacationValidation {
    object Success : DeleteVacationValidation()
    data class Failure(val reason: FailureReason) : DeleteVacationValidation()

    enum class FailureReason {
        VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD,
        VACATION_NOT_FOUND,
        VACATION_RANGE_CLOSED,
    }
}
