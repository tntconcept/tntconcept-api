package com.autentia.tnt.binnacle.core.domain.alertvalidators

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AnnualWorkSummaryAlertValidatorsTest {

    @Test
    fun `test number of validators`() {
        assertEquals(2, AnnualWorkSummaryAlertValidators.values().size)
    }

    @Test
    fun `test instance of validators`() {
        assertTrue(
            AnnualWorkSummaryAlertValidators.NOT_REACHED_TARGET_WORK.validator is NotReachedTargetWorkAlertValidator
        )
        assertTrue(
            AnnualWorkSummaryAlertValidators.PENDING_VACATIONS.validator is PendingVacationsAlertValidator
        )
    }

}
