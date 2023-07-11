package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.repositories.UserRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
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

    fun getAuthenticatedDomainUser(): com.autentia.tnt.binnacle.core.domain.User =
        userRepository.findByAuthenticatedUser()
            .orElseThrow { IllegalStateException("There isn't authenticated user") }.toDomain()

    fun getActiveUsersWithoutSecurity(): List<User> =
        userRepository.findWithoutSecurity()

    fun getByUserName(userName: String): User =
        userRepository.findByUsername(userName) ?: error("User is not found")

}
