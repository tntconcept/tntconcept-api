package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.TimeSummaryDTO
import com.autentia.tnt.binnacle.usecases.UserTimeSummaryUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate

@Controller("/api/time-summary")
internal class TimeSummaryController(
    private val userWorkTimeUseCase: UserTimeSummaryUseCase,
) {

    @Operation(summary = "Retrieves working time from a given date")
    @Get
    fun getTimeSummary(date: LocalDate): TimeSummaryDTO =
        userWorkTimeUseCase.getTimeSummary(date)

}
