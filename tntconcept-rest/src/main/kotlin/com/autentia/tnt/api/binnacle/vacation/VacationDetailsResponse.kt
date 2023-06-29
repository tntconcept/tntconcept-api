package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.binnacle.entities.dto.VacationDetailsDTO

data class VacationDetailsResponse(
    val holidaysAgreement: Int,
    val correspondingVacations: Int,
    val acceptedVacations: Int,
    val remainingVacations: Int
) {
    companion object {
        fun from(vacationDetailsDTO: VacationDetailsDTO): VacationDetailsResponse =
            VacationDetailsResponse(
                vacationDetailsDTO.holidaysAgreement,
                vacationDetailsDTO.correspondingVacations,
                vacationDetailsDTO.acceptedVacations,
                vacationDetailsDTO.remainingVacations,
            )
    }
}