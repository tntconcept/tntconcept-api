package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import java.time.LocalDateTime

data class IntervalResponse(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val duration: Int,
    val timeUnit: TimeUnit
) {
    companion object {
        fun from(intervalResponseDTO: IntervalResponseDTO) =
            IntervalResponse(
                intervalResponseDTO.start,
                intervalResponseDTO.end,
                intervalResponseDTO.duration,
                intervalResponseDTO.timeUnit
            )
    }
}