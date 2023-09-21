package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class UsersVacationsFromPeriodUseCase {

    fun getVacationsByPeriod(startDate: LocalDate, endDate: LocalDate): List<VacationDTO>{
        return listOf()
    }
}
