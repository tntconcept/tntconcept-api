package com.autentia.tnt.api.binnacle.request.activity

import com.autentia.tnt.api.binnacle.request.TimeInterval
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Introspected
@Schema(description = "A request data for creating or updating an activity")
data class ActivityRequest(
    @get:Schema(description = "The activity identifier")
    val id: Long? = null,

    @get:Schema(description = "The interval for the activity")
    val interval: TimeInterval,

    @field:Size(max = 2048, message = "Description must not exceed 2048 characters")
    @get:Schema(description = "The description of the activity", maxLength = 2048)
    val description: String,

    @get:Schema(description = "Specifies whether the activity is billable or not")
    val billable: Boolean,

    @get:Schema(description = "The ID of the project role associated with the activity")
    val projectRoleId: Long,

    @get:Schema(description = "Specifies whether the activity has evidences or not")
    val hasEvidences: Boolean,

    @get:Schema(description = "The evidence file associated with the activity")
    @Pattern(regexp = "^data:[^,]+,.+$", message = "String format should be data:<mediatype>,<data>")
    @Nullable
    val evidence: String? = null
)