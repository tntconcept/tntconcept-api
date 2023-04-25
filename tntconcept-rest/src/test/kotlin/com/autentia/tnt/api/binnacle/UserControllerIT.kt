package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.Role
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.WorkingAgreement
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import com.autentia.tnt.binnacle.usecases.FindByUserNameUseCase
import com.autentia.tnt.binnacle.usecases.UsersRetrievalUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus.*
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

@MicronautTest
@TestInstance(PER_CLASS)
internal class UserControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(FindByUserNameUseCase::class)
    internal val findByUserNameUseCase = mock<FindByUserNameUseCase>()

    @get:MockBean(UsersRetrievalUseCase::class)
    internal val usersRetrievalUseCase = mock<UsersRetrievalUseCase>()

    @BeforeAll
    fun setup() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get logged user`() {
        val user = User(
            1L,
            "username",
            "password",
            2L,
            "name",
            "photoUrl",
            dayDuration = 24,
            WorkingAgreement(3L, emptySet()),
            null,
            LocalDate.now(),
            "email",
            Role(4, "role"),
            true
        )
        doReturn(user).whenever(findByUserNameUseCase).find()

        val request = HttpRequest.GET<Any>("/api/user/me")

        val response = client.exchange(request, UserResponse::class.java)

        assertEquals(200, response.status.code)
        assertEquals(UserResponse(user), response.body.get())
    }

    @Test
    fun `get users`() {
        whenever(usersRetrievalUseCase.getAllUsers()).thenReturn(listOf(USER_RESPONSE_DTO))

        val response = client.exchangeList<UserResponseDTO>(
            HttpRequest.GET("/api/user"),
        )

        assertEquals(OK, response.status)
        assertEquals(listOf(USER_RESPONSE_DTO), response.body.get())
    }

    private companion object {
        private val USER_RESPONSE_DTO = UserResponseDTO(
            1L,
            "username",
            2L,
            "name",
            "photoUrl",
            dayDuration = 24,
            WorkingAgreement(3L, emptySet()),
            null,
            LocalDate.now(),
            "email",
            Role(4, "role"),
        )
    }

}
