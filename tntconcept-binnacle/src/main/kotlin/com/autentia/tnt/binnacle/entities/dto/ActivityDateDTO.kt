package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDate


data class ActivityDateDTO(
    val date: LocalDate,
    val workedMinutes: Int,
    val activities: List<ActivityResponseDTO>
)
