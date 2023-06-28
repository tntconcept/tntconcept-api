package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO

data class HolidayResponse(
    val holidays: List<HolidayRequest>,
    val vacations: List<VacationRequest>,
) {
    companion object {
        fun from(holidayResponseDTO: HolidayResponseDTO): HolidayResponse =
            HolidayResponse(
                holidayResponseDTO.holidays.map { HolidayRequest.from(it) },
                holidayResponseDTO.vacations.map { VacationRequest.from(it) }
            )
    }
}
