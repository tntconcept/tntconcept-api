package com.autentia.tnt.binnacle.repositories.predicates

import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.*
import io.micronaut.data.jpa.repository.criteria.Specification
import java.time.LocalDate
import java.time.LocalTime
import javax.persistence.criteria.*

internal object ActivityPredicates {

    internal val ALL = EmptySpecification<Activity>()
    internal fun id(id: Long) = ActivityIdSpecification(id)
    internal fun approvalState(approvalState: ApprovalState): Specification<Activity> =
        ActivityApprovalStateSpecification(approvalState)

    internal fun roleId(roleId: Long) = ActivityRoleIdSpecification(roleId)
    internal fun startDateLessThanOrEqualTo(date: LocalDate) = ActivityStartDateLessOrEqualSpecification(date)

    internal fun startDateBetweenDates(dateInterval: DateInterval) = ActivityStartDateBetweenSpecification(dateInterval)

    internal fun endDateGreaterThanOrEqualTo(date: LocalDate) = ActivityEndDateGreaterOrEqualSpecification(date)
    internal fun projectId(projectId: Long) = ActivityProjectIdSpecification(projectId)
    internal fun projectIds(projectIds: List<Long>) = ActivityProjectIdsSpecification(projectIds)

    internal fun organizationId(organizationId: Long) = ActivityOrganizationIdSpecification(organizationId)
    internal fun organizationIds(organizationIds: List<Long>) = ActivityOrganizationIdsSpecification(organizationIds)

    internal fun userId(userId: Long) = ActivityUserIdSpecification(userId)

    internal fun missingEvidenceWeekly() = ActivityMissingEvidenceWeeklySpecification()

    internal fun missingEvidenceOnce() = ActivityMissingEvidenceOnceSpecification()

    internal fun belongsToUsers(userIds: List<Long>) = ActivityBelongsToUsers(userIds)
}

class ActivityBelongsToUsers(private val userIds: List<Long>) : Specification<Activity> {

    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
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

class ActivityMissingEvidenceOnceSpecification : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return getActivitiesWithoutOnce(root, criteriaBuilder)
    }

    private fun getActivitiesWithoutOnce(
        root: Root<Activity>,
        criteriaBuilder: CriteriaBuilder,
    ): Predicate? = criteriaBuilder.and(
        requiresEvidenceType(root, criteriaBuilder, RequireEvidence.ONCE),
        criteriaBuilder.not(hasEvidenceCondition(root, criteriaBuilder)) ,
        criteriaBuilder.not(requiresApprovalState(root, criteriaBuilder, ApprovalState.ACCEPTED))
    )

    private fun requiresApprovalState(
            root: Root<Activity>,
            criteriaBuilder: CriteriaBuilder,
            approvalState: ApprovalState
    ): Predicate? = criteriaBuilder.equal(
            root.get<ApprovalState>("approvalState"),
            approvalState
    )

    private fun requiresEvidenceType(
        root: Root<Activity>,
        criteriaBuilder: CriteriaBuilder,
        requireEvidence: RequireEvidence
    ): Predicate? = criteriaBuilder.equal(
        root.get<ProjectRole>("projectRole").get<RequireEvidence>("requireEvidence"),
        requireEvidence
    )

    private fun hasEvidenceCondition(
        root: Root<Activity>,
        criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return criteriaBuilder.or(
            criteriaBuilder.isNotEmpty(root.get("evidences")),
            criteriaBuilder.like(root.get("description"), "###Autocreated evidence###%")
        )
    }
}

class ActivityStartDateBetweenSpecification(private val dateInterval: DateInterval) : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.between(
            root.get("start"),
            criteriaBuilder.literal(dateInterval.start.atStartOfDay()),
            criteriaBuilder.literal(dateInterval.end.atTime(LocalTime.MAX))
        )
    }

}

