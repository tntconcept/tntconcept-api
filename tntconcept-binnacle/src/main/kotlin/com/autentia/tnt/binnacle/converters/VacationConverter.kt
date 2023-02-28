package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import jakarta.inject.Singleton
import java.time.LocalDate
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain

@Singleton
class VacationConverter {

    fun toVacationDomain(vacationEntity: Vacation, days: List<LocalDate>) = VacationDomain(
        id = vacationEntity.id,
        observations = vacationEntity.observations,
        description = vacationEntity.description,
        state = vacationEntity.state,
        startDate = vacationEntity.startDate,
        endDate = vacationEntity.endDate,
        days = days,
        chargeYear = vacationEntity.chargeYear
    )

    fun toVacationDTO(vacationDomain: VacationDomain) = VacationDTO(
        id = vacationDomain.id,
        observations = vacationDomain.observations,
        description = vacationDomain.description,
        state = vacationDomain.state,
        startDate = vacationDomain.startDate,
        endDate = vacationDomain.endDate,
        days = vacationDomain.days,
        chargeYear = vacationDomain.chargeYear
    )

}
