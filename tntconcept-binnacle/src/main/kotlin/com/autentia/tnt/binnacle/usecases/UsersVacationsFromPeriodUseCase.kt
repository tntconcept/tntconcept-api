package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.VacationConverter
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import com.autentia.tnt.binnacle.exception.DateRangeException
import com.autentia.tnt.binnacle.repositories.VacationRepository
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class UsersVacationsFromPeriodUseCase internal constructor(
    private val vacationRepository: VacationRepository,
    private val calendarFactory: CalendarFactory,
    private val vacationConverter: VacationConverter
) {

    fun getVacationsByPeriod(startDate: LocalDate, endDate: LocalDate): List<VacationDTO> {
        checkDates(endDate, startDate)
        return vacationRepository.find(startDate, endDate)
            .map {
                vacationConverter.toVacationDomain(
                    it,
                    getWorkableDays(it.startDate, it.endDate)
                )
            }
            .map { vacationConverter.toVacationDTO(it) }
    }

    private fun getWorkableDays(startDate: LocalDate, endDate: LocalDate) =
        calendarFactory.create(DateInterval.of(startDate, endDate)).workableDays

    private fun checkDates(endDate: LocalDate, startDate: LocalDate) {
        if (endDate.isBefore(startDate)) {
            throw DateRangeException(startDate, endDate)
        }
    }

}
