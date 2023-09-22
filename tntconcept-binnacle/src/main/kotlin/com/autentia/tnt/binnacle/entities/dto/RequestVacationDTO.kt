package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Introspected
data class RequestVacationDTO(
    val id: Long? = null,

    @field:NotNull
    val startDate: LocalDate,

    @field:NotNull
    val endDate: LocalDate,

    @field:NotNull
    val chargeYear: Int,

    @field:Size(max = 1024, message = "Description must not exceed 1024 characters")
    var description: String? = null
)
