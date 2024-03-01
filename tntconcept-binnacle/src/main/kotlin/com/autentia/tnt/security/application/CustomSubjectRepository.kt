package com.autentia.tnt.security.application

import io.archimedesfw.security.auth.Subject
import io.archimedesfw.security.auth.UsernamePrincipal
import io.micronaut.data.jdbc.runtime.JdbcOperations
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.sql.ResultSet

@Singleton
class CustomSubjectRepository(
    @Inject
    private val jdbcOperations: JdbcOperations,
    @Inject
    private val subjectRoleRelationDao: CustomSubjectRoleRelationDao,
    @Inject
    private val mapToJsonConverter: CustomMapToJsonConverter
) {

    private val table = "archimedes_security_subject"
    private val select = "SELECT id, principal_name, attributes FROM $table"
    private val findById = "$select WHERE id = ?"
    private val findByPrincipal = "$select WHERE principal_name = ?"

    fun findById(id: Int): Subject? =
        jdbcOperations.prepareStatement(findById) {
            it.setInt(1, id)
            val rs = it.executeQuery()
            extractData(rs)
        }.singleOrNull()

    private fun extractData(rs: ResultSet): List<Subject> {
        val subjects = mutableListOf<Subject>()
        while (rs.next()) {
            subjects.add(extractSubject(rs))
        }
        return subjects
    }

    private fun extractSubject(rs: ResultSet): Subject {
        val id = rs.getInt(1)
        val roles = subjectRoleRelationDao.findRolesBySubjectId(id)
        return Subject(
            id,
            UsernamePrincipal(rs.getString(2)),
            roles,
            mapToJsonConverter.toMap(rs.getString(3))
        )
    }

    fun findByPrincipal(name: String): Subject? =
        jdbcOperations.prepareStatement(findByPrincipal) {
            it.setString(1, name)
            val rs = it.executeQuery()
            extractData(rs)
        }.singleOrNull()

}
