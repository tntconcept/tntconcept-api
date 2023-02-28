package com.autentia.tnt.binnacle.core.domain.alertvalidators

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary

internal class FakeAlwaysAlertedAlertValidator : AnnualWorkSummaryAlertValidator {
    override fun isAlerted(annualWorkSummary: AnnualWorkSummary): Boolean = true
}
