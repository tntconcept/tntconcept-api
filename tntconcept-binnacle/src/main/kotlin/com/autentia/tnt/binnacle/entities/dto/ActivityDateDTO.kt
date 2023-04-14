package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDate

@Deprecated("DTO used in deprecated activities endpoint")
data class ActivityDateDTO(
    val date: LocalDate,
    val workedMinutes: Int,
    val activities: List<ActivitiesResponseDTO>
)