package com.autentia.tnt.api.binnacle.holiday

import com.autentia.tnt.api.binnacle.vacation.HolidaysResponse
import com.autentia.tnt.binnacle.usecases.UserHolidaysBetweenDatesUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate

@Deprecated("Use instead HolidayController")
@Controller("/api/holidays")
internal class HolidaysController(
    private val userHolidaysBetweenDatesUseCase: UserHolidaysBetweenDatesUseCase
) {

    @Operation(summary = "Retrieves existing holidays within a period")
    @Get
    fun getHolidaysBetweenDate(startDate: LocalDate, endDate: LocalDate): HolidaysResponse =
        HolidaysResponse.from(userHolidaysBetweenDatesUseCase.getHolidays(startDate, endDate))

}
