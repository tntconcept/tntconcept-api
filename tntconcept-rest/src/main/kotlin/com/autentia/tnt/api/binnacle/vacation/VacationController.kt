package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.binnacle.usecases.UsersVacationsFromPeriodUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate

@Controller("/api/vacation")
@Validated
class VacationController(
    private val usersVacationsFromPeriod: UsersVacationsFromPeriodUseCase
) {

    @Get
    @Operation(summary = "Retrieves holidays within a given a period and a list of userIds.")
    internal fun getUsersVacationsFromPeriod(
        @QueryValue startDate: LocalDate,
        @QueryValue endDate: LocalDate
    ): List<VacationResponse> =
        usersVacationsFromPeriod.getVacationsByPeriod(startDate, endDate).map { VacationResponse.from(it) }

}