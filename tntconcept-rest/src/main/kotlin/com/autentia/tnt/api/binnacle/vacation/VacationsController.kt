package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
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
private const val NO_MORE_DAYS_LEFT_IN_YEAR = "NO_MORE_DAYS_LEFT_IN_YEAR"

@Controller("/api/vacations")
@Validated
internal class VacationsController(
    private val privateHolidaysByChargeYearUseCase: PrivateHolidaysByChargeYearUseCase,
    private val privateHolidayDetailsUseCase: PrivateHolidayDetailsUseCase,
    private val privateHolidayPeriodCreateUseCase: PrivateHolidayPeriodCreateUseCase,
    private val privateHolidayPeriodUpdateUseCase: PrivateHolidayPeriodUpdateUseCase,
    private val privateHolidayPeriodDeleteUseCase: PrivateHolidayPeriodDeleteUseCase,
) {

    @Deprecated("Use VacationController instead")
    @Get
    @Operation(summary = "Retrieves holidays within a given charge year.")
    internal fun getPrivateHolidaysByChargeYear(chargeYear: Int): HolidaysResponse =
        HolidaysResponse.from(privateHolidaysByChargeYearUseCase.get(chargeYear))

    @Get("/details")
    @Operation(summary = "Retrieves details for a holiday within a given charge year.")
    internal fun getPrivateHolidayDetails(chargeYear: Int): VacationDetailsResponse {
        val vacationsByChargeYear = privateHolidaysByChargeYearUseCase.get(chargeYear).vacations
        return VacationDetailsResponse.from(privateHolidayDetailsUseCase.get(chargeYear, vacationsByChargeYear))
    }

    @Post
    @Operation(summary = "Creates a holiday period.")
    internal fun createPrivateHolidayPeriod(
        @Body @Valid createVacationRequest: CreateVacationRequest,
        locale: Locale,
    ): CreateVacationResponse =
        CreateVacationResponse.from(privateHolidayPeriodCreateUseCase.create(createVacationRequest.toDto(), locale))

    @Put
    @Operation(summary = "Updates a holiday period.")
    internal fun updatePrivateHolidayPeriod(
        @Body @Valid createVacationRequest: CreateVacationRequest,
        locale: Locale,
    ): CreateVacationResponse =
        CreateVacationResponse.from(privateHolidayPeriodUpdateUseCase.update(createVacationRequest.toDto(), locale))

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
        e: VacationAcceptedPastPeriodStateException,
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

    @Error
    internal fun onVacationRequestEmpty(request: HttpRequest<*>, e: NoMoreDaysLeftInYearException) =
        HttpResponse.badRequest(ErrorResponse(NO_MORE_DAYS_LEFT_IN_YEAR, e.message))

}
