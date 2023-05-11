package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.core.domain.TimeInterval
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime

@Introspected
data class TimeIntervalRequestDTO(val start: LocalDateTime, val end: LocalDateTime) {
    fun toDomain() = TimeInterval.of(start, end)
}