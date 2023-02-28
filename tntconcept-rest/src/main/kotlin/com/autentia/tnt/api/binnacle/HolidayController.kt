package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.usecases.HolidaysBetweenDateForAuthenticateUserUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate

@Controller("/api/holidays")
internal class HolidayController(
    private val holidaysBetweenDateForAuthenticateUserUseCase: HolidaysBetweenDateForAuthenticateUserUseCase
) {

    @Operation(summary = "Retrieves existing holidays within a period")
    @Get
    fun getHolidaysBetweenDate(startDate: LocalDate, endDate: LocalDate): HolidayResponseDTO =
        holidaysBetweenDateForAuthenticateUserUseCase.getHolidays(startDate, endDate)

}
