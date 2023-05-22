package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.time.LocalDate


@Singleton
class ProjectRoleByProjectIdUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val securityService: SecurityService,
    private val projectRoleRepository: ProjectRoleRepository,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter,
    private val projectRoleConverter: ProjectRoleConverter
) {

    fun get(projectId: Long): List<ProjectRoleUserDTO> {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()

        val currentYearTimeInterval = TimeInterval.ofYear(LocalDate.now().year)
        val projectRolesOfProject = projectRoleRepository.getAllByProjectId(projectId).map(ProjectRole::toDomain)
        val projectRolesUser = buildProjectRoleWithUserRemaining(
            projectRolesOfProject,
            currentYearTimeInterval,
            userId,
        )

        return projectRolesUser
            .map(projectRoleResponseConverter::toProjectRoleUserDTO)
    }

    private fun buildProjectRoleWithUserRemaining(
        projectRolesOfProject: List<com.autentia.tnt.binnacle.core.domain.ProjectRole>,
        currentYearTimeInterval: TimeInterval,
        userId: Long
    ): MutableList<ProjectRoleUser> {
        val projectRolesUser = mutableListOf<ProjectRoleUser>()

        for (projectRole in projectRolesOfProject) {
            val projectRoleActivities = activityService.getProjectRoleActivities(projectRole.id, userId)
            val remainingOfProjectRoleForUser = activityCalendarService.getRemainingOfProjectRoleForUser(
                projectRole,
                projectRoleActivities.map(Activity::toDomain),
                currentYearTimeInterval.getDateInterval(),
                userId
            )
            val projectRoleUser = projectRoleConverter.toProjectRoleUser(projectRole, remainingOfProjectRoleForUser, userId)
            projectRolesUser.add(projectRoleUser)
        }
        return projectRolesUser
    }
}