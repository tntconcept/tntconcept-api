package com.autentia.tnt.api.binnacle.activity

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Time interval")
data class TimeInterval(
    @get:Schema(description = "Start time of the interval")
    val start: LocalDateTime,
    @get:Schema(description = "End time of the interval")
    val end: LocalDateTime
)