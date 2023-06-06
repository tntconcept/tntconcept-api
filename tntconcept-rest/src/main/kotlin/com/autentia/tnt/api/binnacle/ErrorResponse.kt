package com.autentia.tnt.api.binnacle

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A response data that represents a BadRequest error")
internal data class ErrorResponse(
    @get:Schema(description = "Error identifier", example = "ACTIVITY_TIME_OVERLAPS")
    val code: String,

    @get:Schema(
        description = "Error message", example = "There is already an activity in the indicated period of time"
    )
    val message: String?
)
