package com.autentia.tnt.binnacle.entities

import java.math.BigDecimal
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.validation.constraints.Digits

@Entity
data class AnnualWorkSummary(
    @EmbeddedId
    val annualWorkSummaryId: AnnualWorkSummaryId,

    @Digits(integer = 5, fraction = 2)
    val workedHours: BigDecimal,

    @Digits(integer = 5, fraction = 2)
    val targetHours: BigDecimal,
)
