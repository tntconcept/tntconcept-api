package com.autentia.tnt.api.binnacle.activity

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Model for an Error response exception when the activity project is blocked")
internal class ErrorResponseBlockedProject(
    val code: String,
    val message: String?,
    val data: ErrorValues,
) {
    internal data class ErrorValues(
        val blockedDate: LocalDate
    )
}