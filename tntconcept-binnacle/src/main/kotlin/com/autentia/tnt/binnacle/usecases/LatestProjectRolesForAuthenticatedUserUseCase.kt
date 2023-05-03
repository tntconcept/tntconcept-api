package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.ProjectRolesRecent
import com.autentia.tnt.binnacle.core.domain.StartEndLocalDateTime
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime
import javax.transaction.Transactional

@Singleton
class LatestProjectRolesForAuthenticatedUserUseCase internal constructor(
    private val projectRoleRepository: ProjectRoleRepository,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter,
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val securityService: SecurityService,
    private val projectRoleConverter: ProjectRoleConverter
){

    @Transactional
    @ReadOnly
    fun get(): List<ProjectRoleUserDTO> {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()
        val oneMonthDateRange = oneMonthTimeIntervalFromCurrentDate()
        val dateRange = dateRangeOfCurrentYear()
        val currentYearTimeInterval = TimeInterval.of(dateRange.startDate, dateRange.endDate)

        val activities =
            activityService.getActivitiesOfLatestProjects(currentYearTimeInterval)
        val lastMonthActivities = activityService.filterActivitiesByTimeInterval(oneMonthDateRange, activities)

        val latestUserProjectRoles = lastMonthActivities.map { it.projectRole }.distinct().map { projectRole ->
            val remainingOfProjectRoleForUser = activityCalendarService.getRemainingOfProjectRoleForUser(
                projectRole,
                activities.map(Activity::toDomain),
                currentYearTimeInterval.getDateInterval(),
                userId
            )
            projectRoleConverter.toProjectRoleUser(projectRole, remainingOfProjectRoleForUser, userId)
        }

        return latestUserProjectRoles.map(projectRoleResponseConverter::toProjectRoleUserDTO)
    }

    @Deprecated("Use get method instead")
    @Transactional
    @ReadOnly
    fun getProjectRolesRecent(): List<ProjectRolesRecent> {
        val oneMonthDateRange = oneMonthDateRangeFromCurrentDate()

        val roles = projectRoleRepository.findDistinctProjectRolesBetweenDate(
            oneMonthDateRange.startDate,
            oneMonthDateRange.endDate
        )

        return roles
            .filter { it.projectOpen }
            .sortedByDescending { it.date }
            .distinctBy { it.id }
    }

    private fun oneMonthDateRangeFromCurrentDate(): StartEndLocalDateTime {
        val now = LocalDate.now()
        val startDate = now.minusMonths(1).atTime(LocalTime.MIN)
        val endDate = now.atTime(23, 59, 59)

        return StartEndLocalDateTime(startDate, endDate)
    }

    private fun oneMonthTimeIntervalFromCurrentDate(): TimeInterval {
        val now = LocalDate.now()
        val startDate = now.minusMonths(1).atTime(LocalTime.MIN)
        val endDate = now.atTime(23, 59, 59)

        return TimeInterval.of(startDate, endDate)
    }

    private fun dateRangeOfCurrentYear(): StartEndLocalDateTime {
        val now = LocalDate.now()
        val startDate = LocalDate.of(now.year, 1, 1).atTime(LocalTime.MIN)
        val endDate = LocalDate.of(now.year, 12, 31).atTime(23, 59, 59)

        return StartEndLocalDateTime(startDate, endDate)
    }
}