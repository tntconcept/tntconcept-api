package com.autentia.tnt.binnacle.security.ldap

import com.autentia.tnt.AppProperties
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Replaces
import io.micronaut.core.convert.value.ConvertibleValues
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationFailureReason
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.ldap.ContextAuthenticationMapper
import io.micronaut.security.ldap.DefaultContextAuthenticationMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional


@Replaces(DefaultContextAuthenticationMapper::class)
@Singleton
internal class CustomLdapContextAuthenticationMapper : DefaultContextAuthenticationMapper() {

    private val findIdByPrincipalName = "select id, attributes from archimedes_security_subject where principal_name = ?"

    private val objectMapper: ObjectMapper = ObjectMapper()

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
        val subjectData:Map<String, Any> =
            jdbcOperations.prepareStatement(findIdByPrincipalName) {
                it.setString(1, "$username@${appProperties.security.subjectNameSuffix}")
                val rs = it.executeQuery()
                if (rs.next()) {
                    val jsonAttributes = rs.getString("attributes")
                    val attributesMap:Map<String, Any> = if (jsonAttributes.isBlank()) emptyMap() else objectMapper.readValue(jsonAttributes, Map::class.java) as Map<String, Any>
                    mapOf(
                        "id" to rs.getLong("id").toString(),
                        "attributes" to HashMap(attributesMap)
                    )
                } else {
                    mapOf()
                }
            }
        if(subjectData.isEmpty()) {
            return AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND)
        } else {
            val attributesMap = subjectData["attributes"] as Map<String, Any>
            val userId = attributesMap["sub"] as String
            return AuthenticationResponse.success(userId, groups, attributesMap)
        }
    }
}
