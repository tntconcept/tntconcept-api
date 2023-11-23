package com.autentia.tnt.binnacle.entities.dto

data class UserResponseDTO(
    val id: Long,
    val username: String,
    val name: String,
    val email: String
)