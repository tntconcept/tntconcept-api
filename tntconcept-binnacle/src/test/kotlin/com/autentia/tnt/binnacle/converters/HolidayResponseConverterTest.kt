package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.HolidayResponse
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.HolidayDTO
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain


internal class HolidayResponseConverterTest {

    private var sut = HolidayResponseConverter(VacationConverter(), HolidayConverter())

    @Test
    fun `given HolidayResponse should return HolidayResponseDTO`() {

        val holidayResponse = HOLIDAY_RESPONSE

        val result = sut.toHolidayResponseDTO(holidayResponse)

        assertTrue(result.holidays.isNotEmpty())
        assertTrue(result.vacations.isNotEmpty())

        assertEquals(HOLIDAY_RESPONSE_DTO, result)

    }

    private companion object{
        private val HOLIDAY_ID = 10L
        private val HOLIDAY_DESCRIPTION = "Dummy Holiday"
        private val HOLIDAY_DATE = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0)

        private val VACATION_ID = 20L
        private val VACATION_OBSERVATIONS = ""
        private val VACATION_DESCRIPTION = "Dummy Description"
        private val VACATION_STATE = VacationState.ACCEPT
        private val VACATION_START_DATE = LocalDate.of(2020, Month.JANUARY, 2)
        private val VACATION_END_DATE = LocalDate.of(2020, Month.JANUARY, 3)
        private val VACATION_DAYS = listOf(LocalDate.of(2020, Month.JANUARY, 2), LocalDate.of(2020, Month.JANUARY, 3))
        private val VACATION_CHARGE_YEAR = LocalDate.of(2020, Month.JANUARY, 1)

        val holiday = Holiday(
            HOLIDAY_ID,
            HOLIDAY_DESCRIPTION,
            HOLIDAY_DATE
        )


        val holidayDTO = HolidayDTO(
            holiday.id,
            holiday.description,
            holiday.date.toLocalDate()
        )


        val vacation = VacationDomain(
            VACATION_ID,
            VACATION_OBSERVATIONS,
            VACATION_DESCRIPTION,
            VACATION_STATE,
            VACATION_START_DATE,
            VACATION_END_DATE,
            VACATION_DAYS,
            VACATION_CHARGE_YEAR
            )


        val vacationDTO = VacationDTO(
            vacation.id,
            vacation.observations,
            vacation.description,
            vacation.state,
            vacation.startDate,
            vacation.endDate,
            vacation.days,
            vacation.chargeYear
        )

        val HOLIDAY_RESPONSE = HolidayResponse(
            listOf(holiday),
            listOf(vacation)
        )

        val HOLIDAY_RESPONSE_DTO = HolidayResponseDTO(
            listOf(holidayDTO),
            listOf(vacationDTO)
        )

    }

}
