package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.services.AnnualWorkSummaryAuditJobService
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.system.measureNanoTime

@Singleton
class AnnualWorkSummaryJobUseCase internal constructor(
    private val createAnnualSummaryReportUseCase: CreateAnnualSummaryReportUseCase,
    private val annualWorkSummaryAuditJobService: AnnualWorkSummaryAuditJobService,
) {

    fun createWorkSummariesYearBefore() {
        val started = LocalDateTime.now()
        val elapsedNanos = measureNanoTime {
            try {
                createAnnualSummaryReportUseCase.createAnnualSummaryFromYear(started.year - 1)
            } catch (e: Exception) {
                logger.error("ERROR executing work summary job", e)
            }
        }
        annualWorkSummaryAuditJobService.createAuditJob(started, elapsedNanos)
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(AnnualWorkSummaryJobUseCase::class.java)
    }
}

