package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import com.autentia.tnt.binnacle.entities.dto.VacationDetailsDTO
import com.autentia.tnt.binnacle.services.MyVacationsDetailService
import com.autentia.tnt.binnacle.services.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.LocalDate
import java.time.Month
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

internal class PrivateHolidayDetailsUseCaseTest {

    private val myHolidayDetailService = mock<MyVacationsDetailService>()
    private val userService = mock<UserService>()

    private val privateHolidayDetailsUseCase = PrivateHolidayDetailsUseCase(userService, myHolidayDetailService)
    @Test
    fun `get user vacation details`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(22).whenever(myHolidayDetailService).getCorrespondingVacationDaysSinceHiringDate(USER, CHARGE_YEAR)
        doReturn(23).whenever(myHolidayDetailService).getCorrespondingVacationDaysSinceHiringDate(USER, YEAR_WHEN_AGREEMENT_CHANGE)

        assertEquals(VacationDetailsDTO(
            holidaysAgreement = 22,
            correspondingVacations = 22,
            acceptedVacations = 2,
            remainingVacations = 19
        ), privateHolidayDetailsUseCase.get(CHARGE_YEAR, VACATIONS_DTO))

        assertEquals(VacationDetailsDTO(
            holidaysAgreement = 23,
            correspondingVacations = 23,
            acceptedVacations = 2,
            remainingVacations = 20
        ), privateHolidayDetailsUseCase.get(YEAR_WHEN_AGREEMENT_CHANGE, VACATIONS_DTO))

    }



    private companion object{
        private val USER = createUser(LocalDate.of(2020, Month.JANUARY, 1))
        private const val YEAR_WHEN_AGREEMENT_CHANGE = 2022
        private const val CHARGE_YEAR = 2020

        val VACATIONS_DTO = listOf(
            VacationDTO(
                id = 1,
                state = VacationState.REJECT,
                startDate = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 1),
                endDate = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 1),
                days = listOf(LocalDate.of(CHARGE_YEAR, Month.JANUARY, 1)),
                chargeYear = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 1)
            ),
            VacationDTO(
                id = 2,
                state = VacationState.CANCELLED,
                startDate = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 2),
                endDate = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 2),
                days = listOf(LocalDate.of(CHARGE_YEAR, Month.JANUARY, 2)),
                chargeYear = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 1)
            ),
            VacationDTO(
                id = 3,
                state = VacationState.ACCEPT,
                startDate = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 3),
                endDate = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 6),
                days = listOf(LocalDate.of(CHARGE_YEAR, Month.JANUARY, 4), LocalDate.of(CHARGE_YEAR, Month.JANUARY, 5)),
                chargeYear = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 1)
            ),
            VacationDTO(
                id = 4,
                state = VacationState.PENDING,
                startDate = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 7),
                endDate = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 7),
                days = listOf(LocalDate.of(CHARGE_YEAR, Month.JANUARY, 7)),
                chargeYear = LocalDate.of(CHARGE_YEAR, Month.JANUARY, 1)
            )
        )


    }
}
