package com.autentia.tnt.binnacle.security.ldap

import com.autentia.tnt.security.application.id
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomLdapContextAuthenticationMapperIT {

    @Inject
    private lateinit var customLdapContextAuthenticationMapper: CustomLdapContextAuthenticationMapper

    @ParameterizedTest
    @CsvSource(value = ["usuario.prueba1, 11", "usuario.prueba2, 12"])
    fun `should map username to id on authenticated response`(username: String, id: Long) {
        val roles = mutableSetOf("user")
        val authenticationResponse = customLdapContextAuthenticationMapper.map(null, username, roles)
        assertTrue(authenticationResponse.isAuthenticated)
        assertEquals(id, authenticationResponse.authentication.get().id())
        assertEquals(roles, authenticationResponse.authentication.get().roles)
    }

    @Test
    fun `should fail authentication if not username found`() {
        val authenticationResponse = customLdapContextAuthenticationMapper.map(null, "unexistant", mutableSetOf("user"))
        assertFalse(authenticationResponse.isAuthenticated)
    }
}
