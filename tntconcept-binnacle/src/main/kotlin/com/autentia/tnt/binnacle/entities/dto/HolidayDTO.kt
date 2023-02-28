package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDate

data class HolidayDTO(
    val id: Long,
    val description: String,
    val date: LocalDate
)
