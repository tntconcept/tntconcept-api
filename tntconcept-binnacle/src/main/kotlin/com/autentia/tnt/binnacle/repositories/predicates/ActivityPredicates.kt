package com.autentia.tnt.binnacle.repositories.predicates

import com.autentia.tnt.binnacle.entities.*
import io.micronaut.data.jpa.repository.criteria.Specification
import java.time.LocalDate
import javax.persistence.criteria.*

internal object ActivityPredicates {

    internal val ALL = EmptySpecification<Activity>()
    internal fun id(id: Long) = ActivityIdSpecification(id)
    internal fun approvalState(approvalState: ApprovalState): Specification<Activity> =
        ActivityApprovalStateSpecification(approvalState)

    internal fun roleId(roleId: Long) = ActivityRoleIdSpecification(roleId)
    internal fun startDateLessThanOrEqualTo(date: LocalDate) = ActivityStartDateLessOrEqualSpecification(date)
    internal fun endDateGreaterThanOrEqualTo(date: LocalDate) = ActivityEndDateGreaterOrEqualSpecification(date)
    internal fun projectId(projectId: Long) = ActivityProjectIdSpecification(projectId)
    internal fun organizationId(organizationId: Long) = ActivityOrganizationIdSpecification(organizationId)
    internal fun userId(userId: Long) = ActivityUserIdSpecification(userId)
}

class ActivityIdSpecification(private val id: Long) : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.equal(root.get<Long>("id"), id)
    }

    override fun toString(): String {
        return "activity.id==$id"
    }
}

class ActivityApprovalStateSpecification(private val approvalState: ApprovalState) :
    Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.equal(root.get<ApprovalState>("approvalState"), approvalState)
    }

    override fun toString(): String {
        return "activity.approvalState==$approvalState"
    }
}

class ActivityRoleIdSpecification(private val roleId: Long) : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.equal(root.get<ProjectRole>("projectRole").get<Long>("id"), roleId)
    }

    override fun toString(): String {
        return "activity.projectRole.id==$roleId"
    }
}

class ActivityProjectIdSpecification(private val projectId: Long) : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.equal(
            root.join<Activity, ProjectRole>("projectRole", JoinType.INNER)
                .join<ProjectRole, Project>("project", JoinType.INNER).get<Long>("id"), projectId
        )
    }

    override fun toString(): String {
        return "activity.projectRole.project.id==$projectId"
    }
}

class ActivityOrganizationIdSpecification(private val organizationId: Long) : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.equal(
            root.join<Activity, ProjectRole>("projectRole", JoinType.INNER)
                .join<ProjectRole, Project>("project", JoinType.INNER).join<Project, Organization>("organization")
                .get<Long>("id"), organizationId
        )
    }

    override fun toString(): String {
        return "activity.projectRole.project.organization.id==$organizationId"
    }
}

class ActivityStartDateLessOrEqualSpecification(private val startDate: LocalDate) : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.lessThanOrEqualTo(root.get("start"), startDate.atTime(23, 59, 59))
    }

    override fun toString(): String {
        return "activity.start<=$startDate"
    }

}

class ActivityEndDateGreaterOrEqualSpecification(private val endDate: LocalDate) : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.greaterThanOrEqualTo(root.get("end"), endDate.atStartOfDay())
    }

    override fun toString(): String {
        return "activity.end>=$endDate"
    }
}

class ActivityUserIdSpecification(private val userId: Long) : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.equal(root.get<Long>("userId"), userId)
    }

    override fun toString(): String {
        return "activity.userId==$userId"
    }

}