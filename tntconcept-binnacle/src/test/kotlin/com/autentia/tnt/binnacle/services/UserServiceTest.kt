package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.repositories.UserRepositorySecured
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

internal class UserServiceTest {

    private val userRepositorySecured =  mock<UserRepositorySecured>()
    private val securityService = mock<SecurityService>()

    private var userService = UserService(userRepositorySecured)

    @Test
    fun `get authenticated user`() {
        val user = createUser()
        doReturn(Optional.of(user)).whenever(userRepositorySecured).findByAuthenticatedUser()

        val authenticatedUser = userService.getAuthenticatedUser()

        assertEquals(user, authenticatedUser)
    }

    @Test
    fun `fail if there isn't authenticated user`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userService.getAuthenticatedUser() }
    }

    @Test
    fun `get user by username`() {
        val user = createUser()
        doReturn(user).whenever(userRepositorySecured).findByUsername(user.username)

        userService.getUserByUserName(user.username)

        verify(userRepositorySecured, times(1)).findByUsername(user.username)
    }

    @Test
    fun `fail if there isn't authenticated user fetching the user by username`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { userService.getUserByUserName(createUser().username) }
    }


}
