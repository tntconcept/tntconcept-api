package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.VacationConverter
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.CreateVacationResponse
import com.autentia.tnt.binnacle.core.domain.RequestVacation
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.VacationState.ACCEPT
import com.autentia.tnt.binnacle.entities.VacationState.PENDING
import com.autentia.tnt.binnacle.exception.MaxNextYearRequestVacationException
import com.autentia.tnt.binnacle.repositories.VacationRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month.DECEMBER
import java.time.Month.JANUARY
import java.time.Month.SEPTEMBER
import java.time.temporal.TemporalAdjusters
import java.util.Date

@TestInstance(PER_CLASS)
internal class VacationServiceTest {

    private val holidayService = mock<HolidayService>()
    private val myVacationsDetailService = mock<MyVacationsDetailService>()
    private val vacationRepository = mock<VacationRepository>()
    private val vacationConverter = VacationConverter()
    private val calendarFactory = CalendarFactory(holidayService)

    private val vacationService = VacationService(
        vacationRepository, holidayService, myVacationsDetailService,
        vacationConverter, calendarFactory
    )

    @Test
    fun `return all vacations between date`() {
        val vacation =
            createVacation(startDate = FIRST_DAY_2020, endDate = JAN_TENTH_2020, state = PENDING, userId = USER.id)

        doReturn(listOf(vacation)).whenever(vacationRepository)
            .getVacationsBetweenDate(FIRST_DAY_2020, END_DATE, USER.id)

        doReturn(holidays2020).whenever(holidayService).findAllBetweenDate(FIRST_DAY_2020, JAN_TENTH_2020)

        val vacationsBetweenDates =
            vacationService.getVacationsBetweenDates(beginDate = FIRST_DAY_2020, finalDate = END_DATE, user = USER)

        assertThat(vacationsBetweenDates[0].days).hasSize(6)
    }

    @Test
    fun `return vacations between date when no exist vacations `() {
        doReturn(EMPTY_VACATIONS).whenever(vacationRepository)
            .getVacationsBetweenDate(FIRST_DAY_2020, END_DATE, USER.id)

        val vacations =
            vacationService.getVacationsBetweenDates(beginDate = FIRST_DAY_2020, finalDate = END_DATE, user = USER)

        assertThat(vacations.sumOf { it.days.size }).isEqualTo(0)
    }

    @Test
    fun `return all vacations by charge year`() {
        doReturn(vacations2020).whenever(vacationRepository)
            .filterBetweenChargeYears(FIRST_DAY_2020, FIRST_DAY_2020, USER.id)

        doReturn(holidays2020).whenever(holidayService).findAllBetweenDate(FIRST_DAY_2020, FOURTH_FEB_2020)

        val vacations = vacationService.getVacationsByChargeYear(YEAR_2020, USER)

        assertThat(vacations.sumOf { it.days.size }).isEqualTo(8)
    }

    @Test
    fun `return all vacations by charge year when vacations are empty`() {
        doReturn(EMPTY_VACATIONS).whenever(vacationRepository)
            .filterBetweenChargeYears(FIRST_DAY_2020, FIRST_DAY_2020, USER.id)

        val actual = vacationService.getVacationsByChargeYear(YEAR_2020, USER)

        assertThat(actual.sumOf { it.days.size }).isEqualTo(0)
    }

    @Test
    fun `return vacation even if the begin and end date are equal`() {
        val vacation = createVacation(startDate = APR_NINTH_2020, endDate = APR_NINTH_2020, state = ACCEPT)

        doReturn(listOf(vacation)).whenever(vacationRepository)
            .getVacationsBetweenDate(APR_FIRST_2020, APR_THIRTEENTH_2020, USER.id)

        val vacationsBetweenDates = vacationService.getVacationsBetweenDates(APR_FIRST_2020, APR_THIRTEENTH_2020, USER)

        assertThat(vacationsBetweenDates[0].days).hasSize(1)
        assertEquals(vacationsBetweenDates[0].days[0], APR_NINTH_2020)
    }

