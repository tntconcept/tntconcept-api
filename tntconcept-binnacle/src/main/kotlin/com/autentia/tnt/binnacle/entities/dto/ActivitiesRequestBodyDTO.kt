package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import jakarta.validation.constraints.Size
@Deprecated("Use ActivityRequestBodyDTO instead")
@Introspected
data class ActivitiesRequestBodyDTO(
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