package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.RequestVacation
import com.autentia.tnt.binnacle.entities.dto.RequestVacationDTO
import jakarta.inject.Singleton

@Singleton
class RequestVacationConverter {

    fun toRequestVacation(requestVacationDTO: RequestVacationDTO): RequestVacation =
        RequestVacation(
            id = requestVacationDTO.id,
            startDate = requestVacationDTO.startDate,
            endDate = requestVacationDTO.endDate,
            description = requestVacationDTO.description
        )

}
