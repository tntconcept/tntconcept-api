package com.autentia.tnt.binnacle.core.domain.alertvalidators

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummaryAlert

enum class AnnualWorkSummaryAlertValidators(
    val alert: AnnualWorkSummaryAlert,
    val validator: AnnualWorkSummaryAlertValidator
) {

    NOT_REACHED_TARGET_WORK(
        AnnualWorkSummaryAlert("Not reached target work"),
        validator = NotReachedTargetWorkAlertValidator()
    ),

    PENDING_VACATIONS(
        AnnualWorkSummaryAlert("Pending vacations"),
        validator = PendingVacationsAlertValidator(listOf(NOT_REACHED_TARGET_WORK.validator))
    )

}
