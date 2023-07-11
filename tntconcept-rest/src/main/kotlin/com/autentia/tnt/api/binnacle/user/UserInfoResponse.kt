package com.autentia.tnt.api.binnacle.user

import com.autentia.tnt.binnacle.entities.dto.UserInfoResponseDTO
import java.time.LocalDate

data class UserInfoResponse(
    val id: Long,
    val username: String,
    val hiringDate: LocalDate,
    val roles: List<String>,
) {
    companion object {
        fun from(userInfoResponseDTO: UserInfoResponseDTO) =
            UserInfoResponse(
                userInfoResponseDTO.id,
                userInfoResponseDTO.username,
                userInfoResponseDTO.hiringDate,
                userInfoResponseDTO.roles,
            )
    }
}