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
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.userId
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton


@Singleton
class ActivitiesByFilterUseCase internal constructor(
    private val activityService: ActivityService,
    private val securityService: SecurityService,
    private val activityResponseConverter: ActivityResponseConverter,
) {
    fun getActivities(activityFilter: ActivityFilterDTO): List<ActivityResponseDTO> {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()
        val predicate: Specification<Activity> = getPredicateFromActivityFilter(activityFilter, userId)
        val activities = activityService.getActivities(predicate)
        return activities.map { activityResponseConverter.toActivityResponseDTO(it) }
    }

    private fun getPredicateFromActivityFilter(activityFilter: ActivityFilterDTO, userId: Long): Specification<Activity> {
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

        predicate = if (activityFilter.userId !== null) {
            PredicateBuilder.and(predicate, userId(activityFilter.userId))
        } else {
            PredicateBuilder.and(predicate, userId(userId))
        }
        return predicate
    }
}