package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.dto.HolidayDTO
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month.DECEMBER
import java.time.Month.JANUARY


internal class UserHolidayBetweenDatesUseCaseTest {
    private var holidayRepository = mock<HolidayRepository>()
    private var userHolidayUseCase = mock<UserHolidayBetweenDatesUseCase>()

    @Test
    fun `return holidays given a year`() {

        doReturn(listOf(THREE_KINGS_DAY)).whenever(holidayRepository)
            .findAllByDateBetween(FIRST_DAY_OF_YEAR.atTime(LocalTime.MIN), LAST_DAY_OF_YEAR.atTime(23, 59, 59))

        doReturn(listOf((THREE_KINGS_DAY_DTO))).whenever(userHolidayUseCase).getHolidays(CURRENT_YEAR)

        assertEquals(listOf(THREE_KINGS_DAY_DTO), userHolidayUseCase.getHolidays(CURRENT_YEAR))
    }

    private companion object {
        private val TODAY = LocalDate.now()
        private val CURRENT_YEAR = TODAY.year

        private val FIRST_DAY_OF_YEAR = LocalDate.of(CURRENT_YEAR, JANUARY, 1)
        private val LAST_DAY_OF_YEAR = LocalDate.of(CURRENT_YEAR, DECEMBER, 31)

        private val THREE_KINGS_DAY = listOf(Holiday(1, "REYES", LocalDateTime.of(CURRENT_YEAR, JANUARY, 6, 0, 0)))

        private val THREE_KINGS_DAY_DTO = listOf(HolidayDTO(1, "REYES", LocalDate.of(CURRENT_YEAR, JANUARY, 6)))

    }
}
