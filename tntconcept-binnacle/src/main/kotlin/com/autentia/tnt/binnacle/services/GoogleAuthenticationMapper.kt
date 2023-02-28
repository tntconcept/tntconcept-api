package com.autentia.tnt.binnacle.services

import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.oauth2.endpoint.authorization.state.State
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdAuthenticationMapper
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdClaims
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdTokenResponse
import jakarta.inject.Named
import jakarta.inject.Singleton

@Singleton
@Named("google")
internal class GoogleOpenIdAuthenticationMapper : OpenIdAuthenticationMapper {

    override fun createAuthenticationResponse(
        providerName: String,
        tokenResponse: OpenIdTokenResponse,
        openIdClaims: OpenIdClaims,
        state: State?
    ): AuthenticationResponse {
        val email = openIdClaims.email
            ?: return AuthenticationResponse.failure("Field email does not exist in JWT token")

        val username = email.substringBefore(EMAIL_AT_CHARACTER)

        return AuthenticationResponse.success(username, mapOf(SUBJECT_TOKEN_KEY to username))
    }

    private companion object {
        const val SUBJECT_TOKEN_KEY = "sub"
        const val EMAIL_AT_CHARACTER = "@"
    }
}
