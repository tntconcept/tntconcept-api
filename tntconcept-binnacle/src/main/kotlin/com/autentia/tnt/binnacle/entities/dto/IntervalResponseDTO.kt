package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.TimeUnit
import java.time.LocalDateTime

data class IntervalResponseDTO(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val duration: Int,
    val timeUnit: TimeUnit
)
