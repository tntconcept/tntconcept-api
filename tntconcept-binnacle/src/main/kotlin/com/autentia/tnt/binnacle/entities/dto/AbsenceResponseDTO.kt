package com.autentia.tnt.binnacle.entities.dto

data class AbsenceResponseDTO(
    val userId: Long,
    val userName: String,
    val absences: List<AbsenceDTO>,
)