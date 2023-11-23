package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.User
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaSpecificationExecutor
import io.micronaut.data.repository.PageableRepository

@Repository
internal interface UserDao : PageableRepository<User, Long>, JpaSpecificationExecutor<User> {

    fun findByUsername(username: String): User?

    fun findByActiveTrue(): List<User>

    fun findByIdIn(ids: List<Long>): List<User>
}
