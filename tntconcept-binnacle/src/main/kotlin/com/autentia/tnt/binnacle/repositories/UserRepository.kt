package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.User
import io.micronaut.data.jpa.repository.criteria.Specification
import java.util.Optional


internal interface UserRepository {

    fun find(userId: Long): User?

    fun findByAuthenticatedUser(): Optional<User>

    fun findByUsername(username: String): User?

    fun findWithoutSecurity(): List<User>

    fun findAll(userPredicate: Specification<User>): List<User>

}
