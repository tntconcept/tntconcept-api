package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.ApprovalState
import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class ActivityFilterDTO(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val approvalState: ApprovalState? = null,
    val organizationId: Long? = null,
    val projectId: Long? = null,
    val roleId: Long? = null
)