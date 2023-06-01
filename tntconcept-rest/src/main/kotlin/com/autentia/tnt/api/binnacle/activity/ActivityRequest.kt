package com.autentia.tnt.api.binnacle.activity

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Introspected
@Schema(description = "A request data for creating or updating an activity")
data class ActivityRequest(
    @get:Schema(
        description = "The activity identifier. It is required for updating and not for creating",
        example = "1"
    ) val id: Long? = null,

    @get:Schema(description = "The interval for the activity", required = true) val interval: TimeInterval,

    @field:Size(max = 2048, message = "Description must not exceed 2048 characters") @get:Schema(
        description = "The description of the activity",
        maxLength = 2048,
        example = "Feature: Store user in database",
        required = true
    ) val description: String,

    @get:Schema(
        description = "Specifies whether the activity is billable or not", example = "true", required = true
    ) val billable: Boolean,

    @get:Schema(
        description = "The ID of the project role associated with the activity", example = "3", required = true
    ) val projectRoleId: Long,

    @get:Schema(
        description = "Specifies whether the activity has evidences or not", example = "true", required = true
    ) val hasEvidences: Boolean,

    @get:Schema(
        description = "The evidence file associated with the activity",
        example = "data:image/jpg;base64,SGVsbG8gV29ybGQh"
    ) @field:Pattern(
        regexp = "^data:[^,]+;base64,.+$", message = "String format should be data:<mediatype>;base64,<data>"
    ) @field:Nullable val evidence: String? = null
)