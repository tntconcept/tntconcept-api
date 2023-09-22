package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.binnacle.entities.dto.RequestVacationDTO
import io.micronaut.core.annotation.Introspected
import java.time.LocalDate
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Introspected
data class CreateVacationRequest(
    val id: Long? = null,

    @field:NotNull
    val startDate: LocalDate,

    @field:NotNull
    val endDate: LocalDate,

    @field:NotNull
    val chargeYear: Int,

    @field:Size(max = 1024, message = "Description must not exceed 1024 characters")
    var description: String? = null
) {
    fun toDto(): RequestVacationDTO = RequestVacationDTO(
        id,
        startDate,
        endDate,
        chargeYear,
        description,
    )
}