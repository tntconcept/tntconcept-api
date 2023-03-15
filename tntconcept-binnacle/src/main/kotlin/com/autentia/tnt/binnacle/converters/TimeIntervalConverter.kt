package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.dto.TimeIntervalRequestDTO
import jakarta.inject.Singleton

@Singleton
class TimeIntervalConverter {

    fun toTimeInterval(interval: TimeIntervalRequestDTO) = TimeInterval.of(interval.start, interval.end)
}