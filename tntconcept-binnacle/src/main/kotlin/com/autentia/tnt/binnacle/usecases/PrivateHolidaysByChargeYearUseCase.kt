package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.HolidayResponseConverter
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.HolidayResponse
import com.autentia.tnt.binnacle.core.domain.Vacation
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.services.UserService
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
    private val userService: UserService,
    private val holidayResponseConverter: HolidayResponseConverter,
) {

    @Transactional
    @ReadOnly
    fun get(chargeYear: Int): HolidayResponseDTO {
        val user = userService.getAuthenticatedUser()

        val userTimeSinceHiringYear = getTimeSinceHiringYear(user.hiringDate.year)
        val holidays: List<Holiday> = getAllByDateBetween(userTimeSinceHiringYear.start, userTimeSinceHiringYear.end)
        val vacations: List<Vacation> = vacationService.getVacationsByChargeYear(chargeYear)

        val holidayResponse = HolidayResponse(holidays, vacations)

        return holidayResponseConverter.toHolidayResponseDTO(holidayResponse)
    }

    private fun getTimeSinceHiringYear(year: Int): DateInterval {
        val startDate = LocalDate.of(year, Month.JANUARY, 1)
        val endDate = LocalDate.of(LocalDate.now().year + 1, Month.DECEMBER, 31)

        return DateInterval.of(startDate, endDate)
    }

    fun getAllByDateBetween(startDate: LocalDate, endDate: LocalDate): List<Holiday> {
        val startDateMinHour = startDate.atTime(LocalTime.MIN)
        val endDateMaxHour = endDate.atTime(23, 59, 59)
        return holidayRepository.findAllByDateBetween(startDateMinHour, endDateMaxHour)
    }

}

