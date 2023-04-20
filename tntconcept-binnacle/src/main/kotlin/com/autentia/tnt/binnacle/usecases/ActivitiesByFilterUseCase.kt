package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.services.ActivityService
import jakarta.inject.Singleton

@Singleton
class ActivitiesByFilterUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityResponseConverter: ActivityResponseConverter
) {
    fun getActivities(activityFilterDTO: ActivityFilterDTO): List<ActivityResponseDTO> {
        val predicate = ActivityPredicates.ALL
        activityFilterDTO.approvalState?.let { predicate.and(ActivityPredicates.approvalState(activityFilterDTO.approvalState)) }
        activityFilterDTO.startDate?.let { predicate.and(ActivityPredicates.startAfter(activityFilterDTO.startDate)) }
        activityFilterDTO.endDate?.let { predicate.and(ActivityPredicates.endBefore(activityFilterDTO.endDate)) }
        if (activityFilterDTO.roleId !== null) {
            predicate.and(ActivityPredicates.roleId(activityFilterDTO.roleId))
        } else if (activityFilterDTO.projectId !== null) {
            predicate.and(ActivityPredicates.projectId(activityFilterDTO.projectId))
        } else if (activityFilterDTO.organizationId !== null) {
            predicate.and(ActivityPredicates.organizationId(activityFilterDTO.organizationId))
        }

        val activities = activityService.getActivities(predicate)
        return activityResponseConverter.mapActivitiesToActivitiesResponseDTO(activities)
    }
}