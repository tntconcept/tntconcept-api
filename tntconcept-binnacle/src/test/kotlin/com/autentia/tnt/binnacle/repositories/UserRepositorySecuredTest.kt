package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.security.application.id
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import junit.framework.TestCase.assertTrue
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import java.util.*

class UserRepositorySecuredTest {
    private val userDao = mock<UserDao>()
    private val securityService = mock<SecurityService>()
    private val userRepositorySecured = UserRepositorySecured(userDao, securityService)

    @Test
    fun `find all users when the rol is not admin should return only authenticated user`() {
        val user = createUser().copy(id = userId)

        whenever(securityService.authentication).thenReturn(Optional.of(userAuth))
        whenever(userDao.findById(userId)).thenReturn(Optional.of(user))

        val users = userRepositorySecured.find()

        verify(userDao, never()).findByActiveTrue()
        assertEquals(1, users.size)
        assertTrue(users.contains(user))
    }

    @Test
    fun `find all users when the rol is not admin and authenticated user is not found should return IllegalStateException`() {
        whenever(securityService.authentication).thenReturn(Optional.of(userAuth))
        whenever(userDao.findById(userId)).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userRepositorySecured.find() }
    }

    @Test
    fun `find all users when no user logged throws exception`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userRepositorySecured.find() }
    }

    @Test
    fun `find all users when the rol is admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(adminAuth))

        userRepositorySecured.find()

        verify(userDao).findByActiveTrue()
    }

    @Test
    fun `find all users when the rol is project-blocker`() {
        whenever(securityService.authentication).thenReturn(Optional.of(projectBlockerAuth))

        userRepositorySecured.find()

        verify(userDao).findByActiveTrue()
        verify(userDao, never()).findById(projectBlockerAuth.id())
    }

    @Test
    fun `find all users when the rol is activity approval`() {
        whenever(securityService.authentication).thenReturn(Optional.of(activityApprovalAuth))

        userRepositorySecured.find()

        verify(userDao).findByActiveTrue()
        verify(userDao, never()).findById(activityApprovalAuth.id())
    }

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

    private companion object {
        private const val userId = 1L
        private const val adminUserId = 3L

        private val adminAuth =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("admin", "activity-approval")))
        private val activityApprovalAuth =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("activity-approval")))
        private val projectBlockerAuth =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("project-blocker")))
        private val userAuth = ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))
    }
}