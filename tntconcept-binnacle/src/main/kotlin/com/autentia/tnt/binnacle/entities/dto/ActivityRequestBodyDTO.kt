package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.core.domain.ProjectRole
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import javax.validation.constraints.Size

@Introspected
data class ActivityRequestBodyDTO(
    val id: Long? = null,
    val interval: TimeIntervalRequestDTO,
    @field:Size(max = 2048, message = "Description must not exceed 2048 characters")
    val description: String,
    val billable: Boolean,
    val projectRoleId: Long,
    val hasEvidences: Boolean,
    val imageFile: String? = null
) {
    constructor(
        id: Long? = null,
        start: LocalDateTime,
        end: LocalDateTime,
        description: String,
        billable: Boolean,
        projectRoleId: Long,
        hasEvidences: Boolean,
        imageFile: String? = null
    ) : this(id, TimeIntervalRequestDTO(start, end), description, billable, projectRoleId, hasEvidences, imageFile)

    fun toDomain(projectRole: ProjectRole, userId: Long) =
        com.autentia.tnt.binnacle.core.domain.Activity(interval.start, interval.end, projectRole, userId)
}