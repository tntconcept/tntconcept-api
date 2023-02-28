package com.autentia.tnt.binnacle.core.domain.alertvalidators

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class NotReachedTargetWorkAlertValidatorTest {

    private val sut = NotReachedTargetWorkAlertValidator()

    @ParameterizedTest
    @MethodSource("alertedParametersProvider")
    fun `given annual summary with target work and worked should return alerted`(
        testDescription: String,
        targetWork: Int,
        worked: Int,
        expected: Boolean,
    ) {
        val annualWorkSummary =
            AnnualWorkSummary(
                year = 2022,
                targetWorkingTime = targetWork.toDuration(DurationUnit.MINUTES),
                workedTime = worked.toDuration(DurationUnit.MINUTES)
            )

        val alerted = sut.isAlerted(annualWorkSummary)

        assertEquals(expected, alerted)
    }

    private companion object {
        @JvmStatic
        private fun alertedParametersProvider() = listOf(
            Arguments.of(
                "given target work greater than worked should alerted",
                10,
                9,
                true
            ),
            Arguments.of(
                "given worked greater than target work should not alerted",
                9,
                10,
                false
            ),
            Arguments.of(
                "given equals target work and worked should not alerted",
                10,
                10,
                false
            )
        )
    }
}
