package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import java.time.LocalDateTime

data class ProjectRoleRecent (
    val id: Long,
    val name: String,
    val projectId: Long,
    val organizationId: Long,
    val projectOpen: Boolean,
    val date: LocalDateTime,
    val maxAllowed: Int,
    val timeUnit: TimeUnit,
    val requireEvidence: RequireEvidence,
    val requireApproval: Boolean,
    val userId: Long
)
