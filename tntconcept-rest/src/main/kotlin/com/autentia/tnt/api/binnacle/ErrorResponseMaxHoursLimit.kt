package com.autentia.tnt.api.binnacle

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for an Error response exception when max hours limit is exceeded")
internal class ErrorResponseMaxHoursLimit(
    val code: String,
    val message: String?,
    val data: ErrorValues
)

data class ErrorValues(
    val maxAllowedHours: Double,
    val remainingHours: Double
)
