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
import java.time.LocalDate
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

    private fun getTimeInterval(year: Int?) = TimeInterval.ofYear(year ?: LocalDate.now().year)

    fun getDescriptions(roleIds: List<Long>, year: Int?): SearchResponseDTO {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()
        val projectRoleIds = roleIds.distinct()


        val timeInterval = getTimeInterval(year)
        val projectRoles = projectRoleService.getAllByIds(projectRoleIds)
        val activities = activityService.getActivitiesByProjectRoleIds(timeInterval, projectRoleIds, userId)

        val projectRoleUsers = projectRoles.map { projectRole ->
            val remainingOfProjectRole = activityCalendarService.getRemainingOfProjectRoleForUser(
                projectRole, activities, timeInterval.getDateInterval(), userId
            )
            projectRoleConverter.toProjectRoleUser(projectRole, remainingOfProjectRole, userId)
        }
        return searchConverter.toResponseDTO(projectRoles, projectRoleUsers)
    }
}