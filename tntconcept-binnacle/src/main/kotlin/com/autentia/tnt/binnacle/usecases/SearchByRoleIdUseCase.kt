package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleConverter
import com.autentia.tnt.binnacle.converters.SearchConverter
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ProjectRoleService
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.time.LocalDateTime

@Singleton
class SearchByRoleIdUseCase internal constructor(
    private val projectRoleService: ProjectRoleService,
    private val activityService: ActivityService,
    private val securityService: SecurityService,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRoleConverter: ProjectRoleConverter,
    private val searchConverter: SearchConverter,

    ) {

    fun getDescriptions(roleIds: List<Long>): SearchResponseDTO {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()
        val projectRoleIds = roleIds.distinct()
        val currentYearTimeInterval = TimeInterval.ofYear(LocalDateTime.now().year)

        val projectRoles = projectRoleService.getAllByIds(projectRoleIds)
        val activities = activityService.getActivitiesByProjectRoleIds(currentYearTimeInterval, projectRoleIds)

        val projectRoleUsers = projectRoles.map { projectRole ->
            val remainingOfProjectRole = activityCalendarService.getRemainingOfProjectRoleForUser(
                projectRole, activities, currentYearTimeInterval.getDateInterval(), userId
            )
            projectRoleConverter.toProjectRoleUser(projectRole, remainingOfProjectRole, userId)
        }
        return searchConverter.toResponseDTO(projectRoles, projectRoleUsers)
    }
}