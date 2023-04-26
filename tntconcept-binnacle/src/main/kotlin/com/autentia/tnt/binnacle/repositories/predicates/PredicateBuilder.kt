package com.autentia.tnt.binnacle.repositories.predicates

import io.micronaut.data.jpa.repository.criteria.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

internal object PredicateBuilder {

    fun <T> and(spec1: Specification<T>, spec2: Specification<T>): Specification<T> {
        return when {
            spec1 !is EmptySpecification && spec2 !is EmptySpecification -> SpecificationComposition(
                spec1, spec2, SpecificationComposition.Operator.AND
            )

            spec1 !is EmptySpecification -> spec1
            spec2 !is EmptySpecification -> spec2
            else -> EmptySpecification()
        }
    }

    fun <T> or(spec1: Specification<T>, spec2: Specification<T>): Specification<T> {
        return when {
            spec1 !is EmptySpecification && spec2 !is EmptySpecification -> SpecificationComposition(
                spec1, spec2, SpecificationComposition.Operator.OR
            )

            spec1 !is EmptySpecification -> spec1
            spec2 !is EmptySpecification -> spec2
            else -> EmptySpecification()
        }
    }

    class SpecificationComposition<T>(
        private val lhs: Specification<T>,
        private val rhs: Specification<T>,
        private val operator: Operator,
    ) :
        Specification<T> {

        enum class Operator {
            AND, OR
        }

        override fun toPredicate(
            root: Root<T>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder,
        ): Predicate? {
            val predicate = when (operator) {
                Operator.OR -> lhs.or(rhs)
                Operator.AND -> lhs.and(rhs)
            }

            return predicate.toPredicate(root, query, criteriaBuilder)
        }

        override fun toString(): String {
            return when (operator) {
                Operator.OR -> "($lhs OR $rhs)"
                Operator.AND -> "($lhs AND $rhs)"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SpecificationComposition<*>) return false
            if (operator !== other.operator) return false
            return when (lhs) {
                other.lhs -> rhs == other.rhs
                other.rhs -> rhs == other.lhs
                else -> false
            }
        }

        override fun hashCode(): Int {
            var result = lhs.hashCode()
            result = 31 * result + rhs.hashCode()
            return result
        }
    }
}

class EmptySpecification<T> : Specification<T> {
    override fun toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
    ): Predicate? {
        return null
    }

    override fun toString(): String {
        return "TRUE"
    }
}


