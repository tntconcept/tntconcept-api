package com.autentia.tnt.binnacle.services

import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus.OK
import io.micronaut.http.HttpStatus.SEE_OTHER
import io.micronaut.http.MutableHttpResponse
import io.micronaut.security.config.RedirectConfiguration
import io.micronaut.security.config.RedirectService
import io.micronaut.security.token.jwt.cookie.AccessTokenCookieConfiguration
import io.micronaut.security.token.jwt.cookie.JwtCookieClearerLogoutHandler
import io.micronaut.security.token.jwt.cookie.RefreshTokenCookieConfiguration
import jakarta.inject.Singleton

@Singleton
@Replaces(bean = JwtCookieClearerLogoutHandler::class)
class CustomJwtCookieClearerLogoutHandler(
    accessTokenCookieConfiguration: AccessTokenCookieConfiguration,
    refreshTokenCookieConfiguration: RefreshTokenCookieConfiguration,
    redirectConfiguration: RedirectConfiguration,
    redirectService: RedirectService
) : JwtCookieClearerLogoutHandler(
    accessTokenCookieConfiguration, refreshTokenCookieConfiguration, redirectConfiguration, redirectService
) {
    override fun logout(request: HttpRequest<*>?): MutableHttpResponse<*> {
        val logoutResponse = super.logout(request)

        if (logoutResponse.status == SEE_OTHER) logoutResponse.status(OK)

        return logoutResponse
    }
}