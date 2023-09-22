package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.binnacle.exception.DateRangeException
import com.autentia.tnt.binnacle.usecases.UsersVacationsFromPeriodUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate

private const val INVALID_DATE_RANGE = "INVALID_DATE_RANGE"

@Controller("/api/vacation")
@Validated
class VacationController(
    private val usersVacationsFromPeriod: UsersVacationsFromPeriodUseCase
) {

    @Get
    @Operation(summary = "Retrieves holidays within a given a period.")
    internal fun getUsersVacationsFromPeriod(
        @QueryValue startDate: LocalDate,
        @QueryValue endDate: LocalDate
    ): List<VacationResponse> =
        usersVacationsFromPeriod.getVacationsByPeriod(startDate, endDate).map { VacationResponse.from(it) }

    @Error
    internal fun onDateRangeException(request: HttpRequest<*>, e: DateRangeException) =
        HttpResponse.badRequest(ErrorResponse(INVALID_DATE_RANGE, e.message))

}