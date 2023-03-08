package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import java.time.LocalDateTime

data class ActivityResponse(
    val id: Long,
    val startDate: LocalDateTime,
    val duration: Int,
    val description: String,
    val projectRole: ProjectRole,
    val userId: Long,
    val billable: Boolean,
    val organization: Organization,
    val project: Project,
    val hasEvidences: Boolean?,
    val approvalState: ApprovalState
) {}
