package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.HolidayConverter
import com.autentia.tnt.binnacle.converters.HolidayResponseConverter
import com.autentia.tnt.binnacle.converters.VacationConverter
import com.autentia.tnt.binnacle.core.domain.Vacation
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.dto.HolidayDTO
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.services.VacationService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.LocalDate
import java.time.Month
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

internal class PrivateHolidaysByChargeYearUseCaseTest {

    private val user = createUser()
    private val holidayService = mock<HolidayService>()
    private val vacationService = mock<VacationService>()
    private val userService = mock<UserService>()

    private val holidayConverter = HolidayResponseConverter(VacationConverter(), HolidayConverter())

   private val privateHolidaysByChargeYearUseCase = PrivateHolidaysByChargeYearUseCase(holidayService, vacationService, userService, holidayConverter)

    @Test
    fun `get the vacations by charge year`() {
        val startDate = user.hiringDate
        val endDate = LocalDate.of(LocalDate.now().year + 1, Month.DECEMBER, 31)

        doReturn(user).whenever(userService).getAuthenticatedUser()
        doReturn(HOLIDAYS).whenever(holidayService).findAllBetweenDate(startDate, endDate)
        doReturn(VACATIONS).whenever(vacationService).getVacationsByChargeYear(CHARGE_YEAR, user)

        assertEquals(HOLIDAY_RESPONSE_DTO, privateHolidaysByChargeYearUseCase.get(CHARGE_YEAR))
    }

    private companion object{
       private const val CHARGE_YEAR = 2022
       private val HOLIDAYS = listOf<Holiday>()
       private val HOLIDAYS_DTO = listOf<HolidayDTO>()
       private val VACATIONS = listOf<Vacation>()
       private val VACATIONS_DTO = listOf<VacationDTO>()
       private val HOLIDAY_RESPONSE_DTO = HolidayResponseDTO(HOLIDAYS_DTO, VACATIONS_DTO)
    }

}
