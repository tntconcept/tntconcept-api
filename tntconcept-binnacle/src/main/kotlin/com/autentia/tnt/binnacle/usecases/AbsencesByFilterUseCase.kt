package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.dto.AbsenceDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceFilterDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceType
import com.autentia.tnt.binnacle.repositories.VacationRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AbsencesByFilterUseCase internal constructor(
    private val vacationRepository: VacationRepository
) {
    @Transactional
    @ReadOnly
    fun getAbsences(absenceFilter: AbsenceFilterDTO): List<AbsenceDTO> {
        return vacationRepository.find(absenceFilter.startDate, absenceFilter.endDate).map{
            AbsenceDTO(it.userId, "John Doe", AbsenceType.VACATION, it.startDate, it.endDate)
        }
    }
}