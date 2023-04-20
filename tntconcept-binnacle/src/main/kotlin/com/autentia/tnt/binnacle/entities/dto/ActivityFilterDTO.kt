package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.ApprovalState
import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class ActivityFilterDTO(
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val approvalState: ApprovalState?,
    val organizationId: Long?,
    val projectId: Long?,
    val roleId: Long?
)