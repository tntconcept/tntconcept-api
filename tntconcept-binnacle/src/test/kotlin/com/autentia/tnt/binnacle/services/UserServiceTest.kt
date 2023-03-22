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
import org.mockito.kotlin.verify
import org.mockito.kotlin.times
import org.mockito.kotlin.any
import java.util.*

internal class UserServiceTest {

    private val userRepository =  mock<UserRepository>()
    private val principalProviderService =  mock<PrincipalProviderService>()

    private var userService = UserService(userRepository, principalProviderService)

    @Test
    fun `get authenticated user`() {
        val principal = UserPrincipal("1")
        val user = createUser()
        doReturn(Optional.of(principal)).whenever(principalProviderService).getAuthenticatedPrincipal()
        doReturn(Optional.of(user)).whenever(userRepository).findById(1)

        val authenticatedUser = userService.getAuthenticatedUser()

        assertEquals(user, authenticatedUser)
    }

    @Test
    fun `fail if there isn't authenticated user`() {
        whenever(principalProviderService.getAuthenticatedPrincipal()).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userService.getAuthenticatedUser() }

        verify(userRepository, times(0)).findByUsername(any())
    }

    @Test
    fun `fail if cannot find t authenticated user`() {
        doReturn(Optional.of(UserPrincipal("1"))).whenever(principalProviderService).getAuthenticatedPrincipal()
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
