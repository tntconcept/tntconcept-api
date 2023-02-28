package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import com.autentia.tnt.binnacle.entities.AnnualWorkSummaryId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import kotlin.time.Duration

internal class AnnualWorkSummaryConverterTest {

    private val sut = AnnualWorkSummaryConverter()

    @ParameterizedTest
    @MethodSource("annualWorkSummaryParametersProvider")
    fun `given working time should return DTO with converted values`(
        testDescription: String,
        year: Int,
        earnedVacations: Int,
        consumedVacations: Int,
        entitySummary: com.autentia.tnt.binnacle.entities.AnnualWorkSummary?,
        expectedDomainSummary: AnnualWorkSummary
    ) {
        val domainSummary = sut.toAnnualWorkSummary(year, earnedVacations, consumedVacations, entitySummary)

        assertEquals(expectedDomainSummary, domainSummary)

    }

    private companion object {
        @JvmStatic
        private fun annualWorkSummaryParametersProvider() = listOf(
            Arguments.of(
                "given null work summary entity should return domain with converted values",
                2021,
                22,
                20,
                null,
                AnnualWorkSummary(
                    year = 2021,
                    workedTime = Duration.ZERO,
                    targetWorkingTime = Duration.ZERO,
                    earnedVacations = 22,
                    consumedVacations = 20
                )
            ),
            Arguments.of(
                "given work summary entity should return domain with converted values",
                2021,
                22,
                20,
                com.autentia.tnt.binnacle.entities.AnnualWorkSummary(
                    annualWorkSummaryId = AnnualWorkSummaryId(userId = 1L, year = 2021),
                    workedHours = BigDecimal.valueOf(160.50),
                    targetHours = BigDecimal.valueOf(140.50)
                ),
                AnnualWorkSummary(
                    2021,
                    workedTime = Duration.parse("160h 30m"),
                    targetWorkingTime = Duration.parse("140h 30m"),
                    earnedVacations = 22,
                    consumedVacations = 20
                )
            )
        )
    }
}
