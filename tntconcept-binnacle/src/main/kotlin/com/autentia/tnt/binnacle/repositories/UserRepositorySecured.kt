package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.security.application.canAccessToOthersInfo
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.*

@Singleton
internal class UserRepositorySecured(
    private val userDao: UserDao,
    private val securityService: SecurityService
) : UserRepository {

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

    override fun find(userId: Long): User? {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessToOthersInfo()) {
            userDao.findById(userId).orElse(null)
        } else {
            userDao.findById(authentication.id()).orElse(null)
        }
    }

    override fun find(): List<User> {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessToOthersInfo()) {
            userDao.findByActiveTrue()
        } else {
            val user = userDao.findById(authentication.id())
            check(user.isPresent) { "Authenticated user not found" }
            listOf(user.get())
        }
    }

}
