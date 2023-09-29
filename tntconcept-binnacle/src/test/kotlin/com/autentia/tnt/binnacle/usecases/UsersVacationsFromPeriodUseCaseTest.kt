package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createVacation
import com.autentia.tnt.binnacle.config.createVacationDTO
import com.autentia.tnt.binnacle.converters.VacationConverter
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import com.autentia.tnt.binnacle.exception.DateRangeException
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.VacationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.DayOfWeek.THURSDAY
import java.time.LocalDate
import java.time.Month.MARCH
import java.time.temporal.TemporalAdjusters

internal class UsersVacationsFromPeriodUseCaseTest {
    private val vacationRepository = mock<VacationRepository>()
    private val holidayRepository = mock<HolidayRepository>()
    private val calendarFactory = CalendarFactory(holidayRepository)
    private val vacationConverter = VacationConverter()
    private val usersVacationsFromPeriodUseCase =
        UsersVacationsFromPeriodUseCase(vacationRepository, calendarFactory, vacationConverter)


    @Test
    fun `throw a day range exception when endDate is more previous than startDate`() {
        val startDate = LocalDate.of(CURRENT_YEAR, 12, 31)
        val endDate = LocalDate.of(CURRENT_YEAR, 1, 1)

        assertThrows<DateRangeException> {
            usersVacationsFromPeriodUseCase.getVacationsByPeriod(startDate, endDate)
        }

    }

    @Test
    fun `return an empty list vacation`() {
        val startDate = LocalDate.of(CURRENT_YEAR, 1, 1)
        val endDate = LocalDate.of(CURRENT_YEAR, 1, 31)

        val result: List<VacationDTO> = usersVacationsFromPeriodUseCase.getVacationsByPeriod(startDate, endDate)

        assertEquals(emptyList<VacationDTO>(), result)
    }


    @Test
    fun `return a list of vacations`() {
        val startDate = LocalDate.of(CURRENT_YEAR, 1, 1)
        val endDate = LocalDate.of(CURRENT_YEAR, 12, 31)

        doReturn(VACATION_WITH_WEEKEND_BETWEEN).whenever(vacationRepository).find(startDate, endDate)
        doReturn(emptyList<Holiday>()).whenever(holidayRepository)
            .findAllByDateBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))

        val result: List<VacationDTO> = usersVacationsFromPeriodUseCase.getVacationsByPeriod(startDate, endDate)

        assertEquals(VACATION_WITH_WEEKEND_BETWEEN_DTO, result)
    }

    private companion object {
        private val TODAY = LocalDate.now()
        private val CURRENT_YEAR = TODAY.year

        private val FIRST_THURSDAY_OF_YEAR =
            LocalDate.of(CURRENT_YEAR, MARCH, 1).with(TemporalAdjusters.firstInMonth(THURSDAY))

        private val VACATION_WITH_WEEKEND_BETWEEN = listOf(
            createVacation(
                id = 1L,
                startDate = FIRST_THURSDAY_OF_YEAR,
                endDate = FIRST_THURSDAY_OF_YEAR.plusDays(5)
            )
        )

        private val VACATION_WITH_WEEKEND_BETWEEN_DTO = listOf(
            createVacationDTO(
                id = 1L,
                startDate = FIRST_THURSDAY_OF_YEAR,
                endDate = FIRST_THURSDAY_OF_YEAR.plusDays(5),
                days = listOf(
                    FIRST_THURSDAY_OF_YEAR,
                    FIRST_THURSDAY_OF_YEAR.plusDays(1),
                    FIRST_THURSDAY_OF_YEAR.plusDays(4),
                    FIRST_THURSDAY_OF_YEAR.plusDays(5)
                )
            )
        )

    }
}