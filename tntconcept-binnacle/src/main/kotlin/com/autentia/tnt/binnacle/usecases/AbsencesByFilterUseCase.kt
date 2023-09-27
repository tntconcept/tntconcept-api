package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.AbsenceResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.AbsenceDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceFilterDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceType
import com.autentia.tnt.binnacle.repositories.AbsenceRepository
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Named
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AbsencesByFilterUseCase internal constructor(
    @param:Named("Internal") private val activityRepository: ActivityRepository,
    private val absenceRepository: AbsenceRepository,
    private val absenceResponseConverter: AbsenceResponseConverter,
) {
    @Transactional
    @ReadOnly
    fun getAbsences(absenceFilter: AbsenceFilterDTO): List<AbsenceDTO> {
        var userIds = absenceFilter.userIds
        if (absenceFilter.userIds.isNullOrEmpty()) {
            val predicate = getActivityPredicate(absenceFilter)
            val activities = activityRepository.findAll(predicate)
            userIds = activities.map { it.userId }.toSet().toList()
        }

        val absences = absenceRepository.find(absenceFilter.startDate, absenceFilter.endDate, userIds)
        return absences.map {
            absenceResponseConverter.toAbsenceDTO(it)
        }
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