package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.repositories.predicates.UserPredicates
import com.autentia.tnt.binnacle.converters.AbsenceResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.AbsenceFilterDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceResponseDTO
import com.autentia.tnt.binnacle.repositories.AbsenceRepository
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Named
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AbsencesByFilterUseCase internal constructor(
    @param:Named("Internal") private val activityRepository: ActivityRepository,
    private val absenceRepository: AbsenceRepository,
    private val userRepository: UserRepository,
    private val absenceResponseConverter: AbsenceResponseConverter,
) {

    @Transactional
    @ReadOnly
    fun getAbsences(absenceFilter: AbsenceFilterDTO): List<AbsenceResponseDTO> {
        require(isAbsenceFilterValid(absenceFilter)) { "At least user ids or organization/project ids should be specified" }

        var userIds = absenceFilter.userIds ?: listOf()

        if (shouldFilterUsersByOrganizationAndProjectActivities(absenceFilter)) {
            val activities = getActivitiesByFilter(absenceFilter)
            val userActivities = activities.map { it.userId }.distinct().toList()

            userIds = if (userIds.isNotEmpty()) {
                (userIds intersect userActivities.toSet()).toList()
            } else {
                userActivities
            }
        }

        if (userIds.isNotEmpty()) {
            val pageable = Pageable.unpaged()
            val users = userRepository.findAll(UserPredicates.fromUserIds(userIds),
                pageable.order("name", Sort.Order.Direction.ASC))
            val absences = absenceRepository.find(absenceFilter.startDate, absenceFilter.endDate, userIds)

            return absenceResponseConverter.toAbsenceResponseDTO(users, absences)
        }
        return emptyList()
    }

    private fun isAbsenceFilterValid(absenceFilter: AbsenceFilterDTO) =
        !absenceFilter.userIds.isNullOrEmpty() || !absenceFilter.organizationIds.isNullOrEmpty() || !absenceFilter.projectIds.isNullOrEmpty()

    private fun shouldFilterUsersByOrganizationAndProjectActivities(absenceFilter: AbsenceFilterDTO) =
        !absenceFilter.organizationIds.isNullOrEmpty() || !absenceFilter.projectIds.isNullOrEmpty()

    private fun getActivitiesByFilter(absenceFilter: AbsenceFilterDTO): List<Activity> {
        val predicate = getActivityPredicate(absenceFilter)
        return activityRepository.findAll(predicate)
    }

    private fun getActivityPredicate(filter: AbsenceFilterDTO): Specification<Activity> {
        var predicate = PredicateBuilder.and(
            ActivityPredicates.startDateLessThanOrEqualTo(filter.endDate),
            ActivityPredicates.endDateGreaterThanOrEqualTo(filter.startDate)
        )

        if (!filter.organizationIds.isNullOrEmpty()) {
            predicate = PredicateBuilder.and(predicate, ActivityPredicates.organizationIds(filter.organizationIds))
        }

        if (!filter.projectIds.isNullOrEmpty()) {
            predicate = PredicateBuilder.and(predicate, ActivityPredicates.projectIds(filter.projectIds))
        }

        return predicate
    }
}