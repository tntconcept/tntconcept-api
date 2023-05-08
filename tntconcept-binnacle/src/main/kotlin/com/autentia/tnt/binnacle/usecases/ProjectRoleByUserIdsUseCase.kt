package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import javax.transaction.Transactional

@Singleton
class ProjectRoleByUserIdsUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter,
    private val projectRoleConverter: ProjectRoleConverter
) {

    @Transactional
    @ReadOnly
    fun get(userIds: List<Long>): List<ProjectRoleUserDTO> {

        val currentYearTimeInterval = TimeInterval.ofYear(LocalDate.now().year)

        val activities =
            activityService.getActivities(currentYearTimeInterval, userIds)

        val projectRoles = mutableListOf<ProjectRoleUser>()
        userIds.forEach { userId ->
            val userProjectRoles =
                activities.asSequence().filter { it.userId == userId }.map { it.projectRole.toDomain() }.distinct()
                    .map { projectRole ->
                        val remainingOfProjectRoleForUser = activityCalendarService.getRemainingOfProjectRoleForUser(
                            projectRole,
                            activities.map(Activity::toDomain),
                            currentYearTimeInterval.getDateInterval(),
                            userId
                        )
                        projectRoleConverter.toProjectRoleUser(projectRole, remainingOfProjectRoleForUser, userId)
                    }.toList()
            projectRoles.addAll(userProjectRoles)
        }


        return projectRoles.map(projectRoleResponseConverter::toProjectRoleUserDTO)
    }
}