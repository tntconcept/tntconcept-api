package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.repositories.UserRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.util.Optional
import javax.transaction.Transactional

@Singleton
@Transactional
@ReadOnly
class UserService internal constructor(
    private val userRepository: UserRepository,
) {

    fun getAuthenticatedUser(): User =
        userRepository.findByAuthenticatedUser()
            .orElseThrow { IllegalStateException("There isn't authenticated user") }

    fun findActive(): List<User> =
        userRepository.findByActiveTrue()

    fun findAll(): List<User>  =
        userRepository.find()


    fun getUserByUserName(userName: String): User =
        Optional.ofNullable(userRepository.findByUsername(userName))
            .orElseThrow { IllegalStateException("There isn't authenticated user") }
}