    private fun vacationPeriodProvider() = arrayOf(
        arrayOf(
            // Using remaining days from LAST year
            START_DATE,
            START_DATE.plusDays(2),
            2,
            22,
            22,
            listOf(
                CreateVacationResponse(START_DATE, START_DATE.plusDays(2), 2, CURRENT_YEAR - 1)
            )
        ),
        arrayOf(
            // Using the remaining days from LAST year and CURRENT year
            START_DATE,
            START_DATE.plusDays(10),
            2,
            22,
            22,
            listOf(
                CreateVacationResponse(START_DATE, START_DATE.plusDays(2), 2, CURRENT_YEAR - 1),
                CreateVacationResponse(START_DATE.plusDays(3), START_DATE.plusDays(10), 6, CURRENT_YEAR)
            )
        ),
        arrayOf(
            // Using the remaining days from CURRENT year
            START_DATE,
            START_DATE.plusDays(10),
            0,
            18,
            22,
            listOf(
                CreateVacationResponse(START_DATE, START_DATE.plusDays(10), 8, CURRENT_YEAR)
            )
        ),
        arrayOf(
            // Using the remaining days from CURRENT year and NEXT YEAR
            START_DATE,
            START_DATE.plusDays(5),
            0,
            3,
            22,
            listOf(
                CreateVacationResponse(START_DATE, START_DATE.plusDays(3), 3, CURRENT_YEAR),
                CreateVacationResponse(START_DATE.plusDays(4), START_DATE.plusDays(4), 1, CURRENT_YEAR + 1)
            )
        ),
        arrayOf(
            // Using the NEXT year vacation days if the days quantity is max 5
            START_DATE,
            START_DATE.plusDays(7),
            0,
            0,
            23,

            listOf(
                CreateVacationResponse(START_DATE, START_DATE.plusDays(7), 5, CURRENT_YEAR + 1)
            )
        ),

        arrayOf(
            // TNT BUGFIX description:
            // Sometimes the user, by mistake, using old TNT request 2 vacation days of 2018 CHARGED in 2019,
            // instead of 2018, that leads to negative remaining vacations days in 2019
            // after all vacations days of 2019 are requested.
            // In this example, 2018 will still have 2 remaining days and 2019 will have -2 days.
            // These tests check that if that case happens in BINNACLE, the result is correct.
            START_DATE,
            START_DATE.plusDays(2),
            // Using the OLD TNT the user by mistake:
            // - Charged in the last year the remaining days of the previous year
            -5,
            2,
            22,

            // Using BINNACLE the user request 2 days and will be charged in this year
            listOf(
                CreateVacationResponse(START_DATE, START_DATE.plusDays(2), 2, CURRENT_YEAR)
            )
        ),
        arrayOf(
            // TNT BUGFIX: using negative remaining days from LAST YEAR
            START_DATE,
            START_DATE.plusDays(2),
            // Using the OLD TNT the user by mistake:
            // - Charged in the last year the remaining days of the previous year
            // - And has only two days in this year
            -2,
            2,
            22,
            // Using BINNACLE the user request 2 days and will be charged in this year
            listOf(
                CreateVacationResponse(START_DATE, START_DATE.plusDays(2), 2, CURRENT_YEAR)
            )
        ),
        arrayOf(
            // TNT BUGFIX: using negative remaining days from CURRENT YEAR
            START_DATE,
            START_DATE.plusDays(2),
            // Using the OLD TNT the user by mistake:
            // - Charged in this year the remaining vacation days of last year
            // - And requested ALL vacation days of this year too
            2,
            -2,
            22,
            // Using BINNACLE the user request 2 days and will be charged in the NEXT year, because
            // the user already charged all vacation days of the last and current year.
            listOf(
                CreateVacationResponse(START_DATE, START_DATE.plusDays(2), 2, CURRENT_YEAR + 1)
            )
        ),
        arrayOf(
            // TNT BUGFIX description:
            // When requesting vacation days in the current year charged to the past year and a holiday is present,
            // the holiday shouldn't be counted towards the number of total days.
            START_DATE,
            START_DATE.plusDays(4),
            1,
            22,
            22,
            listOf(
                CreateVacationResponse(START_DATE, START_DATE, 1, CURRENT_YEAR - 1),
                CreateVacationResponse(START_DATE.plusDays(2), START_DATE.plusDays(4), 3, CURRENT_YEAR),
            )
        ),
    )

