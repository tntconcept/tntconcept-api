package com.autentia.tnt.binnacle.repositories.predicates

import io.micronaut.data.jpa.repository.criteria.Specification
import jakarta.inject.Singleton
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Singleton
internal class PredicateBuilder<T> {
    fun where(specification: Specification<T>): Specification<T> {
        return SpecificationComposition(specification, "$specification")
    }

    fun and(spec1: Specification<T>, spec2: Specification<T>): Specification<T> {
        return when {
            spec1 is EmptySpecification -> where(spec2)
            spec2 is EmptySpecification -> where(spec1)
            else -> SpecificationComposition(spec1.and(spec2), "($spec1&$spec2)")
        }
    }

    fun or(spec1: Specification<T>, spec2: Specification<T>): Specification<T> {
        return when {
            spec1 is EmptySpecification -> where(spec2)
            spec2 is EmptySpecification -> where(spec1)
            else -> SpecificationComposition(spec1.or(spec2), "($spec1||$spec2)")
        }
    }

    private class SpecificationComposition<T>(
        private val specification: Specification<T>,
        private val descriptor: String
    ) :
        Specification<T> {
        override fun toPredicate(
            root: Root<T>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder
        ): Predicate? {
            return specification.toPredicate(root, query, criteriaBuilder)
        }

        override fun toString(): String {
            return descriptor;
        }


        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SpecificationComposition<*>) return false
            return descriptor == other.descriptor
        }

        override fun hashCode(): Int {
            return descriptor.hashCode()
        }

    }
}

class EmptySpecification<T> : Specification<T> {
    override fun toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        return null
    }

    override fun toString(): String {
        return ""
    }
}


