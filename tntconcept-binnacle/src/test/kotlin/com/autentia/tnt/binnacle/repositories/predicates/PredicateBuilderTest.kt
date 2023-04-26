package com.autentia.tnt.binnacle.repositories.predicates

import com.autentia.tnt.binnacle.entities.Activity
import io.micronaut.data.jpa.repository.criteria.Specification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class PredicateBuilderTest {

    @Test
    fun `test and`() {
        val specification1 = object : Specification<Activity> {
            override fun toPredicate(
                root: Root<Activity>,
                query: CriteriaQuery<*>,
                criteriaBuilder: CriteriaBuilder,
            ): Predicate? {
                return null
            }

            override fun toString(): String {
                return "mySpecificationExpression1"
            }
        }
        val specification2 = object : Specification<Activity> {
            override fun toPredicate(
                root: Root<Activity>,
                query: CriteriaQuery<*>,
                criteriaBuilder: CriteriaBuilder,
            ): Predicate? {
                return null
            }

            override fun toString(): String {
                return "mySpecificationExpression2"
            }
        }
        val predicate = PredicateBuilder.and(specification1, specification2)
        val reversePredicate = PredicateBuilder.and(specification2, specification1)

        assertEquals(predicate, reversePredicate)
        assertEquals("(mySpecificationExpression1 AND mySpecificationExpression2)", predicate.toString())
    }

    @Test
    fun `test and when one is empty`() {
        val specification1 = object : Specification<Activity> {
            override fun toPredicate(
                root: Root<Activity>,
                query: CriteriaQuery<*>,
                criteriaBuilder: CriteriaBuilder,
            ): Predicate? {
                return null
            }

            override fun toString(): String {
                return "mySpecificationExpression1"
            }
        }
        val specification2 = EmptySpecification<Activity>()

        val predicate = PredicateBuilder.and(specification1, specification2)
        val reversePredicate = PredicateBuilder.and(specification2, specification1)

        assertEquals("mySpecificationExpression1", predicate.toString())
        assertEquals("mySpecificationExpression1", reversePredicate.toString())
    }

    @Test
    fun `test or`() {
        val specification1 = object : Specification<Activity> {
            override fun toPredicate(
                root: Root<Activity>,
                query: CriteriaQuery<*>,
                criteriaBuilder: CriteriaBuilder,
            ): Predicate? {
                return null
            }

            override fun toString(): String {
                return "mySpecificationExpression1"
            }
        }
        val specification2 = object : Specification<Activity> {
            override fun toPredicate(
                root: Root<Activity>,
                query: CriteriaQuery<*>,
                criteriaBuilder: CriteriaBuilder,
            ): Predicate? {
                return null
            }

            override fun toString(): String {
                return "mySpecificationExpression2"
            }
        }

        val predicate = PredicateBuilder.or(specification1, specification2)
        val reversePredicate = PredicateBuilder.or(specification2, specification1)

        assertEquals(predicate, reversePredicate)
        assertEquals("(mySpecificationExpression1 OR mySpecificationExpression2)", predicate.toString())
    }

    @Test
    fun `test or when one is empty`() {
        val specification1 = object : Specification<Activity> {
            override fun toPredicate(
                root: Root<Activity>,
                query: CriteriaQuery<*>,
                criteriaBuilder: CriteriaBuilder,
            ): Predicate? {
                return null
            }

            override fun toString(): String {
                return "mySpecificationExpression1"
            }
        }
        val specification2 = EmptySpecification<Activity>()

        val predicate = PredicateBuilder.or(specification1, specification2)
        val reversePredicate = PredicateBuilder.or(specification2, specification1)

        assertEquals("mySpecificationExpression1", predicate.toString())
        assertEquals("mySpecificationExpression1", reversePredicate.toString())
    }
}