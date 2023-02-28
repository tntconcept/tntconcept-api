package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AnnualWorkSummaryId
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@MicronautTest
@TestInstance(PER_CLASS)
internal class AnnualWorkSummaryRepositoryIT {

    @Inject
    private lateinit var annualWorkSummaryRepository: AnnualWorkSummaryRepository

    @Test
    fun `should find the summary by id`() {
        val idToSearch = AnnualWorkSummaryId(1L, 2021)

        val result = annualWorkSummaryRepository.findById(idToSearch)

        assertThat(result.get().annualWorkSummaryId.userId).isEqualTo(idToSearch.userId)
        assertThat(result.get().annualWorkSummaryId.year).isEqualTo(idToSearch.year)
    }

}
