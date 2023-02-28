package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.Role
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.WorkingAgreement
import java.time.LocalDate

internal data class UserResponse(
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
    val role: Role
) {

    constructor(user: User) : this(
        user.id,
        user.username,
        user.departmentId,
        user.name,
        user.photoUrl,
        user.dayDuration,
        user.agreement,
        user.agreementYearDuration,
        user.hiringDate,
        user.email,
        user.role
    )

}
