package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.services.PrincipalProviderService
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
    private val principalProviderService: PrincipalProviderService
) {

    fun getAuthenticatedUser(): User =
        principalProviderService.getAuthenticatedPrincipal()
            .flatMap { userRepository.findById(it.name.toLong()) }
            .orElseThrow { IllegalStateException("There isn't authenticated user") }

    fun findActive(): List<User> =
        userRepository.findByActiveTrue()

}
