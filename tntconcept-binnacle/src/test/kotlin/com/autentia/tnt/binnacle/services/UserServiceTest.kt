package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.repositories.UserRepository
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.*

internal class UserServiceTest {

    private val userRepository = mock<UserRepository>()
    private val securityService = mock<SecurityService>()

    private var userService = UserService(userRepository, securityService)

    @Test
    fun `get authenticated user`() {
        val authentication = ClientAuthentication("1", emptyMap())
        val user = createUser()
        doReturn(Optional.of(authentication)).whenever(securityService).authentication
        doReturn(Optional.of(user)).whenever(userRepository).findById(1)

        val authenticatedUser = userService.getAuthenticatedUser()

        assertEquals(user, authenticatedUser)
    }

    @Test
    fun `fail if there isn't authenticated user`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userService.getAuthenticatedUser() }
    }

    @Test
    fun `fail if cannot find t authenticated user`() {
        val authentication = ClientAuthentication("1", emptyMap())
        doReturn(Optional.of(authentication)).whenever(securityService).authentication
        doReturn(Optional.empty<User>()).whenever(userRepository).findById(any())

        assertThrows<IllegalStateException> { userService.getAuthenticatedUser() }
    }

    @Test
    fun `find active users`() {
        val users = listOf(mock(User::class.java))

        doReturn(users).whenever(userRepository).findByActiveTrue()

        val result = userService.findActive()

        assertEquals(users, result)
    }
}
