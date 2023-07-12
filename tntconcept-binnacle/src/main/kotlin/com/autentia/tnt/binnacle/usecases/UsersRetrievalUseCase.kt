package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.UserResponseConverter
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import com.autentia.tnt.binnacle.repositories.UserRepository
import jakarta.inject.Singleton

@Singleton
class UsersRetrievalUseCase internal constructor(
    private val userRepository: UserRepository,
    private val userResponseConverter: UserResponseConverter
) {
    fun getAllActiveUsers(): List<UserResponseDTO> {
        return userRepository.find()
            .map { userResponseConverter.mapUserToUserResponseDTO(it) }
    }
}
