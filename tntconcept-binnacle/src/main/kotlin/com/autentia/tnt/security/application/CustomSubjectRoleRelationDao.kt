package com.autentia.tnt.security.application

import io.archimedesfw.security.auth.Role
import io.micronaut.data.jdbc.runtime.JdbcOperations
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.sql.ResultSet

@Singleton
class CustomSubjectRoleRelationDao(
    @Inject
    private val jdbcOperations: JdbcOperations
) {

    private val table = "archimedes_security_subject_role_relation"
    private val select = "SELECT subject_id, role_name FROM $table"
    private val findBySubjectId = "$select WHERE subject_id = ? ORDER BY role_name"

    internal fun findRolesBySubjectId(subjectId: Int): Set<Role> =
        jdbcOperations!!.prepareStatement(findBySubjectId) {
            it.setInt(1, subjectId)
            val rs = it.executeQuery()
            extractData(rs)
        }

    private fun extractData(rs: ResultSet): Set<Role> {
        val roles = mutableSetOf<Role>()
        while (rs.next()) {
            roles.add(Role(rs.getString(2)))
        }
        return roles
    }

}