    @ParameterizedTest
    @MethodSource("vacationPeriodProvider")
    fun createVacationPeriod(
        startDate: LocalDate,
        endDate: LocalDate,
        remainingVacationsLastYear: Int,
        remainingVacationsThisYear: Int,
        remainingVacationsNextYear: Int,
        expectedResult: List<CreateVacationResponse>
    ) {
        doReturn(NEW_YEAR_CURRENT_HOLIDAYS).whenever(holidayService)
            .findAllBetweenDate(LAST_YEAR_FIRST_DAY, NEXT_YEAR_LAST_DAY)

        doReturn(remainingVacationsLastYear)
            .whenever(myVacationsDetailService).getRemainingVacations(eq(LAST_YEAR.year), any(), eq(USER))

        doReturn(remainingVacationsThisYear)
            .whenever(myVacationsDetailService).getRemainingVacations(eq(CURRENT_YEAR), any(), eq(USER))

        doReturn(remainingVacationsNextYear)
            .whenever(myVacationsDetailService).getRemainingVacations(eq(NEXT_YEAR.year), any(), eq(USER))

        doReturn(listOf<Vacation>()).whenever(vacationRepository).filterBetweenChargeYears(any(), any(), any())

        val requestVacation = RequestVacation(null, startDate, endDate, "Lorem ipsum...")

        val actual = vacationService.createVacationPeriod(requestVacation, USER)

        actual.forEachIndexed { index, result ->
            assertEquals(result.startDate, expectedResult[index].startDate)
            assertEquals(result.endDate, expectedResult[index].endDate)
            assertEquals(result.days, expectedResult[index].days)
            assertEquals(result.chargeYear, expectedResult[index].chargeYear)
        }
    }

    @Test
    fun `throw max days of next year request vacation`() {
        doReturn(holidaysBetweenLastYearAndCurrent).whenever(holidayService)
            .findAllBetweenDate(LAST_YEAR_FIRST_DAY, NEXT_YEAR_LAST_DAY)

        doReturn(0).whenever(myVacationsDetailService).getRemainingVacations(eq(LAST_YEAR.year), any(), eq(USER))

        doReturn(0).whenever(myVacationsDetailService).getRemainingVacations(eq(CURRENT_YEAR), any(), eq(USER))

        // First time the user request a vacation period using the vacation days of the next year
        doReturn(22).whenever(myVacationsDetailService).getRemainingVacations(eq(NEXT_YEAR.year), any(), eq(USER))

        doReturn(listOf<Vacation>()).whenever(vacationRepository).filterBetweenChargeYears(any(), any(), any())

        assertThrows<MaxNextYearRequestVacationException> {
            vacationService.createVacationPeriod(JANUARY_CURRENT_YEAR_VACATIONS, USER)
        }
    }

    @Test
    fun `throw max days of next year request vacation 2`() {
        doReturn(0).whenever(myVacationsDetailService).getRemainingVacations(eq(LAST_YEAR.year), any(), eq(USER))

        doReturn(0).whenever(myVacationsDetailService).getRemainingVacations(eq(CURRENT_YEAR), any(), eq(USER))

        // The user already requested a period before and when he tries again, he can't request a new period.
        doReturn(17).whenever(myVacationsDetailService).getRemainingVacations(eq(NEXT_YEAR.year), any(), eq(USER))

        doReturn(listOf<Vacation>()).whenever(vacationRepository).filterBetweenChargeYears(any(), any(), any())

        val requestVacation = RequestVacation(
            id = null,
            startDate = LocalDate.of(CURRENT_YEAR, JANUARY, 1),
            endDate = LocalDate.of(CURRENT_YEAR, JANUARY, 4),
            description = "Lorem ipsum..."
        )

        assertThrows<MaxNextYearRequestVacationException> {
            vacationService.createVacationPeriod(requestVacation, USER)
        }
    }

