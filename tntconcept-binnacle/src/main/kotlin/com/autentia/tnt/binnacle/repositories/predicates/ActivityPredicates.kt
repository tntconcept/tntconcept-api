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

    internal fun startAfter(date: LocalDate) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.greaterThan(root.get("start"), date)
    }

    internal fun endBefore(date: LocalDate) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.lessThan(root.get("end"), date)
    }

    internal fun roleId(roleId: Long) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.equal(root.get<Long>("roleId"), roleId)
    }
}