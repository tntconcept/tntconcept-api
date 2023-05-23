package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.User
import java.time.LocalDate

data class UserInfoResponseDTO(
    val username: String,
    val hiringDate: LocalDate,
    val roles: List<String>
) {

    constructor(user: User, roles: List<String>) : this(
        user.username,
        user.hiringDate,
        roles
    )

}