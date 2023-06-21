package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDate

data class UserInfoResponseDTO(
    val id: Long,
    val username: String,
    val hiringDate: LocalDate,
    val roles: List<String>,
)