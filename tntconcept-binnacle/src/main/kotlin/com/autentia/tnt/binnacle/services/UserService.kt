package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.repositories.UserRepositorySecured
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional

@Singleton
@Transactional
@ReadOnly
class UserService internal constructor(
    private val userRepositorySecured: UserRepositorySecured,
) {

    fun getAuthenticatedUser(): User =
        userRepositorySecured.findByAuthenticatedUser()
            .orElseThrow { IllegalStateException("There isn't authenticated user") }

    fun findActive(): List<User> =
        userRepositorySecured.findByActiveTrue()

    fun findAll(): List<User>  =
        userRepositorySecured.find()


    fun getUserByUserName(userName: String): User =
        Optional.ofNullable(userRepositorySecured.findByUsername(userName))
            .orElseThrow { IllegalStateException("There isn't authenticated user") }
}
