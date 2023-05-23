package com.autentia.tnt.security.application

import io.micronaut.security.authentication.Authentication
import io.micronaut.security.utils.SecurityService

private const val ADMIN_ROLE = "admin"
private const val ACTIVITY_APPROVAL_ROLE = "activity-approval"

fun SecurityService.checkAuthentication(): Authentication =
    authentication.orElseThrow { IllegalStateException("Required authentication") }

fun SecurityService.checkRole(role: String): Authentication {
    val authentication = checkAuthentication()
    check(authentication.roles.contains(role)) { "Required role" }
    return authentication
}

fun SecurityService.checkActivityApprovalRole(): Authentication {
    return checkRole(ACTIVITY_APPROVAL_ROLE)
}

fun Authentication.isAdmin(): Boolean = roles.contains(ADMIN_ROLE)
private fun Authentication.isActivityApproval(): Boolean = roles.contains(ACTIVITY_APPROVAL_ROLE)

fun Authentication.canAccessAllUsers() = isAdmin() || isActivityApproval()
fun Authentication.canNotAccessAllUsers() = !canAccessAllUsers()

fun Authentication.canAccessAllActivities() = isAdmin() || isActivityApproval()
fun Authentication.canNotAccessAllActivities() = !canAccessAllActivities()

fun Authentication.id(): Long = name.toLong()
