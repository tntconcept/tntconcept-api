package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import javax.transaction.Transactional


@Singleton
class ProjectRoleByProjectIdUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter
) {
    @Transactional
    @ReadOnly
    fun get(projectId: Long): List<ProjectRoleUserDTO> {

        val currentYearTimeInterval = TimeInterval.ofYear(LocalDate.now().year)

        val activities =
            activityService.getActivitiesByProjectId(currentYearTimeInterval, projectId)

        val remainingGroupedByProjectRoleAndUserId = activityCalendarService.getRemainingGroupedByProjectRoleAndUser(
            activities.map(Activity::toDomain), currentYearTimeInterval.getDateInterval()
        )

        return remainingGroupedByProjectRoleAndUserId.map(projectRoleResponseConverter::toProjectRoleUserDTO)
    }
}