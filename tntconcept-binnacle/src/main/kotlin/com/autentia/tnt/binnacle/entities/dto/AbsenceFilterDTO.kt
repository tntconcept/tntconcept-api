package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class AbsenceFilterDTO (
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userId: Long? = null,
    val organizationId: Long? = null,
    val projectId: Long? = null,
)