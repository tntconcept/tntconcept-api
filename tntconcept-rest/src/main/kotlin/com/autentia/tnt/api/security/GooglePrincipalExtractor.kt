package com.autentia.tnt.api.security

import io.archimedesfw.security.auth.token.oauth2.EmailPrincipalExtractor
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Prototype
import jakarta.inject.Singleton

@Prototype
@Factory
internal class GooglePrincipalExtractor {

    @Singleton
    internal fun principalExtractor() = EmailPrincipalExtractor()

}
