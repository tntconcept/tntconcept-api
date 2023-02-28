package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.services.PrincipalProviderService
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.sun.security.auth.UserPrincipal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.times
import org.mockito.kotlin.any
import java.util.*

internal class UserServiceTest {

    private val userRepository =  mock<UserRepository>()
    private val principalProviderService =  mock<PrincipalProviderService>()

    private var userService = UserService(userRepository, principalProviderService)

    @Test
    fun `return user by username from repository`() {
        val username = "Test"
        val user = mock(User::class.java)

        doReturn(user).whenever(userRepository).findByUsername(username)

        val result = userService.findByUsername(username)

        assertEquals(user, result)
    }

    @Test
    fun `throw exception when the user was not found`() {
        val username = "Test"

        whenever(userRepository.findByUsername(username)).thenReturn(null)

        assertThrows<IllegalStateException> { userService.findByUsername(username) }
    }

    @Test
    fun `given principal should return authenticated user`() {
        val principal = UserPrincipal("name")
        val user = createUser()

        doReturn(Optional.of(principal)).whenever(principalProviderService).getAuthenticatedPrincipal()
        doReturn(user).whenever(userRepository).findByUsername(principal.name)

        val authenticatedUser = userService.getAuthenticatedUser()

        assertEquals(user, authenticatedUser)
    }

    @Test
    fun `given unknown principal should return error`() {
        val principal = UserPrincipal("name")

        doReturn(Optional.of(principal)).whenever(principalProviderService).getAuthenticatedPrincipal()
        doThrow(IllegalStateException()).whenever(userRepository).findByUsername(principal.name)

        assertThrows<IllegalStateException> { userService.getAuthenticatedUser() }
    }

    @Test
    fun `given empty principal should return error`() {

        whenever(principalProviderService.getAuthenticatedPrincipal()).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userService.getAuthenticatedUser() }

        verify(userRepository, times(0)).findByUsername(any())
    }

    @Test
    fun `should return active users from repository`() {
        val users = listOf(mock(User::class.java))

        doReturn(users).whenever(userRepository).findByActiveTrue()

        val result = userService.findActive()

        assertEquals(users, result)
    }
}
