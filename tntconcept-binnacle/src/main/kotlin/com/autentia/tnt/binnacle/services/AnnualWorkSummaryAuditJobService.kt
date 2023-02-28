package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.entities.AnnualWorkSummaryAuditJob
import com.autentia.tnt.binnacle.repositories.AnnualWorkSummaryAuditJobRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
@Transactional
@ReadOnly
internal class AnnualWorkSummaryAuditJobService(
    private val annualWorkSummaryAuditJobRepository: AnnualWorkSummaryAuditJobRepository,
) {

    @Transactional(rollbackOn = [Exception::class])
    fun createAuditJob(startedTime: LocalDateTime, elapsedNanos: Long): AnnualWorkSummaryAuditJob {
        val finished = startedTime.plusNanos(elapsedNanos)
        val auditJob = AnnualWorkSummaryAuditJob(id = null, startedTime, finished)
        return annualWorkSummaryAuditJobRepository.save(auditJob)
    }

}
