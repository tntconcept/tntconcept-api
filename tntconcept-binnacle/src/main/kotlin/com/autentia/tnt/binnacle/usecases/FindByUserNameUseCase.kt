package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.services.UserService
import jakarta.inject.Singleton

@Singleton
class FindByUserNameUseCase internal constructor(
    private val userService: UserService
){
    fun find(): User {
        return userService.getAuthenticatedUser()
    }

}
