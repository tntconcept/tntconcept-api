package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.VacationState.ACCEPT
import com.autentia.tnt.binnacle.entities.dto.HolidayDTO
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import com.autentia.tnt.binnacle.usecases.HolidaysBetweenDateForAuthenticateUserUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month.JANUARY

internal class HolidayControllerTest {

    private val holidaysBetweenDateForAuthenticateUserUseCase = mock<HolidaysBetweenDateForAuthenticateUserUseCase>()

    private val holidayController = HolidayController(holidaysBetweenDateForAuthenticateUserUseCase)

    @Test
    fun `return holidays between start date and end date`() {
        val startDate = LocalDate.of(2020, JANUARY, 1)
        val endDate = LocalDate.of(2020, JANUARY, 31)

        val holidayResponseDTO = HolidayResponseDTO(
            listOf(
                HolidayDTO(10, "Dummy Holiday", LocalDate.of(2020, JANUARY, 1))
            ), listOf(
                VacationDTO(
                    20,
                    null,
                    "Dummy Description",
                    ACCEPT,
                    LocalDate.of(2020, JANUARY, 2),
                    LocalDate.of(2020, JANUARY, 3),
                    listOf(LocalDate.of(2020, JANUARY, 2), LocalDate.of(2020, JANUARY, 3)),
                    LocalDate.of(2020, JANUARY, 1)
                )
            )
        )
        doReturn(holidayResponseDTO)
            .whenever(holidaysBetweenDateForAuthenticateUserUseCase).getHolidays(startDate, endDate)

        val result = holidayController.getHolidaysBetweenDate(startDate, endDate)

        assertEquals(holidayResponseDTO, result)
    }

}
