package com.autentia.tnt.api.binnacle

import io.archimedesfw.security.auth.Subject

internal data class UserSecurityResponse(
    private val username: String,
    private val roles: List<String>
) {

    internal constructor(subject: Subject) : this(subject.principal.name, subject.roles.map { it.name })

}
