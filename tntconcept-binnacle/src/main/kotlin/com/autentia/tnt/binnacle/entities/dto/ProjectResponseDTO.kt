package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.ProjectBillingType
import java.time.LocalDate

data class ProjectResponseDTO(
    val id: Long,
    val name: String,
    val open: Boolean,
    val projectBillingType: ProjectBillingType,
    val organizationId: Long,
    val startDate: LocalDate,
    val blockDate: LocalDate? = null,
    val blockedByUser: Long? = null,
)
