package com.autentia.tnt.binnacle.repositories

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import javax.persistence.EntityManager

interface ArchimedesRepository {
    fun findAllPrincipalNameByRoleName(roleName: String): List<String>
}

@Singleton
internal class DefaultArchimedesRepository(private val entityManager: EntityManager) : ArchimedesRepository {
    private val archRoleRelationTable = "archimedes_security_subject_role_relation"
    private val archSubjectTable = "archimedes_security_subject"

    private val FIND_ALL_PRINCIPAL_NAME_BY_ROLE_NAME_QUERY = """
        SELECT archss.principal_name
        FROM $archSubjectTable archss, $archRoleRelationTable assrr
        WHERE archss.id = assrr.subject_id AND assrr.role_name = ?
    """.trimIndent()


    override fun findAllPrincipalNameByRoleName(roleName: String): List<String> {
        val query = entityManager.createNativeQuery(FIND_ALL_PRINCIPAL_NAME_BY_ROLE_NAME_QUERY)
        query.setParameter(1, roleName)

        return try {
            query.resultList as List<String>
        } catch (err: RuntimeException) {
            logger.error("Error obtaining findAllPrincipalNameByRoleName", err)
            listOf()
        }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultArchimedesRepository::class.java)
    }
}