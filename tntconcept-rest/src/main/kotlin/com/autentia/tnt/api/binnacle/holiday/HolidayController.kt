package com.autentia.tnt.api.binnacle.holiday

import com.autentia.tnt.api.binnacle.vacation.HolidayResponse
import com.autentia.tnt.binnacle.usecases.UserHolidaysBetweenDatesUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/holiday")
internal class HolidayController(
    private val userHolidaysBetweenDatesUseCase: UserHolidaysBetweenDatesUseCase
) {

    @Operation(summary = "Retrieves existing holidays given a year")
    @Get
    fun getHolidaysBetweenDate(@QueryValue year: Int?): HolidayResponse =
        HolidayResponse.from(userHolidaysBetweenDatesUseCase.getHolidays(year))
}
