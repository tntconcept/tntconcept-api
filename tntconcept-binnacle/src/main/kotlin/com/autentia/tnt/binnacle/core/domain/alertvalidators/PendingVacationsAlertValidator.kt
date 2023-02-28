package com.autentia.tnt.binnacle.core.domain.alertvalidators

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary

class PendingVacationsAlertValidator(
    private val dependingAlertValidators: List<AnnualWorkSummaryAlertValidator>
) : AnnualWorkSummaryAlertValidator {

    override fun isAlerted(annualWorkSummary: AnnualWorkSummary): Boolean {
        return dependingAlertValidators
            .map { it.isAlerted(annualWorkSummary) }
            .all { it }
            .and(annualWorkSummary.earnedVacations > annualWorkSummary.consumedVacations)
    }
}
