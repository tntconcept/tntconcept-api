package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AnnualWorkSummary
import com.autentia.tnt.binnacle.entities.AnnualWorkSummaryId
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.math.BigDecimal

@MicronautTest
@TestInstance(PER_CLASS)
internal class AnnualWorkSummaryRepositoryIT {

    @Inject
    private lateinit var annualWorkSummaryRepository: AnnualWorkSummaryRepository

    @Test
    fun `should find the summary by id`() {
        val idToSearch = AnnualWorkSummaryId(1L, 2021)
        val annualWorkSummary = AnnualWorkSummary(idToSearch, BigDecimal("0.00"), BigDecimal("0.00"))

        val result = annualWorkSummaryRepository.findById(idToSearch)

        assertEquals(result.get(), annualWorkSummary)
    }

    @Test
    fun `should update an existing summary`() {
        val idToUpdate = AnnualWorkSummaryId(1L, 2021)

        val updated = annualWorkSummaryRepository.saveOrUpdate(
            AnnualWorkSummary(
                idToUpdate,
                BigDecimal("1770.0"),
                BigDecimal("1765.0")
            )
        )

        val found = annualWorkSummaryRepository.findById(idToUpdate)

        assertEquals(updated, found.get())
    }

    @Test
    fun `should create a summary`() {
        val idToCreate = AnnualWorkSummaryId(1L, 2022)

        val created = annualWorkSummaryRepository.saveOrUpdate(
            AnnualWorkSummary(
                idToCreate,
                BigDecimal("1770.0"),
                BigDecimal("1765.0")
            )
        )

        val found = annualWorkSummaryRepository.findById(idToCreate)

        assertEquals(created, found.get())
    }
}

