package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.DateInterval.Companion.getDateIntervalForActivityList
import com.autentia.tnt.binnacle.core.domain.TimeInterval.Companion.getTimeIntervalFromOptionalYear
import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton


@Singleton
class ProjectRoleByProjectIdUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val securityService: SecurityService,
    private val projectRoleRepository: ProjectRoleRepository,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter,
    private val projectRoleConverter: ProjectRoleConverter,
) {

    fun get(projectId: Long, year: Int?, userId: Long?): List<ProjectRoleUserDTO> {
        val projectRoleUserId = getProjectRoleUser(userId)

        val yearTimeInterval = getTimeIntervalFromOptionalYear(year)
        val projectRolesOfProject = projectRoleRepository.getAllByProjectId(projectId).map { it.toDomain() }
        val projectRolesUser = buildProjectRoleWithUserRemaining(
            projectRolesOfProject,
            yearTimeInterval,
            projectRoleUserId,
        )

        return projectRolesUser
            .map(projectRoleResponseConverter::toProjectRoleUserDTO)
    }

    private fun getProjectRoleUser(userId: Long?): Long {
        val authentication = securityService.checkAuthentication()

        return userId ?: authentication.id()
    }

    private fun buildProjectRoleWithUserRemaining(
        projectRolesOfProject: List<com.autentia.tnt.binnacle.core.domain.ProjectRole>,
        yearTimeInterval: TimeInterval,
        userId: Long,
    ): MutableList<ProjectRoleUser> {
        val projectRolesUser = mutableListOf<ProjectRoleUser>()

        for (projectRole in projectRolesOfProject) {
            val projectRoleActivities = activityService.getProjectRoleActivities(projectRole.id, userId)
            val timeIntervalProjectRoleActivities =
                activityService.filterActivitiesByTimeInterval(yearTimeInterval, projectRoleActivities)
                    .filter { it.getYearOfStart() == yearTimeInterval.getYearOfStart() }
            val remainingOfProjectRoleForUser = activityCalendarService.getRemainingOfProjectRoleForUser(
                projectRole,
                timeIntervalProjectRoleActivities,
                getDateIntervalForActivityList(projectRoleActivities.map(Activity::toDomain), yearTimeInterval),
                userId
            )
            val projectRoleUser =
                projectRoleConverter.toProjectRoleUser(projectRole, remainingOfProjectRoleForUser, userId)
            projectRolesUser.add(projectRoleUser)
        }
        return projectRolesUser
    }
}