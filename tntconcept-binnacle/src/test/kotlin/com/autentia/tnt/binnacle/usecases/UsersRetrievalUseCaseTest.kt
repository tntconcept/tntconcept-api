package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.UserResponseConverter
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import com.autentia.tnt.binnacle.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class UsersRetrievalUseCaseTest {
    private val userRepository = mock<UserRepository>()

    private val usersRetrievalUseCase = UsersRetrievalUseCase(userRepository, UserResponseConverter())

    @Test
    fun `should return the list of users`() {
        whenever(userRepository.find()).thenReturn(listOf(createUser()))

        val actual = usersRetrievalUseCase.getAllActiveUsers()

        assertEquals(listOf(userResponseDTO), actual)
    }

    private companion object {
        val userResponseDTO =
            UserResponseDTO(
                createUser().id,
                createUser().username,
                createUser().name,
            )
    }
}