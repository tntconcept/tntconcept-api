package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import java.time.LocalDateTime

data class ActivityResponse(
    val id: Long,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val duration: Int,
    val description: String,
    val projectRole: ProjectRole,
    val userId: Long,
    val billable: Boolean,
    val organization: Organization,
    val project: Project,
    val hasImage: Boolean?
) {}
