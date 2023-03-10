package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime

@Introspected
data class ActivityTimeOnly(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val projectRoleId: Long,
    val timeUnit: TimeUnit
) {
    fun duration() = DurationCalculator.getDuration(start, end, timeUnit)
}