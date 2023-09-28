package com.autentia.tnt.api.binnacle.absence

import com.autentia.tnt.binnacle.usecases.AbsencesByFilterUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/absence")
internal class AbsenceController (
    private val absencesByFilterUseCase: AbsencesByFilterUseCase
){
    @Get("{?absenceFilterRequest*}")
    @Operation(summary = "Gets absence days with specified filters")
    internal fun get(absenceFilterRequest: AbsenceFilterRequest): List<UserAbsenceResponse> =
        absencesByFilterUseCase.getAbsences(absenceFilterRequest.toDto()).map { UserAbsenceResponse.from(it) }

}