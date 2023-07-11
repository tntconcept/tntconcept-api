package com.autentia.tnt.api.binnacle.user

import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO

data class UserResponse(
    val id: Long,
    val username: String,
    val name: String,
) {
    companion object{
        fun from(userResponseDTO: UserResponseDTO) =
            UserResponse(
                userResponseDTO.id,
                userResponseDTO.username,
                userResponseDTO.name,
            )
    }
}