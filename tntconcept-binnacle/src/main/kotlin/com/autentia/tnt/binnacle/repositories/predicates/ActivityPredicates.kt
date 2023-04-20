package com.autentia.tnt.binnacle.repositories.predicates

import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import io.micronaut.data.jpa.repository.criteria.Specification
import java.time.LocalDate

internal object ActivityPredicates {

    internal val ALL = Specification<Activity> { _, _, _ -> null }

    internal fun id(id: Long) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.equal(root.get<Long>("id"), id)
    }

    internal fun approvalState(approvalState: ApprovalState) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.equal(root.get<ApprovalState>("approvalState"), approvalState)
    }

    internal fun startDateAfter(date: LocalDate) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.greaterThan(root.get("start"), date)
    }

    internal fun endBefore(date: LocalDate) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.lessThan(root.get("end"), date)
    }

    internal fun startDateLessThanOrEqualTo(date: LocalDate) = Specification<Activity> { root, _, criteriaBuilder ->
        val dateTime = date.atTime(23, 59, 59)
        criteriaBuilder.lessThanOrEqualTo(root.get("start"), dateTime)
    }

    internal fun endDateGreaterThanOrEqualTo(date: LocalDate) = Specification<Activity> { root, _, criteriaBuilder ->
        val dateTime = date.atStartOfDay()
        criteriaBuilder.greaterThanOrEqualTo(root.get("end"), dateTime)
    }

    internal fun roleId(roleId: Long) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.equal(root.get<Long>("projectRole").get<Long>("id"), roleId)
    }
}