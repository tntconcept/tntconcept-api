package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.usecases.CalendarWorkableDaysUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate

@Controller("/api/calendar")
@Validated
internal class CalendarController(
    private val calendarWorkableDaysUseCase: CalendarWorkableDaysUseCase,
) {

    @Get("/workable-days")
    @Operation(summary = "Retrieves workable days within a given period.")
    internal fun getWorkableDays(startDate: LocalDate, endDate: LocalDate): Int =
        calendarWorkableDaysUseCase.get(startDate, endDate)


}