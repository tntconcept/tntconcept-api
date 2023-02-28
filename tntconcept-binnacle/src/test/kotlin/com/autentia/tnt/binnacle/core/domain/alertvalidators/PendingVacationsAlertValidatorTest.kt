package com.autentia.tnt.binnacle.core.domain.alertvalidators

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class PendingVacationsAlertValidatorTest {

    private val dependingAlertValidators = mock<AnnualWorkSummaryAlertValidator>()
    private val sut = PendingVacationsAlertValidator(
        listOf(
            dependingAlertValidators,
            FakeAlwaysAlertedAlertValidator()
        )
    )

    @ParameterizedTest
    @MethodSource("alertedParametersProvider")
    fun `given annual summary with earned and consumed vacations should return alerted`(
        testDescription: String,
        earnedVacations: Int,
        consumedVacations: Int,
        dependAlertExpected: Boolean,
        expected: Boolean
    ) {
        val annualWorkSummary = AnnualWorkSummary(
            2022,
            earnedVacations = earnedVacations,
            consumedVacations = consumedVacations
        )
        doReturn(dependAlertExpected).whenever(dependingAlertValidators).isAlerted(annualWorkSummary)

        val alerted = sut.isAlerted(annualWorkSummary)

        assertEquals(expected, alerted)
    }

    private companion object {
        @JvmStatic
        private fun alertedParametersProvider() = listOf(
            Arguments.of(
                "given earned vacations greater than consumed vacations and depend alerted should alerted",
                10,
                9,
                true,
                true
            ),
            Arguments.of(
                "given earned vacations greater than consumed vacations and depend not alerted should not alerted",
                10,
                9,
                false,
                false
            ),
            Arguments.of(
                "given consumed vacations greater than earned vacations and depend alerted should not alerted",
                9,
                10,
                true,
                false
            ),
            Arguments.of(
                "given equals earned vacations and consumed vacations and depend alerted should not alerted",
                10,
                10,
                true,
                false
            )
        )
    }
}
