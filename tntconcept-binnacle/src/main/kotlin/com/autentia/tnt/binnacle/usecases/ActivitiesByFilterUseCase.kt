package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivityFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.approvalState
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.endDateGreaterThanOrEqualTo
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.organizationId
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.projectId
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.roleId
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.startDateLessThanOrEqualTo
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.services.ActivityService
import io.micronaut.data.jpa.repository.criteria.Specification
import jakarta.inject.Singleton


@Singleton
class ActivitiesByFilterUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityResponseConverter: ActivityResponseConverter,
) {
    fun getActivities(activityFilter: ActivityFilterDTO): List<ActivityResponseDTO> {
        val predicate: Specification<Activity> = getPredicateFromActivityFilter(activityFilter)
        val activities = activityService.getActivities(predicate)
        return activityResponseConverter.mapActivitiesToActivitiesResponseDTO(activities)
    }

    private fun getPredicateFromActivityFilter(activityFilter: ActivityFilterDTO): Specification<Activity> {
        var predicate: Specification<Activity> = ActivityPredicates.ALL

        if (activityFilter.approvalState !== null) {
            predicate = PredicateBuilder.and(predicate, approvalState(activityFilter.approvalState))
        }

        if (activityFilter.endDate !== null) {
            predicate = PredicateBuilder.and(predicate, startDateLessThanOrEqualTo(activityFilter.endDate))
        }
        if (activityFilter.startDate !== null) {
            predicate = PredicateBuilder.and(predicate, endDateGreaterThanOrEqualTo(activityFilter.startDate))
        }

        if (activityFilter.roleId !== null) {
            predicate = PredicateBuilder.and(predicate, roleId(activityFilter.roleId))
        } else if (activityFilter.projectId !== null) {
            predicate = PredicateBuilder.and(predicate, projectId(activityFilter.projectId))
        } else if (activityFilter.organizationId !== null) {
            predicate = PredicateBuilder.and(predicate, organizationId(activityFilter.organizationId))
        }
        return predicate
    }
}