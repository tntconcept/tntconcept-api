package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import jakarta.inject.Singleton

@Singleton
class UserResponseConverter {
    fun mapUserToUserResponseDTO(user: User) = UserResponseDTO(
        id = user.id,
        username = user.username,
        departmentId = user.departmentId,
        name = user.name,
        photoUrl = user.photoUrl,
        dayDuration = user.dayDuration,
        agreement = user.agreement,
        agreementYearDuration = user.agreementYearDuration,
        hiringDate = user.hiringDate,
        email = user.email,
        role = user.role
    )
}