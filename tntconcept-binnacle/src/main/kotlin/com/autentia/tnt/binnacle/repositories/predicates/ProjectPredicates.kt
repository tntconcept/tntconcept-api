package com.autentia.tnt.binnacle.repositories.predicates

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import io.micronaut.data.jpa.repository.criteria.Specification
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

internal object ProjectPredicates {
    internal fun organizationId(organizationId: Long) = ProjectOrganizationIdSpecification(organizationId)
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