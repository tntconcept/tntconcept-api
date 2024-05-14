package com.autentia.tnt.security.application

import io.micronaut.security.authentication.Authentication
import io.micronaut.security.utils.SecurityService

private const val ADMIN_ROLE = "admin"
private const val ACTIVITY_APPROVAL_ROLE = "activity-approval"
private const val BLOCK_PROJECT_ROLE = "project-blocker"
private const val SUBCONTRACTED_ACTIVITY_MANAGER_ROLE = "subcontracted-activity-manager"

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

fun SecurityService.checkBlockProjectsRole(): Authentication {
    return checkRole(BLOCK_PROJECT_ROLE)
}

fun SecurityService.checkSubcontractedActivityManagerRole(): Authentication {
    return checkRole(SUBCONTRACTED_ACTIVITY_MANAGER_ROLE)
}

fun Authentication.isAdmin(): Boolean = roles.contains(ADMIN_ROLE)
private fun Authentication.isActivityApproval(): Boolean = roles.contains(ACTIVITY_APPROVAL_ROLE)
private fun Authentication.isBlockProject(): Boolean = roles.contains(BLOCK_PROJECT_ROLE)

private fun Authentication.isSubcontractedActivityManager(): Boolean = roles.contains(SUBCONTRACTED_ACTIVITY_MANAGER_ROLE)

fun Authentication.canAccessAllUsers() = isAdmin() || isActivityApproval() || isBlockProject()

fun Authentication.canAccessAllActivities() = isAdmin() || isActivityApproval() || canSubcontract()

fun Authentication.canAccessAllAttachments() = isAdmin() || isActivityApproval()

fun Authentication.canBlockProjects() = isAdmin() || isBlockProject()

fun Authentication.canSubcontract() = isSubcontractedActivityManager()

fun Authentication.id(): Long = name.toLong()
