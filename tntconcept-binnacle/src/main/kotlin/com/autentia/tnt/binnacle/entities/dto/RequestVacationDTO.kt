package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class RequestVacationDTO(
    val id: Long? = null,

    @field:NotNull
    val startDate: LocalDate,

    @field:NotNull
    val endDate: LocalDate,

    @field:Size(max = 1024, message = "Description must not exceed 1024 characters")
    var description: String? = null
)
