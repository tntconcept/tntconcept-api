package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.WorkingAgreement
import java.time.LocalDate

data class UserDetailsResponseDTO(

    val id: Long,

    val username: String,

    val departmentId: Long,

    val name: String,

    val photoUrl: String,

    val dayDuration: Int,

    val agreement: WorkingAgreement,

    val agreementYearDuration: Int? = null,

    val hiringDate: LocalDate,

    val email: String,

    val role: String,
)