package com.autentia.tnt.binnacle.repositories.predicates

import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import io.micronaut.data.jpa.repository.criteria.Specification
import java.time.LocalDate
import javax.persistence.criteria.JoinType

internal object ActivityPredicates {

    internal val ALL =
        Specification<Activity> { _, _, _ -> null }

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
        criteriaBuilder.equal(root.get<ProjectRole>("projectRole").get<Long>("id"), roleId)
    }

    internal fun projectId(projectId: Long) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.equal(
            root.join<Activity, ProjectRole>("projectRole", JoinType.INNER)
                .join<ProjectRole, Project>("project", JoinType.INNER).get<Long>("id"), projectId
        )
    }

    internal fun organizationId(organizationId: Long) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.equal(
            root.join<Activity, ProjectRole>("projectRole", JoinType.INNER)
                .join<ProjectRole, Project>("project", JoinType.INNER).join<Project, Organization>("organization")
                .get<Long>("id"), organizationId
        )
    }

    internal fun userId(userId: Long) = Specification<Activity> { root, _, criteriaBuilder ->
        criteriaBuilder.equal(root.get<Long>("userId"), userId)
    }
}