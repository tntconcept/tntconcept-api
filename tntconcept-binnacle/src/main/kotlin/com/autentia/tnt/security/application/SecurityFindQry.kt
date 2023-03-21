package com.autentia.tnt.security.application

import io.archimedesfw.security.auth.Subject
import io.archimedesfw.security.auth.SubjectService
import io.archimedesfw.security.auth.UsernamePrincipal
import io.micronaut.security.utils.SecurityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
@Transactional
@ReadOnly
class SecurityFindQry(
    private val securityService: SecurityService,
    private val subjectService: SubjectService
) {

    fun find(): Subject? {
        val authentication = securityService.authentication.orElse(null)
        return if (authentication == null) return null else find(authentication.name)
    }

    fun find(username: String): Subject? {
        return subjectService.find(UsernamePrincipal(username))
    }

}
