package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import java.math.BigDecimal
import java.util.Date
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.validation.constraints.Digits

@Entity
data class AnnualWorkSummary(
    @EmbeddedId
    val annualWorkSummaryId: AnnualWorkSummaryId,

    @Digits(integer = 5, fraction = 2)
    val workedHours: BigDecimal,

    @Digits(integer = 5, fraction = 2)
    val targetHours: BigDecimal,
)
