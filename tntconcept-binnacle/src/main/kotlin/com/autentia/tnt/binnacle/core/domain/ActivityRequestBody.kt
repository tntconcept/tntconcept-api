package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDateTime
import javax.validation.constraints.Size

data class ActivityRequestBody(

    val id: Long? = null,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val duration: Int,
    @field:Size(max = 2048, message = "Description must not exceed 2048 characters")
    val description: String,

    val billable: Boolean,
    val projectRoleId: Long,
    val hasEvidences: Boolean,
    val imageFile: String? = null
) {
    fun getTimeInterval() = TimeInterval.of(start, end)
    fun toDomain(projectRole: ProjectRole, userId: Long) = Activity(start, end, projectRole, userId)
}