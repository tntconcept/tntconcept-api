package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ProjectRoleResponse
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.services.ActivityService
import jakarta.inject.Singleton
import java.time.temporal.ChronoUnit

@Singleton
class ProjectRoleResponseConverter internal constructor(
    private val activityService: ActivityService
){

    fun toProjectRoleDTO(projectRole: ProjectRole): ProjectRoleDTO = ProjectRoleDTO(
        id = projectRole.id,
        name = projectRole.name,
        requireEvidence = projectRole.requireEvidence,
    )
    fun toProjectRoleResponseDTO(projectRole: ProjectRoleResponse): ProjectRoleResponseDTO = ProjectRoleResponseDTO(
        id = projectRole.id,
        name = projectRole.name,
        organizationId = projectRole.organizationId,
        projectId = projectRole.projectId,
        maxAllowed = projectRole.maxAllowed,
        remaining = setRemainingTime(projectRole.userId, projectRole.id, projectRole.timeUnit, projectRole.maxAllowed),
        timeUnit = projectRole.timeUnit,
        requireEvidence = projectRole.requireEvidence,
        requireApproval = projectRole.requireApproval,
        userId = projectRole.userId
    )

    private fun setRemainingTime( userId: Long, projectRoleId: Long, timeUnit: TimeUnit, maxAllowed: Int): Int{
        if (maxAllowed == 0) return 0
        val activitiesForGivenRole = activityService.getActivitiesForAGivenProjectRoleAndUser(projectRoleId, userId)
        val projectRoleWorkedTime = if (timeUnit == TimeUnit.MINUTES) {
            activitiesForGivenRole.sumOf { getDurationInMinutes(it) }
        }else{
            activitiesForGivenRole.sumOf { getDurationInDays(it) }
        }
        return maxAllowed - projectRoleWorkedTime
    }

    private fun getDurationInMinutes(activity: Activity): Int{
        return activity.getTimeInterval().getDuration().toMinutes().toInt()
    }

    private fun getDurationInDays(activity: Activity): Int{
        val start = activity.start.truncatedTo(ChronoUnit.DAYS)
        val end = activity.end.truncatedTo(ChronoUnit.DAYS)
        return if(start.equals(end)) {
            1
        }else {
            activity.getTimeInterval().getDuration().toDays().toInt()
        }
    }
}
