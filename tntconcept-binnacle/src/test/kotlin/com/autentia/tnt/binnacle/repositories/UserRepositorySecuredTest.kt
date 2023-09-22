package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.UserPredicates
import com.autentia.tnt.security.application.id
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.*

class UserRepositorySecuredTest {
    private val userDao = mock<UserDao>()
    private val securityService = mock<SecurityService>()
    private val userRepositorySecured = UserRepositorySecured(userDao, securityService)

    @Test
    fun `find by username with security`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        userRepositorySecured.findByUsername("Doe")

        verify(userDao).findByUsername("Doe")
    }

    @Test
    fun `find by active user`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        userRepositorySecured.findWithoutSecurity()

        verify(userDao).findByActiveTrue()
    }

    @Test
    fun `find by authenticated user`() {
        whenever(securityService.authentication).thenReturn(Optional.of(userAuth))

        userRepositorySecured.findByAuthenticatedUser()

        verify(userDao).findById(userAuth.id())
    }

    @Test
    fun `find by authenticated without a logged user should throw IllegalStateException`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userRepositorySecured.findByAuthenticatedUser() }
    }

    @Test
    fun `find by user id should return info if logged user has admin role`() {
        val user = createUser()

        whenever(securityService.authentication).thenReturn(Optional.of(adminAuth))
        whenever(userDao.findById(userId)).thenReturn(Optional.of(user))

        val result = userRepositorySecured.find(userId)

        assertEquals(user, result)
    }

    @Test
    fun `find by user id should return info if logged user has activity-approval role`() {
        val user = createUser()

        whenever(securityService.authentication).thenReturn(Optional.of(activityApprovalAuth))
        whenever(userDao.findById(userId)).thenReturn(Optional.of(user))

        val result = userRepositorySecured.find(userId)

        assertEquals(user, result)
    }

    @Test
    fun `find by user id should return info for authenticated user`() {
        val user = createUser()

        whenever(securityService.authentication).thenReturn(Optional.of(userAuth))
        whenever(userDao.findById(userAuth.id())).thenReturn(Optional.of(user))

        val result = userRepositorySecured.find(adminUserId)

        assertEquals(user, result)
    }

    @Test
    fun `find by user id should throw IllegalStateException if there is not authenticated user`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userRepositorySecured.find(userId) }
    }

    @Test
    fun `findAll cannot access to users info if there is not an user authenticated`() {
        val pageable = Pageable.from(0, 1)
        val predicate = UserPredicates.ALL
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userRepositorySecured.findAll(predicate, pageable) }
    }

    @Test
    fun `findAll can access to all users limit by 2 info if user has activity-approval role`() {
        val pageable = Pageable.from(0, 2)
        val predicate = UserPredicates.ALL
        val expectedUsers = listOf(createUser())

        whenever(securityService.authentication).thenReturn(Optional.of(activityApprovalAuth))
        whenever(userDao.findAll(predicate, pageable)).thenReturn(Page.of(expectedUsers, pageable, 1))

        val result = userRepositorySecured.findAll(predicate, pageable)

        assertEquals(expectedUsers, result)
    }

    @Test
    fun `findAll can access to all users info without pageable if user has activity-approval role`() {
        val predicate = UserPredicates.ALL
        val expectedUsers = listOf(createUser())

        whenever(securityService.authentication).thenReturn(Optional.of(activityApprovalAuth))
        whenever(userDao.findAll(predicate)).thenReturn(expectedUsers)

        val result = userRepositorySecured.findAll(predicate, null)

        assertEquals(expectedUsers, result)
    }

    private companion object {
        private const val userId = 1L
        private const val adminUserId = 3L

        private val adminAuth =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("admin", "activity-approval")))
        private val activityApprovalAuth =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("activity-approval")))
        private val userAuth = ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))
    }
}