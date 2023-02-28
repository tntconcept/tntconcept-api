package com.autentia.tnt.binnacle.core.domain.alertvalidators

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary

interface AnnualWorkSummaryAlertValidator {
    fun isAlerted(annualWorkSummary: AnnualWorkSummary): Boolean
}
