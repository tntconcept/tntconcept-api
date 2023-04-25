package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.ProjectRole
import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.services.ActivityCalendarService

abstract class ProjectRoleUserUseCase internal constructor(
    private val activityCalendarService: ActivityCalendarService,
){

    fun buildProjectRoleWithUserRemaining(projectRole: ProjectRole, activities: List<Activity>, userId: Long, dateInterval: DateInterval) : ProjectRoleUser{
        val remainingOfProjectRoleForUser = activityCalendarService.getRemainingOfProjectRoleForUser(
                projectRole,
                activities.map(Activity::toDomain),
                dateInterval,
                userId
            )
            return ProjectRoleUser(
                projectRole.id,
                projectRole.name,
                projectRole.project.organization.id,
                projectRole.project.id,
                projectRole.getMaxAllowedInUnits(),
                remainingOfProjectRoleForUser,
                projectRole.timeUnit,
                projectRole.requireEvidence,
                projectRole.isApprovalRequired,
                userId
            )
    }

}