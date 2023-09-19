package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.User
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import java.util.Optional


internal interface UserRepository {

    fun find(userId: Long): User?

    fun findByAuthenticatedUser(): Optional<User>

    fun findByUsername(username: String): User?

    fun findWithoutSecurity(): List<User>

    fun findAll(userPredicate: Specification<User>): List<User>

    fun findAll(userPredicate: Specification<User>, pageable: Pageable): Page<User>

}
