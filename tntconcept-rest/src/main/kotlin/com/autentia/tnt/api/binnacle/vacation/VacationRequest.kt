package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class VacationRequest(
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
    val chargeYear: LocalDate,
) {
    companion object {
        fun from(vacationDTO: VacationDTO) =
            VacationRequest(
                vacationDTO.id,
                vacationDTO.observations,
                vacationDTO.description,
                vacationDTO.state,
                vacationDTO.startDate,
                vacationDTO.endDate,
                vacationDTO.days,
                vacationDTO.chargeYear
            )
    }
}