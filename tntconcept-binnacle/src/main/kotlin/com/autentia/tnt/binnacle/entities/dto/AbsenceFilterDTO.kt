package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class AbsenceFilterDTO (
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userIds: List<Long>? = null,
    val organizationIds: List<Long>? = null,
    val projectIds: List<Long>? = null,
)