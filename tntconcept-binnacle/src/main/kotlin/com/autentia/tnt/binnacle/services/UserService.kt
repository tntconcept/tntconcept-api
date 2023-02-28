package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.services.PrincipalProviderService
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.repositories.UserRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
internal class UserService(
    private val userRepository: UserRepository,
    private val principalProviderService: PrincipalProviderService
) {

    @Transactional
    @ReadOnly
    fun findByUsername(username: String): User =
        userRepository.findByUsername(username)
            ?: error("User with username $username not found")

    @Transactional
    @ReadOnly
    fun getAuthenticatedUser(): User =
        principalProviderService.getAuthenticatedPrincipal()
            .map { findByUsername(it.name) }
            .orElseThrow { error("There is no authenticated user") }

    @Transactional
    @ReadOnly
    fun findActive(): List<User> =
        userRepository.findByActiveTrue()

}
