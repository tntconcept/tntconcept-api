package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.util.Optional
import kotlin.time.Duration
import kotlin.time.DurationUnit.MINUTES
import kotlin.time.toDuration

@Singleton
class AnnualWorkSummaryConverter {

    fun toAnnualWorkSummary(
        year: Int,
        earnedVacations: Int,
        consumedVacations: Int,
        workSummaryEntity: com.autentia.tnt.binnacle.entities.AnnualWorkSummary?,
    ): AnnualWorkSummary {
        val workedTime = this.hoursToDuration(workSummaryEntity?.workedHours)
        val targetWorkingTime = this.hoursToDuration(workSummaryEntity?.targetHours)
        return AnnualWorkSummary(
            year = year,
            workedTime = workedTime,
            targetWorkingTime = targetWorkingTime,
            earnedVacations,
            consumedVacations
        )
    }

    private fun hoursToDuration(hours: BigDecimal?) =
        Optional.ofNullable(hours)
            .map {
                it.multiply(BigDecimal.valueOf(60))
                    .toLong()
                    .toDuration(MINUTES)
            }
            .orElse(Duration.ZERO)

}
