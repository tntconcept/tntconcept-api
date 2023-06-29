package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.HolidayResponseConverter
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.HolidayResponse
import com.autentia.tnt.binnacle.core.domain.Vacation
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.VacationService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import javax.transaction.Transactional

@Singleton
class PrivateHolidaysByChargeYearUseCase internal constructor(
    private val holidayRepository: HolidayRepository,
    private val vacationService: VacationService,
    private val userRepository: UserRepository,
    private val holidayResponseConverter: HolidayResponseConverter,
) {

    @Transactional
    @ReadOnly
    fun get(chargeYear: Int): HolidayResponseDTO {
        val user = userRepository.findByAuthenticatedUser()
            .orElseThrow { IllegalStateException("There isn't authenticated user") }

        val userTimeSinceHiringYear = getTimeSinceHiringYear(user.hiringDate.year)
        val startDateMinHour = userTimeSinceHiringYear.start.atTime(LocalTime.MIN)
        val endDateMaxHour = userTimeSinceHiringYear.end.atTime(23, 59, 59)
        val holidays: List<Holiday> =
            holidayRepository.findAllByDateBetween(startDateMinHour, endDateMaxHour)
        val vacations: List<Vacation> = vacationService.getVacationsByChargeYear(chargeYear)

        val holidayResponse = HolidayResponse(holidays, vacations)

        return holidayResponseConverter.toHolidayResponseDTO(holidayResponse)
    }

    private fun getTimeSinceHiringYear(year: Int): DateInterval {
        val startDate = LocalDate.of(year, Month.JANUARY, 1)
        val endDate = LocalDate.of(LocalDate.now().year + 1, Month.DECEMBER, 31)

        return DateInterval.of(startDate, endDate)
    }

}

