package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class HolidayServiceTest {

    private val holidayRepository = mock<HolidayRepository>()

    private var holidayService = HolidayService(holidayRepository)

    @Test
    fun `return all holidays between dates that the user have`() {
        val startDate = LocalDate.of(2019, 1, 1).atStartOfDay()
        val endDate = LocalDate.of(2019, 1, 31).atTime(23, 59, 59)

        val holidaysOfJanuary2019 = listOf(
            Holiday(1, "Año nuevo", LocalDate.of(2019, 1, 1).atStartOfDay()),
            Holiday(2, "Reyes", LocalDate.of(2019, 1, 7).atStartOfDay())
        )

        doReturn(holidaysOfJanuary2019).whenever(holidayRepository).findAllByDateBetween(startDate, endDate)

        val actual = holidayService.findAllBetweenDate(startDate.toLocalDate(), endDate.toLocalDate())

        assertEquals(holidaysOfJanuary2019, actual)
    }

}
