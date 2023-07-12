package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime

internal class CalendarWorkableDaysUseCaseTest {

    private val holidayRepository = mock<HolidayRepository>()

    private val calendarFactory = CalendarFactory(holidayRepository)

    private val useCase = CalendarWorkableDaysUseCase(calendarFactory)

    @Test
    fun `get workable days should return expected days if there are not holidays`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 13)

        whenever(holidayRepository.findAllByDateBetween(startDate.atTime(LocalTime.MIN), endDate.atTime(23, 59, 59))).thenReturn(emptyList())

        val result = useCase.get(startDate, endDate)

        assertEquals(4, result)
    }

    @Test
    fun `get workable days should return expected days if there are holidays`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 13)

        whenever(holidayRepository.findAllByDateBetween(startDate.atTime(LocalTime.MIN), endDate.atTime(23, 59, 59))).thenReturn(
            listOf(Holiday(1L, "holiday", startDate.atStartOfDay()))
        )

        val result = useCase.get(startDate, endDate)

        assertEquals(3, result)
    }

    @Test
    fun `get workable days should return expected days if there are holidays and weekend`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 16)

        whenever(holidayRepository.findAllByDateBetween(startDate.atTime(LocalTime.MIN), endDate.atTime(23, 59, 59))).thenReturn(
            listOf(Holiday(1L, "holiday", startDate.atStartOfDay()))
        )

        val result = useCase.get(startDate, endDate)

        assertEquals(4, result)
    }
}