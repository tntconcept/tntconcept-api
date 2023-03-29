package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.RequireEvidence
import java.time.LocalDateTime

@Deprecated("Use ProjectRoleRecent instead")
data class ProjectRolesRecent (
    val id: Long,
    val name: String,
    val projectName: String,
    val organizationName: String,
    val projectBillable: Boolean,
    val projectOpen: Boolean,
    val date: LocalDateTime,
    val requireEvidence: RequireEvidence,
)