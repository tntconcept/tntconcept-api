package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import java.time.LocalDateTime
@Deprecated("Use ActivityResponse instead")
data class ActivitiesResponse(
    val id: Long,
    val startDate: LocalDateTime,
    val duration: Int,
    val description: String,
    val projectRole: ProjectRole,
    val userId: Long,
    val billable: Boolean,
    val organization: Organization,
    val project: Project,
) {}