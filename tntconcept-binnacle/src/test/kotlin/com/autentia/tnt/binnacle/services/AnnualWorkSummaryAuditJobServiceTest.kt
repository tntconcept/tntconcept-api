package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.entities.AnnualWorkSummaryAuditJob
import com.autentia.tnt.binnacle.repositories.AnnualWorkSummaryAuditJobRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

internal class AnnualWorkSummaryAuditJobServiceTest {

    private val annualWorkSummaryAuditJobRepository = mock<AnnualWorkSummaryAuditJobRepository>()
    private val annualWorkSummaryAuditJobService = AnnualWorkSummaryAuditJobService(annualWorkSummaryAuditJobRepository)

    @Test
    fun `create audit job with started and finished time`() {
        val startedTime = LocalDateTime.now()
        val elapsedNanos = TimeUnit.MINUTES.toNanos(2L)
        val finished = startedTime.plusNanos(elapsedNanos)
        val expectedAuditJob = AnnualWorkSummaryAuditJob(id = 1L, startedTime, finished)

        doReturn(expectedAuditJob).whenever(annualWorkSummaryAuditJobRepository).save(any<AnnualWorkSummaryAuditJob>())

        val result = annualWorkSummaryAuditJobService.createAuditJob(startedTime, elapsedNanos)

        assertEquals(expectedAuditJob, result)
    }

}
