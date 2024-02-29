package com.autentia.tnt.binnacle.security.ldap

import io.micronaut.security.ldap.context.LdapSearchResult
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.naming.directory.BasicAttributes

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomLdapGroupProcessorIT {

    @Inject
    private lateinit var customLdapGroupProcessor: CustomLdapGroupProcessor

    @Test
    fun `should get additional groups`() {
        val ldapSearchResult = LdapSearchResult(BasicAttributes("uid", "11"), "dn");

        val groups = customLdapGroupProcessor.getAdditionalGroups(ldapSearchResult)

        assertNotNull(groups)
    }
}
