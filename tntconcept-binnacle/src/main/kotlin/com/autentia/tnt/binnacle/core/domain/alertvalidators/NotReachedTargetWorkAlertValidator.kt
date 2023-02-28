package com.autentia.tnt.binnacle.core.domain.alertvalidators

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary

class NotReachedTargetWorkAlertValidator: AnnualWorkSummaryAlertValidator {
    override fun isAlerted(annualWorkSummary: AnnualWorkSummary): Boolean =
        annualWorkSummary.targetWorkingTime > annualWorkSummary.workedTime
}
