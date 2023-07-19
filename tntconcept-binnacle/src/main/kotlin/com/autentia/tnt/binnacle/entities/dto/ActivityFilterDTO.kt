package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

enum class ApprovalStateActivityFilter {
    NA, PENDING, ACCEPTED, ALL
}

@Introspected
data class ActivityFilterDTO(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val approvalState: ApprovalStateActivityFilter? = null,
    val organizationId: Long? = null,
    val projectId: Long? = null,
    val roleId: Long? = null,
    val userId: Long? = null,
)