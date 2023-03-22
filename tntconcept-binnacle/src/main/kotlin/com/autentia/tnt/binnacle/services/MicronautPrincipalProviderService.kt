package com.autentia.tnt.binnacle.services

import io.micronaut.security.authentication.Authentication
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.*

@Singleton
internal class MicronautPrincipalProviderService(
    private val securityService: SecurityService
) {

    fun getAuthenticatedPrincipal(): Optional<Authentication> =
        securityService.authentication

}
