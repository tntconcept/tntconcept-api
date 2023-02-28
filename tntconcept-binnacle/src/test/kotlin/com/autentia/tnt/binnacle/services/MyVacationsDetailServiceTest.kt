package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.services.TimeWorkableService
import com.autentia.tnt.binnacle.core.utils.myDatesUntil
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month

internal class MyVacationsDetailServiceTest {

    private val timeWorkableService = mock<TimeWorkableService>()
    private val myHolidaysDetailService = MyVacationsDetailService(timeWorkableService)

    @Test
    fun `get remaining vacations days`() {
        val user = createUser()
        val year = 2020


        doReturn(user.getAgreementTermsByYear(year).vacation).whenever(timeWorkableService).getEarnedVacationsSinceHiringDate(user, year)

        val resultIncludePending = myHolidaysDetailService.getRemainingVacations(2020, PRIVATE_VACATIONS, user)

        assertEquals(14 , resultIncludePending)
    }

    @Test
    fun `get corresponding vacations since hiring date`() {

        val user = createUser(hiringDate)
        val expectedVacations = 22

        doReturn(expectedVacations).whenever(timeWorkableService).getEarnedVacationsSinceHiringDate(user, hiringDate.year)

        val earnedVacations = myHolidaysDetailService.getCorrespondingVacationDaysSinceHiringDate(user, hiringDate.year)

        assertEquals(expectedVacations, earnedVacations)
    }

    private companion object{

        val hiringDate = LocalDate.ofYearDay(2022, 1)

        val vacationsAgreement = 22

        val PRIVATE_VACATIONS = listOf(
            VacationDomain(
                id = 30,
                state = VacationState.ACCEPT,
                startDate = LocalDate.of(2020, Month.JANUARY, 1),
                endDate = LocalDate.of(2020, Month.JANUARY, 3),
                days = LocalDate.of(2020, Month.JANUARY, 1).myDatesUntil(LocalDate.of(2020, Month.JANUARY, 3)).toList(),
                chargeYear = LocalDate.of(2020, Month.JANUARY, 1)
            ),
            VacationDomain(
                id = 31,
                state = VacationState.PENDING,
                startDate = LocalDate.of(2020, Month.JANUARY, 6),
                endDate = LocalDate.of(2020, Month.JANUARY, 10),
                days = LocalDate.of(2020, Month.JANUARY, 6).myDatesUntil(LocalDate.of(2020, Month.JANUARY, 10))
                    .toList(),
                chargeYear = LocalDate.of(2020, Month.JANUARY, 1)
            ),
            VacationDomain(
                id = 32,
                state = VacationState.CANCELLED,
                startDate = LocalDate.of(2020, Month.JANUARY, 20),
                endDate = LocalDate.of(2020, Month.JANUARY, 23),
                days = LocalDate.of(2020, Month.JANUARY, 20).myDatesUntil(LocalDate.of(2020, Month.JANUARY, 23))
                    .toList(),
                chargeYear = LocalDate.of(2020, Month.JANUARY, 1)
            ),
            VacationDomain(
                id = 33,
                state = VacationState.REJECT,
                startDate = LocalDate.of(2020, Month.JANUARY, 28),
                endDate = LocalDate.of(2020, Month.JANUARY, 29),
                days = LocalDate.of(2020, Month.JANUARY, 28).myDatesUntil(LocalDate.of(2020, Month.JANUARY, 29))
                    .toList(),
                chargeYear = LocalDate.of(2020, Month.JANUARY, 1)
            )
        )

    }

}
