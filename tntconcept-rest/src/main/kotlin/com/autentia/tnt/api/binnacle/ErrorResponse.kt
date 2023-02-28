package com.autentia.tnt.api.binnacle

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for an Error response exception")
internal class ErrorResponse(
    val code: String,
    val message: String? = "Invalid"
)
