package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDate

enum class AbsenceType {
    VACATION, PAID_LEAVE
}

data class AbsenceDTO(
    val type: AbsenceType,
    val startDate: LocalDate,
    val endDate: LocalDate,
)