    @Test
    fun `update vacation period when the new corresponding days quantity IS EQUAL to old corresponding days quantity`() {
        val vacation = createVacation(
            id = VACATION_ID,
            startDate = SEPT_FOURTEENTH_CURRENT.plusDays(7),
            endDate = SEPT_FOURTEENTH_CURRENT.plusDays(8),
            userId = USER.id,
            chargeYear = SEPT_FOURTEENTH_CURRENT.withDayOfYear(1)
        )

        val requestVacation = RequestVacation(
            id = VACATION_ID,
            startDate = SEPT_FOURTEENTH_CURRENT,
            endDate = SEPT_FOURTEENTH_CURRENT.plusDays(1),
            description = "asdasd"
        )

        doReturn(listOf<Holiday>()).whenever(holidayService).findAllBetweenDate(
            LocalDate.of(SEPT_FOURTEENTH_LAST, JANUARY, 1), LocalDate.of(
                SEPT_FOURTEENTH_NEXT, DECEMBER, 31
            )
        )

        val newPrivateHoliday = vacation.copy(
            startDate = requestVacation.startDate,
            endDate = requestVacation.endDate,
            description = requestVacation.description ?: ""
        )

        doReturn(newPrivateHoliday).whenever(vacationRepository).update(newPrivateHoliday)

        val holidays = vacationService.updateVacationPeriod(requestVacation, USER, vacation)

        verify(vacationRepository).update(newPrivateHoliday)

        BDDAssertions.then(holidays).hasSize(1)
    }

    @Test
    fun `request new vacation period when the new corresponding days quantity IS NOT EQUAL to old corresponding days quantity`() {
        val domain = RequestVacation(
            id = VACATION_ID,
            startDate = JAN_SECOND_CURRENT,
            endDate = JAN_SECOND_CURRENT.plusDays(1),
            description = null
        )
        val vacation = Vacation(
            id = VACATION_ID,
            startDate = JAN_SECOND_CURRENT,
            endDate = JAN_SECOND_CURRENT.plusDays(1),
            state = PENDING,
            userId = USER.id,
            observations = "",
            departmentId = null,
            description = "",
            chargeYear = LocalDate.now()
        )

        doReturn(listOf<Holiday>()).whenever(holidayService).findAllBetweenDate(
            LocalDate.of(LAST_YEAR.year, JANUARY, 1),
            LocalDate.of(NEXT_YEAR.year, DECEMBER, 31)
        )

        doReturn(vacation).whenever(vacationRepository).update(eq(vacation))

        val holidays = vacationService.updateVacationPeriod(domain, USER, vacation)

        assertThat(holidays).hasSize(1)
    }

    @Test
    fun `delete vacation by id`() {
        doNothing().whenever(vacationRepository).deleteById(ID_DELETE)

        vacationService.deleteVacationPeriod(ID_DELETE, USER.id)

        verify(vacationRepository).deleteById(ID_DELETE)
    }

