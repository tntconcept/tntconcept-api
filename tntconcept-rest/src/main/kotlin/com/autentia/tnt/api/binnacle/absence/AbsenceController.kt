package com.autentia.tnt.api.binnacle.absence

import com.autentia.tnt.api.OpenApiTag
import com.autentia.tnt.api.OpenApiTag.Companion.ABSENCE
import com.autentia.tnt.binnacle.usecases.AbsencesByFilterUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/api/absence")
@Tag(name = ABSENCE)
internal class AbsenceController (
    private val absencesByFilterUseCase: AbsencesByFilterUseCase
){
    @Get("{?absenceFilterRequest*}")
    @Operation(summary = "Gets absence days with specified filters")
    internal fun get(absenceFilterRequest: AbsenceFilterRequest): List<UserAbsenceResponse> =
        absencesByFilterUseCase.getAbsences(absenceFilterRequest.toDto()).map { UserAbsenceResponse.from(it) }

}