package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
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

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        whenever(userDao.findById(userId)).thenReturn(Optional.of(user))

        val users = userRepositorySecured.find()

        verify(userDao, never()).findByActiveTrue()
        assertEquals(1, users.size)
        assertTrue(users.contains(user))
    }

    @Test
    fun `find all users when the rol is not admin and authenticated user is not found shouls return IllegalStateException`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        whenever(userDao.findById(userId)).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userRepositorySecured.find() }
    }

    @Test
    fun `find all users when no user logged`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userRepositorySecured.find() }
    }

    @Test
    fun `find all users when the rol is admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationAdmin))

        userRepositorySecured.find()

        verify(userDao).findByActiveTrue()
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

        userRepositorySecured.findByActiveTrue()

        verify(userDao).findByActiveTrue()
    }


    @Test
    fun `find by authenticated user`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))

        userRepositorySecured.findByAuthenticatedUser()

        verify(userDao).findById(authenticationUser.id())
    }

    @Test
    fun `find by authenticated without a logged user`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userRepositorySecured.findByAuthenticatedUser() }
    }


    private companion object {
        private const val userId = 1L
        private const val adminUserId = 3L
        private val authenticationAdmin =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("admin")))
        private val authenticationUser = ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))
    }
}