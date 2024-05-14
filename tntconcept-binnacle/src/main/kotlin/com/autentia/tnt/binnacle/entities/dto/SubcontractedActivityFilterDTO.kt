package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class SubcontractedActivityFilterDTO(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val organizationId: Long? = null,
    val projectId: Long? = null,
    val roleId: Long? = null
)

