package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.VacationState
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate


data class VacationDTO(
    var id: Long? = null,
    var observations: String? = null,
    var description: String? = null,
    val state: VacationState,

    @JsonFormat(pattern = "yyyy-MM-dd")
    val startDate: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val endDate: LocalDate,

    @JsonFormat(pattern = "yyyy-MM-dd")
    val days: List<LocalDate>,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val chargeYear: LocalDate
)