    // MUST BE a companion object with @JvmStatic, DO NOT REFACTOR
    // https://blog.oio.de/2018/11/13/how-to-use-junit-5-methodsource-parameterized-tests-with-kotlin/
    private companion object {
        private val USER = createUser()
        private val NOW = LocalDate.now()
        private val CURRENT_YEAR = LocalDate.now().year
        private val LAST_YEAR = LocalDate.now().minusYears(1)
        private val NEXT_YEAR = LocalDate.now().plusYears(1)

        private val LAST_YEAR_FIRST_DAY = LocalDate.of(LAST_YEAR.year, JANUARY, 1)
        private val NEXT_YEAR_LAST_DAY = LocalDate.of(NEXT_YEAR.year, DECEMBER, 31)

        private const val YEAR_2020 = 2020
        private val FIRST_DAY_2020 = LocalDate.of(YEAR_2020, 1, 1)
        private val JAN_TENTH_2020 = LocalDate.of(YEAR_2020, 1, 10)
        private val APR_NINTH_2020 = LocalDate.of(YEAR_2020, 4, 9)
        private val APR_FIRST_2020 = LocalDate.of(YEAR_2020, 4, 1)
        private val APR_THIRTEENTH_2020 = LocalDate.of(YEAR_2020, 4, 30)
        private val DEC_TWEENTYEIGHT_2020 = LocalDate.of(2020, DECEMBER, 28)
        private val JAN_FOURTH_2021 = LocalDate.of(2021, JANUARY, 4)


        private val START_DATE = LocalDate.of(CURRENT_YEAR, JANUARY, 1).with(TemporalAdjusters.firstInMonth(MONDAY))
        private val END_DATE = LocalDate.of(YEAR_2020, 1, 31)

        private val FOURTH_FEB_2020 = LocalDate.of(YEAR_2020, 2, 4)
        private val NEW_YEAR_2020 = Holiday(1, "Año nuevo 2020", FIRST_DAY_2020.atStartOfDay())
        private val NEW_YEAR_2021 = Holiday(1, "Año nuevo 2021", LocalDateTime.of(2021, JANUARY, 1, 0, 0))
        private val REYES_2020 = Holiday(2, "Reyes", LocalDate.of(YEAR_2020, 1, 7).atStartOfDay())

        private val holidays2020 = listOf(NEW_YEAR_2020, REYES_2020)
        private val holidays2021 = listOf(NEW_YEAR_2021)

        private val EMPTY_VACATIONS = emptyList<Vacation>()
        private const val VACATION_ID = 10L
        private const val ID_DELETE = 1L
        private val vacations2020 = listOf(
            createVacation(
                startDate = FIRST_DAY_2020,
                endDate = LocalDate.of(YEAR_2020, 1, 10),
                userId = USER.id,
                chargeYear = FIRST_DAY_2020
            ),
            createVacation(
                id = 2,
                startDate = LocalDate.of(YEAR_2020, 2, 3),
                endDate = FOURTH_FEB_2020,
                userId = USER.id,
                chargeYear = FIRST_DAY_2020
            )
        )


        private val NEW_YEAR_CURRENT_DATE =
            LocalDate.of(CURRENT_YEAR, JANUARY, 1).with(TemporalAdjusters.firstInMonth(MONDAY))
                .plusDays(1)

        private val NEW_YEAR_CURRENT_HOLIDAYS = listOf(
            createHoliday(1, "New Year holiday", NEW_YEAR_CURRENT_DATE),
        )

        private val holidaysBetweenLastYearAndCurrent = listOf(
            createHoliday(1, "Holiday 2019", LocalDate.of(LAST_YEAR.year, DECEMBER, 31)),
            createHoliday(2, "Holiday 2020", LocalDate.of(CURRENT_YEAR, JANUARY, 1))
        )
        private val JANUARY_CURRENT_YEAR_VACATIONS = RequestVacation(
            id = null,
            startDate = LocalDate.of(CURRENT_YEAR, JANUARY, 1),
            endDate = LocalDate.of(CURRENT_YEAR, JANUARY, 10),
            description = "Lorem ipsum..."
        )
        private val JAN_SECOND_CURRENT =
            LocalDate.of(LocalDate.now().year, JANUARY, 2).with(TemporalAdjusters.firstInMonth(MONDAY))
        private val SEPT_FOURTEENTH_CURRENT =
            LocalDate.of(LocalDate.now().year, SEPTEMBER, 14).with(TemporalAdjusters.firstInMonth(MONDAY))
        private val SEPT_FOURTEENTH_LAST = SEPT_FOURTEENTH_CURRENT.year - 1
        private val SEPT_FOURTEENTH_NEXT = SEPT_FOURTEENTH_CURRENT.year + 1

        private fun createVacation(
            id: Long = 1L,
            startDate: LocalDate,
            endDate: LocalDate,
            state: VacationState = PENDING,
            userId: Long = 1L,
            observations: String = "",
            departmentId: Long? = null,
            description: String = "Dummy description",
            chargeYear: LocalDate = NOW,
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

        private fun createHoliday(
            id: Int = 1,
            description: String = "Fake description",
            day: LocalDate = LocalDate.now(),
        ) = Holiday(id.toLong(), description, LocalDateTime.of(day, LocalTime.MIN))

    }

}
