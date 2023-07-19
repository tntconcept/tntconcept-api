package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.TimeUnit
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for an Error response exception when max time limit is exceeded")
internal class ErrorResponseMaxTimeLimit(
    val code: String,
    val message: String?,
    val data: ErrorValues,
)

data class ErrorValues(
    val maxAllowedTime: Double,
    val remainingTime: Double,
    val timeUnit: TimeUnit,
    val year: Int,
)
