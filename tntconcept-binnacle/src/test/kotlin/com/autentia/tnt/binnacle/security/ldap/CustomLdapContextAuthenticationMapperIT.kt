package com.autentia.tnt.binnacle.security.ldap

import com.autentia.tnt.security.application.id
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.testcontainers.shaded.org.hamcrest.Matchers

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomLdapContextAuthenticationMapperIT {

    @Inject
    private lateinit var customLdapContextAuthenticationMapper: CustomLdapContextAuthenticationMapper

    @ParameterizedTest
    @CsvSource(value = ["usuario.prueba1, 11", "usuario.prueba2, 12", "usuario.prueba3, 133"])
    fun `should map username to id on authenticated response`(username: String, id: Long) {
        val roles = mutableSetOf("user")
        val authenticationResponse = customLdapContextAuthenticationMapper.map(null, username, roles)
        assertTrue(authenticationResponse.isAuthenticated)
        assertEquals(id, authenticationResponse.authentication.get().id())
        assertThat(authenticationResponse.authentication.get().roles).containsAll(roles)
    }

    @Test
    fun `should fail authentication if not username found`() {
        val authenticationResponse = customLdapContextAuthenticationMapper.map(null, "unexistant", mutableSetOf("user"))
        assertFalse(authenticationResponse.isAuthenticated)
    }
}
