package com.autentia.tnt.api.binnacle.calendar

import com.autentia.tnt.binnacle.usecases.CalendarDaysForProjectRoleUseCase
import com.autentia.tnt.binnacle.usecases.CalendarWorkableDaysUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate

@Controller("/api/calendar")
internal class CalendarController(
    private val calendarWorkableDaysUseCase: CalendarWorkableDaysUseCase,
    private val calendarDaysForProjectRoleUseCase: CalendarDaysForProjectRoleUseCase,
) {

    @Get("/workable-days/count")
    @Operation(summary = "Retrieves workable days within a given period.")
    internal fun getNumberOfWorkableDays(@QueryValue startDate: LocalDate, @QueryValue endDate: LocalDate): Int =
        calendarWorkableDaysUseCase.get(startDate, endDate)

    @Get("/days/count")
    @Operation(summary = "Retrieves the days within a given period in the time unit of the selected project role")
    internal fun getNumberOfDaysOfPeriodByProjectRole(@QueryValue startDate: LocalDate, @QueryValue endDate: LocalDate, @QueryValue roleId: Long): Int =
        calendarDaysForProjectRoleUseCase.get(startDate, endDate, roleId)

}