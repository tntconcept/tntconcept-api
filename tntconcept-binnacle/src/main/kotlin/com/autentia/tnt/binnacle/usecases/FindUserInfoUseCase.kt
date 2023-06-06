package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.dto.UserInfoResponseDTO
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton

@Singleton
class FindUserInfoUseCase internal constructor(
    private val securityService: SecurityService,
    private val userService: UserService
){
    fun find(): UserInfoResponseDTO {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()
        val roles = authentication.roles.stream().toList()
        val user =  userService.getById(userId)

        return UserInfoResponseDTO(userId, user.username, user.hiringDate, roles)
    }

}
