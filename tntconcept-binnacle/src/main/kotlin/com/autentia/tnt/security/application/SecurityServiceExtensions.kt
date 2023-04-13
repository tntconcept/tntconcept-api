package com.autentia.tnt.security.application

import io.micronaut.security.authentication.Authentication
import io.micronaut.security.utils.SecurityService

private const val ADMIN_ROLE = "admin"
private const val STAFF_ROLE = "staff"

fun SecurityService.checkAuthentication(): Authentication =
    authentication.orElseThrow { IllegalStateException("Required authentication") }

fun SecurityService.checkRole(role: String): Authentication {
    val authentication = checkAuthentication()
    check(authentication.roles.contains(role)) { "Required role" }
    return authentication
}

fun Authentication.isAdmin(): Boolean = roles.contains(ADMIN_ROLE)
fun Authentication.isStaff(): Boolean = roles.contains(STAFF_ROLE)
fun Authentication.id(): Long = name.toLong()
