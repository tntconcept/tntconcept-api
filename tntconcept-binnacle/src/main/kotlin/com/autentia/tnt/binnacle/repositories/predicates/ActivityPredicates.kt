package com.autentia.tnt.binnacle.repositories.predicates

import com.autentia.tnt.binnacle.entities.*
import io.micronaut.data.jpa.repository.criteria.Specification
import java.time.LocalDate
import javax.persistence.criteria.*

internal object ActivityPredicates {

    internal val ALL = EmptySpecification<Activity>()
    internal fun id(id: Long) = ActivityIdSpecification(id)
    internal fun approvalState(approvalState: ApprovalState): Specification<Activity> = ActivityApprovalStateSpecification(approvalState)

    internal fun roleId(roleId: Long) = ActivityRoleIdSpecification(roleId)
    internal fun startDateLessThanOrEqualTo(date: LocalDate) = ActivityStartDateLessOrEqualSpecification(date)
    internal fun endDateGreaterThanOrEqualTo(date: LocalDate) = ActivityEndDateGreaterOrEqualSpecification(date)
    internal fun projectId(projectId: Long) = ActivityProjectIdSpecification(projectId)
    internal fun organizationId(organizationId: Long) = ActivityOrganizationIdSpecification(organizationId)
    internal fun userId(userId: Long) = ActivityUserIdSpecification(userId)
    internal fun projectRoleRequiresEvidence(requireEvidence: RequireEvidence) =
            ActivityProjectRoleRequiresEvidenceSpecification(requireEvidence)

    internal fun hasNotEvidence() = ActivityHasNotEvidenceSpecification()

    internal fun belongsToUsers(userIds: List<Long>) = ActivityBelongsToUsers(userIds)
}

class ActivityBelongsToUsers(private val userIds: List<Long>) : Specification<Activity> {

    override fun toPredicate(root: Root<Activity>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate? {
        return root.get<Long>("userId").`in`(userIds)
    }

    override fun toString(): String {
        return "activity.userId IN $userIds"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityBelongsToUsers) return false

        return userIds == other.userIds
    }

    override fun hashCode(): Int {
        return userIds.hashCode()
    }

}

class ActivityIdSpecification(private val id: Long) : Specification<Activity> {
    override fun toPredicate(
            root: Root<Activity>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder,
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
            criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return criteriaBuilder.equal(root.get<ApprovalState>("approvalState"), approvalState)
    }

    override fun toString(): String {
        return "activity.approvalState==$approvalState"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityApprovalStateSpecification) return false

        return approvalState == other.approvalState
    }

    override fun hashCode(): Int {
        return approvalState.hashCode()
    }


}

class ActivityRoleIdSpecification(private val roleId: Long) : Specification<Activity> {
    override fun toPredicate(
            root: Root<Activity>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return criteriaBuilder.equal(root.get<ProjectRole>("projectRole").get<Long>("id"), roleId)
    }

    override fun toString(): String {
        return "activity.projectRole.id==$roleId"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityRoleIdSpecification) return false

        return roleId == other.roleId
    }

    override fun hashCode(): Int {
        return roleId.hashCode()
    }


}

class ActivityProjectIdSpecification(private val projectId: Long) : Specification<Activity> {
    override fun toPredicate(
            root: Root<Activity>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return criteriaBuilder.equal(
                root.join<Activity, ProjectRole>("projectRole", JoinType.INNER)
                        .join<ProjectRole, Project>("project", JoinType.INNER).get<Long>("id"), projectId
        )
    }

    override fun toString(): String {
        return "activity.projectRole.project.id==$projectId"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityProjectIdSpecification) return false

        return projectId == other.projectId
    }

    override fun hashCode(): Int {
        return projectId.hashCode()
    }
}

class ActivityOrganizationIdSpecification(private val organizationId: Long) : Specification<Activity> {
    override fun toPredicate(
            root: Root<Activity>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder,
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityOrganizationIdSpecification) return false

        return organizationId == other.organizationId
    }

    override fun hashCode(): Int {
        return organizationId.hashCode()
    }
}

class ActivityStartDateLessOrEqualSpecification(private val startDate: LocalDate) : Specification<Activity> {
    override fun toPredicate(
            root: Root<Activity>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return criteriaBuilder.lessThanOrEqualTo(root.get("start"), startDate.atTime(23, 59, 59))
    }

    override fun toString(): String {
        return "activity.start<=$startDate"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityStartDateLessOrEqualSpecification) return false

        return startDate == other.startDate
    }

    override fun hashCode(): Int {
        return startDate.hashCode()
    }


}

class ActivityEndDateGreaterOrEqualSpecification(private val endDate: LocalDate) : Specification<Activity> {
    override fun toPredicate(
            root: Root<Activity>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return criteriaBuilder.greaterThanOrEqualTo(root.get("end"), endDate.atStartOfDay())
    }

    override fun toString(): String {
        return "activity.end>=$endDate"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityEndDateGreaterOrEqualSpecification) return false

        return endDate == other.endDate
    }

    override fun hashCode(): Int {
        return endDate.hashCode()
    }

}

class ActivityUserIdSpecification(private val userId: Long) : Specification<Activity> {
    override fun toPredicate(
            root: Root<Activity>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return criteriaBuilder.equal(root.get<Long>("userId"), userId)
    }

    override fun toString(): String {
        return "activity.userId==$userId"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityUserIdSpecification) return false

        return userId == other.userId
    }

    override fun hashCode(): Int {
        return userId.hashCode()
    }
}

class ActivityProjectRoleRequiresEvidenceSpecification(private val requireEvidence: RequireEvidence) :
        Specification<Activity> {
    override fun toPredicate(
            root: Root<Activity>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return criteriaBuilder.equal(
                root.get<ProjectRole>("projectRole").get<RequireEvidence>("requireEvidence"),
                requireEvidence
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityProjectRoleRequiresEvidenceSpecification) return false

        return requireEvidence == other.requireEvidence
    }

    override fun hashCode(): Int {
        return requireEvidence.hashCode()
    }

    override fun toString(): String {
        return "activity.projectRole.requireEvidence=${requireEvidence}"
    }
}


class ActivityHasNotEvidenceSpecification : Specification<Activity> {
    override fun toPredicate(
            root: Root<Activity>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return criteriaBuilder.isFalse(root.get("hasEvidences"))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ActivityHasNotEvidenceSpecification
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String {
        return "activity.hasEvidence"
    }

}