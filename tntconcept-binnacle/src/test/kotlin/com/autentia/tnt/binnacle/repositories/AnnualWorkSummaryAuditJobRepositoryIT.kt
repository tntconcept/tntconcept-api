package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AnnualWorkSummaryAuditJob
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.time.LocalDateTime

@MicronautTest
@TestInstance(PER_CLASS)
internal class AnnualWorkSummaryAuditJobRepositoryIT {

    @Inject
    private lateinit var annualWorkSummaryAuditJobRepository: AnnualWorkSummaryAuditJobRepository

    @Test
    fun `should save a new job summary`() {
        val started = LocalDateTime.now().minusMinutes(5)
        val finished = LocalDateTime.now().plusMinutes(10)
        val auditJob = AnnualWorkSummaryAuditJob(id = null, started, finished)

        val result = annualWorkSummaryAuditJobRepository.save(auditJob)

        assertNotNull(result.id)
        assertEquals(started, result.started)
        assertEquals(finished, result.finished)
    }

}
