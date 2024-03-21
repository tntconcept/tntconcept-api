package com.autentia.tnt.binnacle.security.ldap

import io.micronaut.context.annotation.Replaces
import io.micronaut.core.convert.value.ConvertibleValues
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.ldap.ContextAuthenticationMapper
import io.micronaut.security.ldap.DefaultContextAuthenticationMapper
import jakarta.inject.Singleton

@Replaces(DefaultContextAuthenticationMapper::class)
@Singleton
class CustomLdapContextAuthenticationMapper: ContextAuthenticationMapper {
    override fun map(
        attributes: ConvertibleValues<Any>?,
        username: String?,
        groups: MutableSet<String>?
    ): AuthenticationResponse {
        return DefaultContextAuthenticationMapper().map(attributes, "13", groups)
    }
}
