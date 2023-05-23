package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.entities.dto.UserInfoResponseDTO
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.security.application.id
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

internal class FindUserInfoUseCaseTest {

    private val userService = mock<UserService>()
    private val securityService = mock<SecurityService>()

    private val findUserInfoUseCase = FindUserInfoUseCase(securityService, userService)

    @Test
    fun `find user by user username`() {

        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
        whenever(userService.getById(authenticatedUser.id())).thenReturn(USER)

        assertEquals(USER_INFO_RESPONSE_DTO, findUserInfoUseCase.find())
    }

    private companion object{
        private val USER = createUser()

        private val authenticatedUser = ClientAuthentication(
            USER.id.toString(), mapOf("roles" to listOf("user"))
        )

        private val USER_INFO_RESPONSE_DTO = UserInfoResponseDTO(USER, authenticatedUser.roles.stream().toList())
    }
}
