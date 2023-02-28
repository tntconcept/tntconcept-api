package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.VacationState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

@TestInstance(PER_CLASS)
internal class VacationTest {

    private fun requestedState() = arrayOf(
        VacationState.PENDING,
        VacationState.ACCEPT
    )

    @ParameterizedTest
    @MethodSource("requestedState")
    fun isRequestedVacation(vacationState: VacationState) {
        assertTrue(
            Vacation(
                state = vacationState,
                chargeYear = LocalDate.now(),
                days = emptyList(),
                startDate = LocalDate.MIN,
                endDate = LocalDate.MAX
            ).isRequestedVacation()
        )
    }

    private fun notRequestedState() = arrayOf(
        VacationState.CANCELLED,
        VacationState.REJECT
    )

    @ParameterizedTest
    @MethodSource("notRequestedState")
    fun isNotRequestedVacation(vacationState: VacationState) {
        assertFalse(
            Vacation(
                state = vacationState,
                chargeYear = LocalDate.now(),
                days = emptyList(),
                startDate = LocalDate.MIN,
                endDate = LocalDate.MAX
            ).isRequestedVacation()
        )
    }

}
