package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.Role
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.WorkingAgreement
import com.autentia.tnt.binnacle.usecases.FindByUserNameUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus.FORBIDDEN
import io.micronaut.http.MediaType.APPLICATION_JSON_TYPE
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
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

    val username = "testuser"
    val userId = 2

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    @get:MockBean(FindByUserNameUseCase::class)
    internal val findByUserNameUseCase = mock<FindByUserNameUseCase>()

    @Test
    fun `return logged user`() {
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

        val request = HttpRequest.GET<Any>("/api/user")

        val response = httpClient.toBlocking().exchange(request, UserResponse::class.java)

        assertEquals(200, response.status.code)
        assertEquals(UserResponse(user), response.body.get())
    }

    //   TODO: @Test
    fun getSecuredData() {
        val token = LoginHelper().obtainAccessToken(username, "holahola")

        val client = httpClient.toBlocking()

        val request = HttpRequest.GET<Any>("/api/user/secured")
            .header("Authorization", "Bearer $token")
            .accept(APPLICATION_JSON_TYPE)
//            .contentEncoding("utf-8")

        val response = client.exchange(request, User::class.java)

        assertEquals(response.status.code, FORBIDDEN.code)
    }

}
