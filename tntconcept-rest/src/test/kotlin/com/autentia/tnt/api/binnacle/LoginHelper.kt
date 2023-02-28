package com.autentia.tnt.api.binnacle

import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import jakarta.inject.Inject
import java.util.*

class LoginHelper {

    @Inject
    @Client("/")
    private lateinit var httpClient: HttpClient

    fun obtainAccessToken(username: String, password: String): String {

        val client = httpClient.toBlocking()
        val base64ClientCredentials = String(Base64.getEncoder().encode("tnt-client:hola".toByteArray()))

        val request = HttpRequest.POST<Any>(
            UriBuilder
                .of("/oauth/token")
                .queryParam("grant_type", "password")
                .queryParam("username", username)
                .queryParam("password", password)
                .queryParam("scope", "tnt")
                .build(),
            ""
        )
            .header("Authorization", base64ClientCredentials)
            .accept(MediaType.APPLICATION_JSON_TYPE)
//            .contentEncoding("utf-8")

        val response = client.retrieve(request, OauthResponse::class.java)

        return response.access_token!!
    }

    private class OauthResponse {
        val access_token: String? = null
    }

}
