package com.autentia.tnt.security.application

import io.micronaut.context.annotation.Replaces
import io.micronaut.security.ldap.context.LdapSearchResult
import io.micronaut.security.ldap.group.DefaultLdapGroupProcessor
import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
@Replaces(DefaultLdapGroupProcessor::class)
class CustomLdapGroupProcessor(

    @Inject
    private val customSubjectRepository: CustomSubjectRepository) :
    DefaultLdapGroupProcessor() {

    override fun getAdditionalGroups(result: LdapSearchResult?): MutableSet<String> {
        if (result != null) {

            val uid = result.attributes.getValue("uid")?.toString()

            if (uid != null) {
                val subject = customSubjectRepository!!.findByPrincipal("$uid@autentia.com")
                subject?.let {
                    return it.roles.map { role -> role.name }.toMutableSet()
                }
            }
        }
        return mutableSetOf("")
    }
}
