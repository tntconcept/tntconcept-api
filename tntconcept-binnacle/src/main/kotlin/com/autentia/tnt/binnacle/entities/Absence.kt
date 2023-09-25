package com.autentia.tnt.binnacle.entities

import java.time.LocalDate
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Entity
data class Absence (
    @EmbeddedId
    val absenceId: AbsenceId,
    val userId: Long,
    val userName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)