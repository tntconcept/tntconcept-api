package com.autentia.tnt.security.application

import io.micronaut.security.authentication.Authentication
import io.micronaut.security.utils.SecurityService
import java.lang.IllegalArgumentException

private const val ADMIN_ROLE = "admin"

fun SecurityService.checkAuthentication(): Authentication =
    authentication.orElseThrow { IllegalArgumentException("Required authentication.") }

fun SecurityService.checkRole(role: String): Authentication {
    val authentication = checkAuthentication()
    check(authentication.roles.contains(role)) { "Required role" }
    return authentication
}

fun Authentication.isAdmin(): Boolean = roles.contains(ADMIN_ROLE)
fun Authentication.id(): Long = name.toLong()
