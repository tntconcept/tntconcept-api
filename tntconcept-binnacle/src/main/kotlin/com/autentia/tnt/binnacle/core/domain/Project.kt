package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.ProjectBillingType
import java.time.LocalDate

data class Project(
    val id: Long,
    val name: String,
    val open: Boolean,
    val billable: Boolean,
    val projectBillingType: ProjectBillingType,
    val startDate: LocalDate,
    val blockDate: LocalDate? = null,
    val blockedByUser: Long? = null,
    val organization: Organization
)