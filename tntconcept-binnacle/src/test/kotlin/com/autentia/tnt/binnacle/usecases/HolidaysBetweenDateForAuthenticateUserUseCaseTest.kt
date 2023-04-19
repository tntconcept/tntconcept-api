package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.HolidayConverter
import com.autentia.tnt.binnacle.converters.HolidayResponseConverter
import com.autentia.tnt.binnacle.converters.VacationConverter
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.HolidayDTO
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.binnacle.services.VacationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month.JANUARY
import org.mockito.kotlin.doReturn
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain


internal class HolidaysBetweenDateForAuthenticateUserUseCaseTest {
    private var holidayService = mock<HolidayService>()
    private var vacationService = mock<VacationService>()
    private var holidaysBetweenDateForAuthenticateUserUseCase =
        HolidaysBetweenDateForAuthenticateUserUseCase(holidayService, vacationService, HolidayResponseConverter(VacationConverter(), HolidayConverter()))

    @Test
    fun `return holidays given start date, end date and username`() {
        doReturn(THREE_KINGS_DAY).whenever(holidayService).findAllBetweenDate(JANUARY_FIFTH, JANUARY_NINTH)

        doReturn(
            listOf(
            VacationDomain(id = 1L, observations = "", description = "", ACCEPTED_VACATION, DAY_AFTER_THREE_KINGS_DAY, DAY_AFTER_THREE_KINGS_DAY, listOf(
                DAY_AFTER_THREE_KINGS_DAY
            ), TODAY
            ))
        ).whenever(vacationService).getVacationsBetweenDates(JANUARY_FIFTH, JANUARY_NINTH)

        assertEquals(HolidayResponseDTO(THREE_KINGS_DAY_DTO, vacationsDTO), holidaysBetweenDateForAuthenticateUserUseCase.getHolidays(
            JANUARY_FIFTH, JANUARY_NINTH
        ))
    }

    private companion object{
        private val TODAY = LocalDate.now()
        private val CURRENT_YEAR = TODAY.year

        private val JANUARY_FIFTH = LocalDate.of(CURRENT_YEAR, JANUARY, 5)
        private val JANUARY_NINTH = LocalDate.of(CURRENT_YEAR, JANUARY, 9)

        private val THREE_KINGS_DAY = listOf(Holiday(1, "REYES", LocalDateTime.of(CURRENT_YEAR, JANUARY, 6,0,0)))
        private val DAY_AFTER_THREE_KINGS_DAY = LocalDate.of(CURRENT_YEAR,JANUARY, 7)

        private val THREE_KINGS_DAY_DTO = listOf(HolidayDTO(1, "REYES", LocalDate.of(CURRENT_YEAR, JANUARY, 6)))

        private val ACCEPTED_VACATION = VacationState.ACCEPT

        private val vacationsDTO = listOf(VacationDTO(
            id = 1L,
            observations = "",
            description = "",
            state = ACCEPTED_VACATION,
            startDate = DAY_AFTER_THREE_KINGS_DAY,
            endDate = DAY_AFTER_THREE_KINGS_DAY,
            days = listOf(DAY_AFTER_THREE_KINGS_DAY),
            chargeYear = TODAY
        ))

    }
}
