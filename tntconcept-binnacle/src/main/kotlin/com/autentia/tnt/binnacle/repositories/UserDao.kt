package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.User
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaSpecificationExecutor
import io.micronaut.data.repository.CrudRepository

@Repository
internal interface UserDao : CrudRepository<User, Long>, JpaSpecificationExecutor<User> {

    fun findByUsername(username: String): User?

    fun findByActiveTrue(): List<User>

}
