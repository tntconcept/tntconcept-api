package com.autentia.tnt.binnacle.entities

import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Absence (
    @Id
    val id: Long,
    val userId: Long,
    val userName: String,
    val type: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)