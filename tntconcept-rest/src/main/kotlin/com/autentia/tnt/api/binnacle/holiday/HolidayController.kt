package com.autentia.tnt.api.binnacle.holiday

import com.autentia.tnt.api.binnacle.vacation.HolidayResponse
import com.autentia.tnt.binnacle.usecases.UserHolidaysBetweenDatesUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate

@Controller("/api/holidays")
internal class HolidayController(
    private val userHolidaysBetweenDatesUseCase: UserHolidaysBetweenDatesUseCase
) {

    @Operation(summary = "Retrieves existing holidays within a period")
    @Get
    fun getHolidaysBetweenDate(startDate: LocalDate, endDate: LocalDate): HolidayResponse =
        HolidayResponse.from(userHolidaysBetweenDatesUseCase.getHolidays(startDate, endDate))

}
