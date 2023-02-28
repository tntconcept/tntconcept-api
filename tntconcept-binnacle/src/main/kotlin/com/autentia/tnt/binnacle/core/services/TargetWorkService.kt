package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import java.time.LocalDate
import kotlin.time.Duration

class TargetWorkService {
    fun getAnnualTargetWork(
        year: Int,
        hiringDate: LocalDate,
        annualWorkingTime: Duration,
        agreementYearDuration: Duration,
        annualWorkSummary: AnnualWorkSummary,
    ): Duration {
        return when {
            hiringDate.year == year -> return if (annualWorkingTime < agreementYearDuration) annualWorkingTime else agreementYearDuration
            hiringDate.year < year -> {
                val balanceYearBefore = annualWorkSummary.workedTime - annualWorkSummary.targetWorkingTime
                return agreementYearDuration - balanceYearBefore
            }
            else -> Duration.ZERO
        }
    }
}
