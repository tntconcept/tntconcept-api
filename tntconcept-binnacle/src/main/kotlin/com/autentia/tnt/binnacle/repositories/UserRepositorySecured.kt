package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import com.autentia.tnt.security.application.isAdmin
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.Optional

@Singleton
internal class UserRepositorySecured(
    private val userDao: UserDao,
    private val securityService: SecurityService
): UserRepository {

    override fun findByAuthenticatedUser(): Optional<User> {
        val authentication = securityService.checkAuthentication()
        return userDao.findById(authentication.id())
    }
    override fun findByUsername(username: String): User? {
        return userDao.findByUsername(username)
    }

    override fun findByActiveTrue(): List<User> {
        return userDao.findByActiveTrue()
    }
    override fun find(): List<User>{
        val authentication = securityService.checkAuthentication()
        return if(authentication.isAdmin()) {
            userDao.findByActiveTrue()
        }else{
            val user = userDao.findById(authentication.id())
            check(user.isPresent) { "Authenticated user not found" }
            listOf(user.get())
        }
    }

}
