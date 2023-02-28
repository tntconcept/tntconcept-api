package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.usecases.FindByUserNameUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class UserControllerTest {

    private val user = createUser()
    private val findByUserNameUseCase = mock<FindByUserNameUseCase>()

    private val userController = UserController(findByUserNameUseCase)

    @Test
    fun `should return the current user`() {
        doReturn(user).whenever(findByUserNameUseCase).find()

        assertEquals(UserResponse(user), userController.getLoggedUser())
    }
}
