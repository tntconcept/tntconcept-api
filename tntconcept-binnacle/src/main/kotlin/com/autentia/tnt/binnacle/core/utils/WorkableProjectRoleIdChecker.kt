package com.autentia.tnt.binnacle.core.utils

import com.autentia.tnt.binnacle.core.domain.ProjectRoleId

class WorkableProjectRoleIdChecker(private val notWorkableRoles: List<ProjectRoleId>) {
    fun isWorkable(projectRoleId: ProjectRoleId) = !notWorkableRoles.contains(projectRoleId)
}

