package com.autentia.tnt.binnacle.entities.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class CreateVacationResponseDTO(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startDate: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val endDate: LocalDate,
    val days: Int,
    val chargeYear: Int
)
