package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.services.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class FindByUserNameUseCaseTest {

    private val userService = mock<UserService>()

    private val findByUserNameUseCase = FindByUserNameUseCase(userService)

    @Test
    fun `find user by user username`() {

        doReturn(USER).whenever(userService).getAuthenticatedUser()

        assertEquals(USER, findByUserNameUseCase.find())
    }

    private companion object{
        private val USER = createUser()
    }
}
