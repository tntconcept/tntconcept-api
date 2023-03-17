package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

internal class PrivateHolidaysPeriodDaysUseCaseTest {

    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)

    private val privateHolidaysPeriodDaysUseCase = PrivateHolidaysPeriodDaysUseCase(calendarFactory)

    @Test
    fun `get workable days between dates`() {
        val startDate = LocalDate.of(2022, 7, 1)
        val endDate = LocalDate.of(2022, 8, 30)

        val holidays = listOf(SANTIAGO_APOSTOL, ASUNCION_VIRGEN)

        doReturn(holidays).whenever(holidayService).findAllBetweenDate(startDate, endDate)

        assertEquals(41, privateHolidaysPeriodDaysUseCase.get(startDate, endDate))
    }

    private companion object {

        private val SANTIAGO_APOSTOL = Holiday(6, "Santiago Apóstol", LocalDateTime.of(2022, 7, 25, 0, 0))
        private val ASUNCION_VIRGEN =  Holiday(7, "Asunción de la Virgen", LocalDateTime.of(2022, 8, 15, 0, 0))

    }

}
