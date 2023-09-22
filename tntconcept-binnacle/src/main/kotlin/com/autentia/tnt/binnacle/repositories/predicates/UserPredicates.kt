package com.autentia.tnt.binnacle.repositories.predicates

import com.autentia.tnt.binnacle.entities.User
import io.micronaut.data.jpa.repository.criteria.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

internal object UserPredicates {

    internal val ALL = EmptySpecification<User>()

    internal fun isActive(active: Boolean) = UserActiveSpecification(active)

    internal fun fromUserIds(ids: List<Long>) = UserFromUserIdsSpecification(ids)

    internal fun userId(id: Long) = UserIdSpecification(id)

    internal fun filterByName(filter: String) = UserFilterByNameSpecification(filter)
}

class UserFilterByNameSpecification(private val filter: String) : Specification<User> {
    override fun toPredicate(
        root: Root<User>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.like(root.get("name"), "%${filter}%")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserFilterByNameSpecification

        return filter == other.filter
    }

    override fun hashCode(): Int {
        return filter.hashCode()
    }


}

class UserIdSpecification(private val id: Long) : Specification<User> {

    override fun toPredicate(
        root: Root<User>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.equal(root.get<User>("id"), id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserIdSpecification

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}

class UserFromUserIdsSpecification(private val userIds: List<Long>) : Specification<User> {
    override fun toPredicate(
        root: Root<User>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return root.get<User>("id").`in`(userIds)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class UserActiveSpecification(private val active: Boolean) : Specification<User> {
    override fun toPredicate(
        root: Root<User>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return criteriaBuilder.equal(root.get<User>("active"), active)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserActiveSpecification

        return active == other.active
    }

    override fun hashCode(): Int {
        return active.hashCode()
    }


}
