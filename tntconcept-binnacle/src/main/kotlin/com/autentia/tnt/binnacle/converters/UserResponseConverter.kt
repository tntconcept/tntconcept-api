package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import jakarta.inject.Singleton

@Singleton
class UserResponseConverter {

    fun mapUserToUserResponseDTO(user: User) = UserResponseDTO(
        id = user.id,
        username = user.username,
        name = user.name,
    )
}