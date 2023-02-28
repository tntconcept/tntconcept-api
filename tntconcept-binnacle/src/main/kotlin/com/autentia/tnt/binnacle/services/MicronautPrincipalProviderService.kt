package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.services.PrincipalProviderService
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.security.Principal
import java.util.Optional

@Singleton
internal class MicronautPrincipalProviderService(
    private val securityService: SecurityService
) : PrincipalProviderService {

    override fun getAuthenticatedPrincipal(): Optional<Principal> =
        securityService.authentication as Optional<Principal>

}
