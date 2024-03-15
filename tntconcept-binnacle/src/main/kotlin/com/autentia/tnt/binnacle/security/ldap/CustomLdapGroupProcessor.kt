package com.autentia.tnt.binnacle.security.ldap

import com.autentia.tnt.AppProperties
import io.micronaut.context.annotation.Replaces
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.security.ldap.context.LdapSearchResult
import io.micronaut.security.ldap.group.DefaultLdapGroupProcessor
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional


@Singleton
@Replaces(DefaultLdapGroupProcessor::class)
internal class CustomLdapGroupProcessor : DefaultLdapGroupProcessor() {

    private val findByPrincipal = "SELECT id FROM archimedes_security_subject WHERE principal_name = ?"
    private val findRolesBySubjectId =
        "SELECT role_name FROM archimedes_security_subject_role_relation WHERE subject_id = ?"

    @Inject
    private lateinit var jdbcOperations: JdbcOperations

    @Inject
    private lateinit var appProperties: AppProperties

    @Transactional
    override fun getAdditionalGroups(result: LdapSearchResult?): MutableSet<String> {

        val roles = mutableSetOf<String>()

        if (result != null) {
            val uid = result.attributes.getValue("uid")?.toString()

            if (uid != null) {
                jdbcOperations.prepareStatement(findByPrincipal) {
                    it.setString(1, "$uid@${appProperties.security.subjectNameSuffix}")
                    val rs = it.executeQuery()
                    if (rs.next()) {
                        roles.addAll(findRolesBySubjectId(rs.getInt(1)))
                    }
                }
            }
        }
        return roles
    }

    private fun findRolesBySubjectId(subjectId: Int): MutableSet<String> =
        jdbcOperations.prepareStatement(findRolesBySubjectId) {
            it.setInt(1, subjectId)
            val rs = it.executeQuery()
            val roles = mutableSetOf<String>()
            while (rs.next()) {
                roles.add(rs.getString(1))
            }
            roles
        }

}
