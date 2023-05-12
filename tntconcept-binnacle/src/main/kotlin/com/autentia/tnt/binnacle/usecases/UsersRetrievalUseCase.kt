package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.UserResponseConverter
import com.autentia.tnt.binnacle.entities.dto.UserDetailsResponseDTO
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import com.autentia.tnt.binnacle.services.UserService
import jakarta.inject.Singleton

@Singleton
class UsersRetrievalUseCase internal constructor(
    private val userService: UserService,
    private val userResponseConverter: UserResponseConverter
) {
    fun getAllActiveUsers(): List<UserResponseDTO> {
        return userService.findAllActive()
            .map { userResponseConverter.mapUserToUserResponseDTO(it) }
    }
}
