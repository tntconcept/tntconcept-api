package com.autentia.tnt.binnacle.repositories.predicates

import io.micronaut.data.jpa.repository.criteria.Specification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class PredicateBuilderTest {

    @Test
    fun `Predicate can be composed of two AND clauses`() {
        val predicate = PredicateBuilder.and(LessThan(5), GreaterThan(2));
        val reversePredicate = PredicateBuilder.and(GreaterThan(2), LessThan(5))

        assertThat(predicate).isEqualTo(reversePredicate)
        assertThat(predicate.toString()).isEqualTo("(LessThan(5) AND GreaterThan(2))")
        assertThat(reversePredicate.toString()).isEqualTo("(GreaterThan(2) AND LessThan(5))");
    }

    @Test
    fun `Predicate can be composed of more than two AND clauses`() {
        val predicate = PredicateBuilder.and(LessThan(5), PredicateBuilder.and(GreaterThan(2), NotEqual(3)))
        val reversePredicate = PredicateBuilder.and(PredicateBuilder.and(GreaterThan(2), NotEqual(3)), LessThan(5))

        assertThat(predicate).isEqualTo(reversePredicate)
        assertThat(predicate.toString()).isEqualTo("(LessThan(5) AND (GreaterThan(2) AND NotEqual(3)))")
        assertThat(reversePredicate.toString()).isEqualTo("((GreaterThan(2) AND NotEqual(3)) AND LessThan(5))")
    }

    @Test
    fun `Predicate can be composed of one AND predicate another empty predicate`() {
        val specification2 = EmptySpecification<DummyClass>()

        val predicate = PredicateBuilder.and(GreaterThan(2), specification2)
        val reversePredicate = PredicateBuilder.and(specification2, GreaterThan(2))

        assertThat(predicate.toString()).isEqualTo("GreaterThan(2)")
        assertThat(reversePredicate.toString()).isEqualTo("GreaterThan(2)")
    }

    @Test
    fun `Predicate can be composed of two OR clauses`() {
        val predicate = PredicateBuilder.or(GreaterThan(2), NotEqual(3))
        val reversePredicate = PredicateBuilder.or(NotEqual(3), GreaterThan(2))
        assertThat(predicate).isEqualTo(reversePredicate)
        assertThat(predicate.toString()).isEqualTo("(GreaterThan(2) OR NotEqual(3))")
    }

    @Test
    fun `Predicate can be composed of more than two OR clauses`() {
        val predicate = PredicateBuilder.or(LessThan(5), PredicateBuilder.or(GreaterThan(2), NotEqual(3)))
        val reversePredicate = PredicateBuilder.or(PredicateBuilder.or(GreaterThan(2), NotEqual(3)), LessThan(5))

        assertThat(predicate).isEqualTo(reversePredicate)
        assertThat(predicate.toString()).isEqualTo("(LessThan(5) OR (GreaterThan(2) OR NotEqual(3)))")
        assertThat(reversePredicate.toString()).isEqualTo("((GreaterThan(2) OR NotEqual(3)) OR LessThan(5))")
    }

    @Test
    fun `Predicate can be composed of one OR predicate another empty predicate`() {
        val predicate = PredicateBuilder.or(GreaterThan(2), EmptySpecification())
        val reversePredicate = PredicateBuilder.or(EmptySpecification(), GreaterThan(2))

        assertThat(predicate.toString()).isEqualTo("GreaterThan(2)").isEqualTo(reversePredicate.toString())
    }

    @Test
    fun `Predicate can be composed mixing OR and AND predicates`() {
        val greaterEqualThanAndLessEqualThanPredicate = PredicateBuilder.and(
                PredicateBuilder.or(GreaterThan(2), Equal(2)),
                PredicateBuilder.or(LessThan(10), Equal(10))
        )

        val greaterEqualThanAndLessEqualThanPredicateReversed = PredicateBuilder.and(
                PredicateBuilder.or(LessThan(10), Equal(10)),
                PredicateBuilder.or(GreaterThan(2), Equal(2))
        )

        assertThat(greaterEqualThanAndLessEqualThanPredicate).isEqualTo(greaterEqualThanAndLessEqualThanPredicateReversed)
        assertThat(greaterEqualThanAndLessEqualThanPredicate.toString())
                .isEqualTo("((GreaterThan(2) OR Equal(2)) AND (LessThan(10) OR Equal(10)))")
    }
}

private class DummyClass(private val value: Long)

private class GreaterThan(private val value: Long) : Specification<DummyClass> {

    override fun toPredicate(root: Root<DummyClass>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate? {
        return criteriaBuilder.greaterThan(root.get("value"), value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GreaterThan

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "GreaterThan($value)"
    }
}

private class LessThan(private val value: Long) : Specification<DummyClass> {

    override fun toPredicate(root: Root<DummyClass>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate? {
        return criteriaBuilder.lessThan(root.get("value"), value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LessThan

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "LessThan($value)"
    }
}

private class Equal(private val value: Long) : Specification<DummyClass> {

    override fun toPredicate(root: Root<DummyClass>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate? {
        return criteriaBuilder.equal(root.get<Long>("value"), value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Equal

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "Equal($value)"
    }
}

private class NotEqual(private val value: Long) : Specification<DummyClass> {

    override fun toPredicate(root: Root<DummyClass>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate? {
        return criteriaBuilder.notEqual(root.get<Long>("value"), value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotEqual

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "NotEqual($value)"
    }
}

