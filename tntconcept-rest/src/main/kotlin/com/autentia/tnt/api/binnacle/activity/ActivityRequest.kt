package com.autentia.tnt.api.binnacle.activity

import io.micronaut.core.annotation.Introspected
import javax.annotation.Nullable
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Introspected
data class ActivityRequest(
    val id: Long? = null,
    val interval: TimeInterval,
    @field:Size(max = 2048, message = "Description must not exceed 2048 characters")
    val description: String,
    val billable: Boolean,
    val projectRoleId: Long,
    val hasEvidences: Boolean,
    @field:Pattern(
        regexp = "^data:[^,]+;base64,.+$", message = "String format should be data:<mediatype>;base64,<data>"
    )
    @field:Nullable
    val evidence: String? = null,
)