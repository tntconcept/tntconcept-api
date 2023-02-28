package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.HolidayResponseConverter
import com.autentia.tnt.binnacle.core.domain.HolidayResponse
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.services.VacationService
import jakarta.inject.Singleton
import java.time.LocalDate
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain

@Singleton
class HolidaysBetweenDateForAuthenticateUserUseCase internal constructor(
    private val holidayService: HolidayService,
    private val vacationService: VacationService,
    private val userService: UserService,
    private val holidayResponseConverter: HolidayResponseConverter
) {

    fun getHolidays(startDate: LocalDate, endDate: LocalDate): HolidayResponseDTO {
        val user: User = userService.getAuthenticatedUser()

        val holidays: List<Holiday> = holidayService.findAllBetweenDate(startDate, endDate)
        val vacations: List<VacationDomain> = vacationService.getVacationsBetweenDates(startDate, endDate, user)

        return holidayResponseConverter.toHolidayResponseDTO(HolidayResponse(holidays, vacations))
    }

}
