package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.DateInterval
import java.time.Duration
import java.time.LocalDateTime

data class TimeInterval private constructor(val start: LocalDateTime, val end: LocalDateTime) {
    
    companion object {
        fun of(start: LocalDateTime, end: LocalDateTime) = TimeInterval(start, end)
    }

    fun getDateInterval() = DateInterval.of(start.toLocalDate(), end.toLocalDate())

    fun getDuration(): Duration = Duration.between(start, end)
}