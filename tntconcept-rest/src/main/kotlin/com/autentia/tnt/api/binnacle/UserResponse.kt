package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.User
import java.time.LocalDate

internal data class UserResponse(
    val username: String,
    val hiringDate: LocalDate,
) {

    constructor(user: User) : this(
        user.username,
        user.hiringDate,
    )

}
