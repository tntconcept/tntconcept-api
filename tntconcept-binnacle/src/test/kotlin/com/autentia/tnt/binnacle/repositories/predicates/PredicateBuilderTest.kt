package com.autentia.tnt.binnacle.repositories.predicates

import com.autentia.tnt.binnacle.entities.Activity
import io.micronaut.data.jpa.repository.criteria.Specification
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class PredicateBuilderTest {
    private val predicateBuilder = PredicateBuilder<Activity>()

    @Test
    fun `test and`() {
        val specification1 = object : Specification<Activity> {
            override fun toPredicate(
                root: Root<Activity>,
                query: CriteriaQuery<*>,
                criteriaBuilder: CriteriaBuilder
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
                criteriaBuilder: CriteriaBuilder
            ): Predicate? {
                return null
            }

            override fun toString(): String {
                return "mySpecificationExpression2"
            }
        }
        val spySpecification1 = spy(specification1)
        val predicate = predicateBuilder.and(spySpecification1, specification2)

        verify(spySpecification1).and(specification2)
        assertEquals("(mySpecificationExpression1&mySpecificationExpression2)", predicate.toString())
    }

    @Test
    fun `test and when one is empty`() {
        val specification1 = object : Specification<Activity> {
            override fun toPredicate(
                root: Root<Activity>,
                query: CriteriaQuery<*>,
                criteriaBuilder: CriteriaBuilder
            ): Predicate? {
                return null
            }

            override fun toString(): String {
                return "mySpecificationExpression1"
            }
        }
        val specification2 = EmptySpecification<Activity>()

        val predicate = predicateBuilder.and(specification1, specification2)
        val reversePredicate = predicateBuilder.and(specification2, specification1)

        assertEquals("mySpecificationExpression1", predicate.toString())
        assertEquals("mySpecificationExpression1", reversePredicate.toString())
    }

    @Test
    fun `test or`() {
        val specification1 = object : Specification<Activity> {
            override fun toPredicate(
                root: Root<Activity>,
                query: CriteriaQuery<*>,
                criteriaBuilder: CriteriaBuilder
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
                criteriaBuilder: CriteriaBuilder
            ): Predicate? {
                return null
            }

            override fun toString(): String {
                return "mySpecificationExpression2"
            }
        }

        val spySpecification1 = spy(specification1)
        val predicate = predicateBuilder.or(spySpecification1, specification2)

        verify(spySpecification1).or(specification2)
        assertEquals("(mySpecificationExpression1||mySpecificationExpression2)", predicate.toString())
    }

    @Test
    fun `test or when one is empty`() {
        val specification1 = object : Specification<Activity> {
            override fun toPredicate(
                root: Root<Activity>,
                query: CriteriaQuery<*>,
                criteriaBuilder: CriteriaBuilder
            ): Predicate? {
                return null
            }

            override fun toString(): String {
                return "mySpecificationExpression1"
            }
        }
        val specification2 = EmptySpecification<Activity>()

        val predicate = predicateBuilder.or(specification1, specification2)
        val reversePredicate = predicateBuilder.or(specification2, specification1)

        assertEquals("mySpecificationExpression1", predicate.toString())
        assertEquals("mySpecificationExpression1", reversePredicate.toString())
    }
}