class ActivityMissingEvidenceWeeklySpecification : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return getActivitiesWithoutWeekly(criteriaBuilder, root, query)
    }

    private fun getActivitiesWithoutWeekly(
        criteriaBuilder: CriteriaBuilder,
        root: Root<Activity>,
        query: CriteriaQuery<*>
    ): Predicate? {
        return criteriaBuilder.and(
            requiresEvidenceType(root, criteriaBuilder, RequireEvidence.WEEKLY),
            notExistsActivityWithEvidenceInWeekPeriod(root, query, criteriaBuilder)
        )
    }

    private fun notExistsActivityWithEvidenceInWeekPeriod(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        val subQuery = query.subquery(Activity::class.java)
        val subQueryRoot = subQuery.from(Activity::class.java)
        subQuery.select(subQueryRoot)

        subQuery.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get<Long>("userId"), subQueryRoot.get<User>("userId")),
                criteriaBuilder.equal(
                    root.get<ProjectRole>("projectRole").get<Long>("id"),
                    subQueryRoot.get<ProjectRole>("projectRole").get<Long>("id")
                ),
                getHasEvidenceCondition(subQueryRoot, criteriaBuilder),
                getActivitiesInDateRange(criteriaBuilder, subQueryRoot, root)
            )
        )
        return criteriaBuilder.not(
            criteriaBuilder.exists(
                subQuery
            )
        )
    }

    private fun getActivitiesInDateRange(
        criteriaBuilder: CriteriaBuilder,
        subQueryRoot: Root<Activity>,
        root: Root<Activity>
    ): Predicate? {
        val startBetween: Expression<LocalDate> = criteriaBuilder.function(
            "SUBDATE",
            LocalDate::class.java,
            subQueryRoot.get<LocalDate>("start"),
            criteriaBuilder.literal(7)
        )
        val endBetween: Expression<LocalDate> = criteriaBuilder.function(
            "ADDDATE",
            LocalDate::class.java,
            subQueryRoot.get<LocalDate>("start"),
            criteriaBuilder.literal(7)
        )
        return criteriaBuilder.between(root.get("start"), startBetween, endBetween)
    }

    private fun requiresEvidenceType(
        root: Root<Activity>,
        criteriaBuilder: CriteriaBuilder,
        requireEvidence: RequireEvidence
    ): Predicate? = criteriaBuilder.equal(
        root.get<ProjectRole>("projectRole").get<RequireEvidence>("requireEvidence"),
        requireEvidence
    )

    private fun getHasEvidenceCondition(
        root: Root<Activity>,
        criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return criteriaBuilder.or(
            criteriaBuilder.isNotEmpty(root.get("evidences")),
            criteriaBuilder.like(root.get("description"), "###Autocreated evidence###%")
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ActivityMissingEvidenceWeeklySpecification
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityIdSpecification) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

class ActivityApprovalStateSpecification(private val approvalState: ApprovalState) : Specification<Activity> {
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

class ActivityProjectIdsSpecification(private val projectIds: List<Long>) : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return root.join<Activity, ProjectRole>("projectRole", JoinType.INNER)
                .join<ProjectRole, Project>("project", JoinType.INNER)
                .get<Long>("id").`in`(projectIds)
    }

    override fun toString(): String {
        return "activity.projectRole.project.ids IN $projectIds"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityProjectIdsSpecification) return false

        return projectIds == other.projectIds
    }

    override fun hashCode(): Int {
        return projectIds.hashCode()
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

class ActivityOrganizationIdsSpecification(private val organizationIds: List<Long>) : Specification<Activity> {
    override fun toPredicate(
        root: Root<Activity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return root.join<Activity, ProjectRole>("projectRole", JoinType.INNER)
                .join<ProjectRole, Project>("project", JoinType.INNER).join<Project, Organization>("organization")
                .get<Long>("id").`in`(organizationIds)
    }

    override fun toString(): String {
        return "activity.projectRole.project.organization.id IN ($organizationIds)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityOrganizationIdsSpecification) return false

        return organizationIds == other.organizationIds
    }

    override fun hashCode(): Int {
        return organizationIds.hashCode()
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