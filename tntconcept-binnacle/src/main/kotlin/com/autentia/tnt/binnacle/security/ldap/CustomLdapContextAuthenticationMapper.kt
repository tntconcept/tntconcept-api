package com.autentia.tnt.binnacle.security.ldap

import com.autentia.tnt.AppProperties
import io.micronaut.context.annotation.Replaces
import io.micronaut.core.convert.value.ConvertibleValues
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.security.authentication.AuthenticationException
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationFailureReason
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.ldap.DefaultContextAuthenticationMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional


@Replaces(DefaultContextAuthenticationMapper::class)
@Singleton
internal class CustomLdapContextAuthenticationMapper : DefaultContextAuthenticationMapper() {

    private val findIdByPrincipalName = "select id from archimedes_security_subject where principal_name = ?"

    @Inject
    private lateinit var jdbcOperations: JdbcOperations

    @Inject
    private lateinit var appProperties: AppProperties

    @Transactional
    override fun map(
        attributes: ConvertibleValues<Any>?,
        username: String?,
        groups: MutableSet<String>?
    ): AuthenticationResponse {
        val id =
            jdbcOperations.prepareStatement(findIdByPrincipalName) {
                it.setString(1, "$username@${appProperties.security.subjectNameSuffix}")
                val rs = it.executeQuery()
                if (rs.next()) {
                    rs.getInt("id")
                } else {
                    -1
                }
            }

        if (id < 0) return AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND) else return super.map(attributes, "$id", groups)
    }
}
