package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.binnacle.entities.dto.CreateVacationResponseDTO
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class CreateVacationResponse(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startDate: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val endDate: LocalDate,
    val days: Int,
    val chargeYear: Int,
) {
    companion object {
        fun from(createVacationResponseDTO: CreateVacationResponseDTO): CreateVacationResponse =
            CreateVacationResponse(
                createVacationResponseDTO.startDate,
                createVacationResponseDTO.endDate,
                createVacationResponseDTO.days,
                createVacationResponseDTO.chargeYear,
            )
    }
}