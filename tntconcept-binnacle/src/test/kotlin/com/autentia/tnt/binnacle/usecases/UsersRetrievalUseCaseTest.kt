package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.UserResponseConverter
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.UserPredicates
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class UsersRetrievalUseCaseTest {
    private val userRepository = mock<UserRepository>()

    private val usersRetrievalUseCase = UsersRetrievalUseCase(userRepository, UserResponseConverter())

    @Test
    fun `should return all users`() {
        whenever(userRepository.findAll(UserPredicates.ALL)).thenReturn(listOf(createUser()))

        val actual = usersRetrievalUseCase.getUsers(null, null)

        assertEquals(listOf(userResponseDTO), actual)
    }

    @Test
    fun `should return active users`() {
        whenever(userRepository.findAll(UserPredicates.isActive(true))).thenReturn(listOf(createUser()))

        val actual = usersRetrievalUseCase.getUsers(null, true)

        assertEquals(listOf(userResponseDTO), actual)
    }

    @Test
    fun `should return not active users`() {
        whenever(userRepository.findAll(UserPredicates.isActive(false))).thenReturn(listOf(createUser()))

        val actual = usersRetrievalUseCase.getUsers(null, false)

        assertEquals(listOf(userResponseDTO), actual)
    }

    @Test
    fun `should return list of given users`() {
        whenever(userRepository.findAll(UserPredicates.fromUserIds(listOf(1, 2, 3)))).thenReturn(listOf(createUser()))

        val actual = usersRetrievalUseCase.getUsers(listOf(1, 2, 3), null)

        assertEquals(listOf(userResponseDTO), actual)
    }

    @Test
    fun `should return list of active users given a list of users`() {
        val compositedSpecification =
            PredicateBuilder.and(
                UserPredicates.fromUserIds(listOf(1, 2, 3)),
                UserPredicates.isActive(true)
            )
        whenever(userRepository.findAll(compositedSpecification)).thenReturn(listOf(createUser()))

        val actual = usersRetrievalUseCase.getUsers(listOf(1, 2, 3), true)

        assertEquals(listOf(userResponseDTO), actual)
    }

    @Test
    fun `should return list of not active users given a list of users`() {
        val compositedSpecification =
            PredicateBuilder.and(
                UserPredicates.fromUserIds(listOf(1, 2, 3)),
                UserPredicates.isActive(false)
            )
        whenever(userRepository.findAll(compositedSpecification)).thenReturn(listOf(createUser()))

        val actual = usersRetrievalUseCase.getUsers(listOf(1, 2, 3), false)

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