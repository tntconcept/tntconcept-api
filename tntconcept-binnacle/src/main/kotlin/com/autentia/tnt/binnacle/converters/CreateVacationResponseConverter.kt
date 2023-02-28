package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.CreateVacationResponse
import com.autentia.tnt.binnacle.entities.dto.CreateVacationResponseDTO
import jakarta.inject.Singleton


@Singleton
class CreateVacationResponseConverter {

    fun toCreateVacationResponseDTO(createVacationResponse: CreateVacationResponse) =
        CreateVacationResponseDTO(
            createVacationResponse.startDate,
            createVacationResponse.endDate,
            createVacationResponse.days,
            createVacationResponse.chargeYear
        )

}
