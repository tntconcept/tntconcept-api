package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDateTime
import jakarta.validation.constraints.Size
@Deprecated("Use ActivityRequestBody instead")
data class ActivitiesRequestBody(
    val id: Long? = null,

    val startDate: LocalDateTime,
    val duration: Int,

    @field:Size(max = 2048, message = "Description must not exceed 2048 characters")
    val description: String,

    val billable: Boolean,
    val projectRoleId: Long,
    val hasImage: Boolean,
    val imageFile: String? = null
)