package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ProjectRole
import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import jakarta.inject.Singleton


@Singleton
class ProjectRoleConverter {

    fun toProjectRoleUser(projectRole: ProjectRole, remaining: Int, userId: Long) =
        ProjectRoleUser(
            projectRole.id,
            projectRole.name,
            projectRole.project.organization.id,
            projectRole.project.id,
            projectRole.getMaxTimeAllowedByYearInUnits(),
            remaining,
            projectRole.getTimeUnit(),
            projectRole.requireEvidence,
            projectRole.isApprovalRequired,
            userId
        )

}