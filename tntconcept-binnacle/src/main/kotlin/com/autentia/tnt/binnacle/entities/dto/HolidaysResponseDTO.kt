package com.autentia.tnt.binnacle.entities.dto

@Deprecated("Use HolidayResponseDTO instead")
data class HolidaysResponseDTO(
        val holidays: List<HolidayDTO>,
        val vacations: List<VacationDTO>
)
