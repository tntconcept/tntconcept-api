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
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.VacationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.util.*

internal class PrivateHolidaysByChargeYearUseCaseTest {

    private val user = createUser()
    private val holidayRepository = mock<HolidayRepository>()
    private val vacationService = mock<VacationService>()
    private val userRepository = mock<UserRepository>()

    private val holidayConverter = HolidayResponseConverter(VacationConverter(), HolidayConverter())

    private val privateHolidaysByChargeYearUseCase = PrivateHolidaysByChargeYearUseCase(holidayRepository, vacationService, userRepository, holidayConverter)

    @Test
    fun `get the vacations by charge year`() {
        val startDate = user.hiringDate.atTime(LocalTime.MIN)
        val endDate = LocalDate.of(LocalDate.now().year + 1, Month.DECEMBER, 31).atTime(23, 59, 59)
        doReturn(Optional.of(user)).whenever(userRepository).findByAuthenticatedUser()
        doReturn(HOLIDAYS).whenever(holidayRepository).findAllByDateBetween(startDate, endDate)
        doReturn(VACATIONS).whenever(vacationService).getVacationsByChargeYear(CHARGE_YEAR)

        assertEquals(HOLIDAY_RESPONSE_DTO, privateHolidaysByChargeYearUseCase.get(CHARGE_YEAR))
    }

    private companion object {
        private const val CHARGE_YEAR = 2022
        private val HOLIDAYS = listOf<Holiday>()
        private val HOLIDAYS_DTO = listOf<HolidayDTO>()
        private val VACATIONS = listOf<Vacation>()
        private val VACATIONS_DTO = listOf<VacationDTO>()
        private val HOLIDAY_RESPONSE_DTO = HolidayResponseDTO(HOLIDAYS_DTO, VACATIONS_DTO)
    }

}
