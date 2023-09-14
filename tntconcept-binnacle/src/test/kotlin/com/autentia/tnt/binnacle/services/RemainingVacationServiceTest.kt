package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.VacationConverter
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.RequestVacation
import com.autentia.tnt.binnacle.core.services.TimeWorkableService
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.VacationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters

class RemainingVacationServiceTest {

    private val holidayRepository = mock<HolidayRepository>()
    private val vacationRepository = mock<VacationRepository>()
    private val myVacationsDetailService = MyVacationsDetailService(TimeWorkableService())
    private val vacationConverter = VacationConverter()

    private val calendarFactory = CalendarFactory(holidayRepository)

    private val remainingVacationService = RemainingVacationService(
        vacationRepository,
        myVacationsDetailService,
        vacationConverter,
        calendarFactory
    )

    @Test
    fun `should rest workable days to remaining days`() {
        doReturn(listOf(FIRST_4_WORKABLE_DAYS)).whenever(vacationRepository)
            .findByChargeYear(LocalDate.of(FIRST_MONDAY.year, 1, 1))

        val remainingDays = remainingVacationService.getRemainingVacations(FIRST_MONDAY.year, USER)

        assertEquals(VACATIONS_23 - REQUEST_4_WORKABLE_DAYS, remainingDays)
    }

    @Test
    fun `should rest workable days to remaining days taking care about holidays`() {
        doReturn(listOf(FIRST_4_WORKABLE_DAYS)).whenever(vacationRepository)
            .findByChargeYear(LocalDate.of(FIRST_MONDAY.year, 1, 1))

        val ONE_HOLIDAY_IN_RANGE = 1
        val holiday = Holiday(1, "National Holiday", FIRST_MONDAY.plusDays(1).atStartOfDay())
        doReturn(listOf(holiday)).whenever(holidayRepository).findAllByDateBetween(
            FIRST_MONDAY.atStartOfDay(), FIRST_MONDAY.plusDays(3).atTime(23, 59, 59))

        val remainingDays = remainingVacationService.getRemainingVacations(FIRST_MONDAY.year, USER)

        assertEquals(VACATIONS_23 - REQUEST_4_WORKABLE_DAYS + ONE_HOLIDAY_IN_RANGE, remainingDays)
    }

    @Test
    fun `a request vacation should count workable days`() {
        val requestVacation = RequestVacation(null, FIRST_MONDAY, FIRST_MONDAY.plusDays(3), CURRENT_YEAR)

        val requestedDays = remainingVacationService.getRequestedVacationsSelectedYear(requestVacation)

        assertEquals(4, requestedDays.size)
    }


    @Test
    fun `a request vacation should skip weekend days`() {
        val requestVacation = RequestVacation(null, FIRST_MONDAY, FIRST_MONDAY.plusDays(9), CURRENT_YEAR)

        val requestedDays = remainingVacationService.getRequestedVacationsSelectedYear(requestVacation)

        assertEquals(8, requestedDays.size)
    }

    @Test
    fun `a request vacation should skip weekend days and holidays`() {
        val requestVacation = RequestVacation(null, FIRST_MONDAY, FIRST_MONDAY.plusDays(9), CURRENT_YEAR)

        val holiday = Holiday(1, "National Holiday", FIRST_MONDAY.plusDays(1).atStartOfDay())
        doReturn(listOf(holiday))
            .whenever(holidayRepository)
            .findAllByDateBetween(
                LocalDate.of(CURRENT_YEAR-1, 1, 1).atStartOfDay(),
                LocalDate.of(CURRENT_YEAR+1, 12, 31).atTime(23, 59, 59)
            )

        val requestedDays = remainingVacationService.getRequestedVacationsSelectedYear(requestVacation)

        assertEquals(7, requestedDays.size)
    }

    companion object {

        private val USER = createUser()

        private val VACATIONS_23 = 23
        private val REQUEST_4_WORKABLE_DAYS = 4


        private val CURRENT_YEAR = LocalDate.now().year
        private val FIRST_MONDAY = LocalDate.of(CURRENT_YEAR, Month.JANUARY, 1).with(
            TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))

        val FIRST_4_WORKABLE_DAYS =
            createVacation(
                startDate = FIRST_MONDAY,
                endDate = FIRST_MONDAY.plusDays(3),
                state = VacationState.PENDING,
                userId = USER.id
            )

        private fun createVacation(
            id: Long = 1L,
            startDate: LocalDate,
            endDate: LocalDate,
            state: VacationState = VacationState.PENDING,
            userId: Long = 1L,
            observations: String = "",
            departmentId: Long? = null,
            description: String = "Dummy description",
            chargeYear: LocalDate = LocalDate.of(CURRENT_YEAR, 1, 1),
        ): Vacation =
            Vacation(
                id,
                startDate,
                endDate,
                state,
                userId,
                observations,
                departmentId,
                description,
                chargeYear,
            )
    }

}