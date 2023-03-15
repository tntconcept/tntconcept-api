package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime

@Introspected
data class TimeIntervalRequestDTO(val start: LocalDateTime, val end: LocalDateTime)