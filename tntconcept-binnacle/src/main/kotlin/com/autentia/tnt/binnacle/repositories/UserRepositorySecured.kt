package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.UserPredicates
import com.autentia.tnt.security.application.canAccessAllUsers
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.data.model.Page
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import io.micronaut.data.model.Pageable
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

    override fun find(userId: Long): User? {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessAllUsers()) {
            userDao.findById(userId).orElse(null)
        } else {
            userDao.findById(authentication.id()).orElse(null)
        }
    }

    override fun findWithoutSecurity(): List<User> {
        return userDao.findByActiveTrue()
    }

    override fun findAll(userPredicate: Specification<User>, pageable: Pageable?): List<User> {
        val authentication = securityService.checkAuthentication()

        var predicate = userPredicate
        if (!authentication.canAccessAllUsers()) {
            predicate = PredicateBuilder.and(userPredicate, UserPredicates.userId((authentication.id())))
        }

        return if (pageable !== null) {
            userDao.findAll(predicate, pageable).content
        } else {
            userDao.findAll(predicate)
        }
    }

}
