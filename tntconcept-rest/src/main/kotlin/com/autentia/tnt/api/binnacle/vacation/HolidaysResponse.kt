package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.binnacle.entities.dto.HolidaysResponseDTO

@Deprecated("Use HolidayResponse instead")
data class HolidaysResponse(
    val holidays: List<HolidayDetailsResponse>,
    val vacations: List<VacationResponse>,
) {
    companion object {
        fun from(holidaysResponseDTO: HolidaysResponseDTO): HolidaysResponse =
            HolidaysResponse(
                holidaysResponseDTO.holidays.map { HolidayDetailsResponse.from(it) },
                holidaysResponseDTO.vacations.map { VacationResponse.from(it) }
            )
    }
}
