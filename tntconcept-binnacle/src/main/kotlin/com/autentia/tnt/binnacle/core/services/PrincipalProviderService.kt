package com.autentia.tnt.binnacle.core.services

import java.security.Principal
import java.util.Optional

interface PrincipalProviderService {
    fun getAuthenticatedPrincipal(): Optional<Principal>
}
