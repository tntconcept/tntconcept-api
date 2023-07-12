package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.entities.dto.UserInfoResponseDTO
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.security.application.id
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

internal class FindUserInfoUseCaseTest {

    private val userRepository = mock<UserRepository>()
    private val securityService = mock<SecurityService>()

    private val findUserInfoUseCase = FindUserInfoUseCase(securityService, userRepository)

    @Test
    fun `find authenticated user`() {

        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
        whenever(userRepository.find(authenticatedUser.id())).thenReturn(USER)

        assertEquals(USER_INFO_RESPONSE_DTO, findUserInfoUseCase.find())
    }

    @Test
    fun `find authenticated user should throw IllegalStateException`() {

        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
        whenever(userRepository.find(authenticatedUser.id())).thenReturn(null)

        assertThrows<IllegalStateException> { findUserInfoUseCase.find() }
    }

    private companion object{
        private val USER = createUser()

        private val roles = listOf("user")

        private val authenticatedUser = ClientAuthentication(
            USER.id.toString(), mapOf("roles" to roles)
        )

        private val USER_INFO_RESPONSE_DTO = UserInfoResponseDTO(USER.id, USER.username, USER.hiringDate, roles)
    }
}
