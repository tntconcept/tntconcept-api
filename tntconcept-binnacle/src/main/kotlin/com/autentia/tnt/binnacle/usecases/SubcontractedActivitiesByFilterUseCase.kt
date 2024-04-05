package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityFilterDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityResponseDTO
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.endDateGreaterThanOrEqualTo
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.organizationId
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.projectId
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.roleId
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.startDateLessThanOrEqualTo
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.security.application.checkSubcontractedActivityManagerRole
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.security.utils.SecurityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional


@Singleton
class SubcontractedActivitiesByFilterUseCase internal constructor(
    private val activityRepository: ActivityRepository,
    private val securityService: SecurityService,
    private val activityResponseConverter: ActivityResponseConverter,
) {

    @Transactional
    @ReadOnly
    fun getActivities(activityFilter: SubcontractedActivityFilterDTO): List<SubcontractedActivityResponseDTO> {

        securityService.checkSubcontractedActivityManagerRole()
        val predicate: Specification<Activity> = getPredicateFromActivityFilter(activityFilter)
        val activities = activityRepository.findAll(predicate).map { it.toDomain() }
        return activities.map { activityResponseConverter.toSubcontractedActivityResponseDTO(it) }
    }

    private fun getPredicateFromActivityFilter(activityFilter: SubcontractedActivityFilterDTO): Specification<Activity> {
        var predicate: Specification<Activity> = ActivityPredicates.ALL

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