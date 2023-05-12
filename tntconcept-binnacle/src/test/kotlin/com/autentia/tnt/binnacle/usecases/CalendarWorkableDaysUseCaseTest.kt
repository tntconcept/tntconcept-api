package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class CalendarWorkableDaysUseCaseTest {

    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)

    private val useCase = CalendarWorkableDaysUseCase(calendarFactory)

    @Test
    fun `get workable days should return expected days if there are not holidays`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 13)

        whenever(holidayService.findAllBetweenDate(startDate, endDate)).thenReturn(emptyList())

        val result = useCase.get(startDate, endDate)

        assertEquals(4, result)
    }

    @Test
    fun `get workable days should return expected days if there are holidays`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 13)

        whenever(holidayService.findAllBetweenDate(startDate, endDate)).thenReturn(
            listOf(Holiday(1L, "holiday", startDate.atStartOfDay()))
        )

        val result = useCase.get(startDate, endDate)

        assertEquals(3, result)
    }

    @Test
    fun `get workable days should return expected days if there are holidays and weekend`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 16)

        whenever(holidayService.findAllBetweenDate(startDate, endDate)).thenReturn(
            listOf(Holiday(1L, "holiday", startDate.atStartOfDay()))
        )

        val result = useCase.get(startDate, endDate)

        assertEquals(4, result)
    }
}