package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.CreateVacationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.entities.dto.RequestVacationDTO
import com.autentia.tnt.binnacle.entities.dto.VacationDetailsDTO
import com.autentia.tnt.binnacle.exception.DateRangeException
import com.autentia.tnt.binnacle.exception.MaxNextYearRequestVacationException
import com.autentia.tnt.binnacle.exception.VacationAcceptedPastPeriodStateException
import com.autentia.tnt.binnacle.exception.VacationAcceptedStateException
import com.autentia.tnt.binnacle.exception.VacationBeforeHiringDateException
import com.autentia.tnt.binnacle.exception.VacationRangeClosedException
import com.autentia.tnt.binnacle.exception.VacationRequestEmptyException
import com.autentia.tnt.binnacle.exception.VacationRequestOverlapsException
import com.autentia.tnt.binnacle.usecases.PrivateHolidayDetailsUseCase
import com.autentia.tnt.binnacle.usecases.PrivateHolidayPeriodCreateUseCase
import com.autentia.tnt.binnacle.usecases.PrivateHolidayPeriodDeleteUseCase
import com.autentia.tnt.binnacle.usecases.PrivateHolidayPeriodUpdateUseCase
import com.autentia.tnt.binnacle.usecases.PrivateHolidaysByChargeYearUseCase
import com.autentia.tnt.binnacle.usecases.PrivateHolidaysPeriodDaysUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate
import java.util.*
import javax.validation.Valid

//REST Errors
private const val INVALID_DATE_RANGE = "INVALID_DATE_RANGE"
private const val INVALID_NEXT_YEAR_VACATION_DAYS_REQUEST = "INVALID_NEXT_YEAR_VACATION_DAYS_REQUEST"
private const val VACATION_ALREADY_ACCEPTED = "VACATION_ALREADY_ACCEPTED"
private const val VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD = "VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD"
private const val VACATION_RANGE_CLOSED = "VACATION_RANGE_CLOSED"
private const val VACATION_BEFORE_HIRING_DATE = "VACATION_BEFORE_HIRING_DATE"
private const val VACATION_REQUEST_OVERLAPS = "VACATION_REQUEST_OVERLAPS"
private const val VACATION_REQUEST_EMPTY = "VACATION_REQUEST_EMPTY"

@Controller("/api/vacations")
@Validated
internal class VacationController(
    private val privateHolidaysByChargeYearUseCase: PrivateHolidaysByChargeYearUseCase,
    private val privateHolidayDetailsUseCase: PrivateHolidayDetailsUseCase,
    private val privateHolidaysPeriodDaysUseCase: PrivateHolidaysPeriodDaysUseCase,
    private val privateHolidayPeriodCreateUseCase: PrivateHolidayPeriodCreateUseCase,
    private val privateHolidayPeriodUpdateUseCase: PrivateHolidayPeriodUpdateUseCase,
    private val privateHolidayPeriodDeleteUseCase: PrivateHolidayPeriodDeleteUseCase
    ) {

        @Get
        @Operation(summary = "Retrieves holidays within a given charge year.")
        internal fun getPrivateHolidaysByChargeYear(chargeYear: Int): HolidayResponseDTO =
            privateHolidaysByChargeYearUseCase.get(chargeYear)

        @Get("/details")
        @Operation(summary = "Retrieves details for a holiday within a given charge year.")
        internal fun getPrivateHolidayDetails(chargeYear: Int): VacationDetailsDTO {
            val vacationsByChargeYear = privateHolidaysByChargeYearUseCase.get(chargeYear).vacations
            return privateHolidayDetailsUseCase.get(chargeYear, vacationsByChargeYear)
        }

        @Get("/days")
        @Operation(summary = "Retrieves holidays within a given period.")
        internal fun getPrivateHolidaysPeriodDays(startDate: LocalDate, endDate: LocalDate): Int =
            privateHolidaysPeriodDaysUseCase.get(startDate, endDate)

        @Post
        @Operation(summary = "Creates a holiday period.")
        internal fun createPrivateHolidayPeriod(
        @Body @Valid requestVacationDTO: RequestVacationDTO,
        locale: Locale
    ): List<CreateVacationResponseDTO> =
        privateHolidayPeriodCreateUseCase.create(requestVacationDTO, locale)

    @Put
    @Operation(summary = "Updates a holiday period.")
    internal fun updatePrivateHolidayPeriod(
        @Body @Valid dto: RequestVacationDTO,
        locale: Locale
    ): List<CreateVacationResponseDTO> =
        privateHolidayPeriodUpdateUseCase.update(dto, locale)

    @Delete("/{id}")
    @Operation(summary = "Deletes a holiday period by a given ID.")
    internal fun deletePrivateHolidayPeriod(id: Long) {
        privateHolidayPeriodDeleteUseCase.delete(id)
    }

    @Error
    internal fun onDateRangeException(request: HttpRequest<*>, e: DateRangeException) =
        HttpResponse.badRequest(ErrorResponse(INVALID_DATE_RANGE, e.message))

    @Error
    internal fun onMaxNextYearVacationException(request: HttpRequest<*>, e: MaxNextYearRequestVacationException) =
        HttpResponse.badRequest(ErrorResponse(INVALID_NEXT_YEAR_VACATION_DAYS_REQUEST, e.message))

    @Error
    internal fun onVacationAlreadyAcceptedException(request: HttpRequest<*>, e: VacationAcceptedStateException) =
        HttpResponse.badRequest(ErrorResponse(VACATION_ALREADY_ACCEPTED, e.message))

    @Error
    internal fun onVacationAcceptedPastPeriodStateException(
        request: HttpRequest<*>,
        e: VacationAcceptedPastPeriodStateException
    ) = HttpResponse.badRequest(ErrorResponse(VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD, e.message))

    @Error
    internal fun onVacationRangeClosedException(request: HttpRequest<*>, e: VacationRangeClosedException) =
        HttpResponse.badRequest(ErrorResponse(VACATION_RANGE_CLOSED, e.message))

    @Error
    internal fun onVacationBeforeHiringDateException(request: HttpRequest<*>, e: VacationBeforeHiringDateException) =
        HttpResponse.badRequest(ErrorResponse(VACATION_BEFORE_HIRING_DATE, e.message))

    @Error
    internal fun onVacationRequestOverlaps(request: HttpRequest<*>, e: VacationRequestOverlapsException) =
        HttpResponse.badRequest(ErrorResponse(VACATION_REQUEST_OVERLAPS, e.message))

    @Error
    internal fun onVacationRequestEmpty(request: HttpRequest<*>, e: VacationRequestEmptyException) =
        HttpResponse.badRequest(ErrorResponse(VACATION_REQUEST_EMPTY, e.message))

}
