package com.autentia.tnt.binnacle.repositories.predicates

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import io.micronaut.data.jpa.repository.criteria.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

internal object ProjectPredicates {
    internal fun organizationId(organizationId: Long) = ProjectOrganizationIdSpecification(organizationId)
    internal fun organizationIds(organizationIds: List<Long>) = ProjectOrganizationIdsSpecification(organizationIds)
    internal fun open(open: Boolean) = ProjectOpenSpecification(open)
}

class ProjectOrganizationIdSpecification(private val organizationId: Long) : Specification<Project> {
    override fun toPredicate(
        root: Root<Project>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.equal(root.get<Organization>("organization").get<Long>("id"), organizationId)
    }

    override fun toString(): String {
        return "project.organizationId==$organizationId"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProjectOrganizationIdSpecification) return false

        return organizationId == other.organizationId
    }

    override fun hashCode(): Int {
        return organizationId.hashCode()
    }
}

class ProjectOrganizationIdsSpecification(private val organizationIds: List<Long>) : Specification<Project> {
    override fun toPredicate(
        root: Root<Project>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return root.get<Organization>("organization").get<Long>("id").`in`(organizationIds)
    }

    override fun toString(): String {
        return "project.organizationId==$organizationIds"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProjectOrganizationIdsSpecification) return false

        return organizationIds == other.organizationIds
    }

    override fun hashCode(): Int {
        return organizationIds.hashCode()
    }
}

class ProjectOpenSpecification(private val open: Boolean) : Specification<Project> {
    override fun toPredicate(
        root: Root<Project>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.equal(root.get<Boolean>("open"), open)
    }

    override fun toString(): String {
        return "project.open==$open"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProjectOpenSpecification) return false

        return open == other.open
    }

    override fun hashCode(): Int {
        return open.hashCode()
    }
}