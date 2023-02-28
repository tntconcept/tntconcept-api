package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.io.File

//TODO properties_es
internal const val SUBJECT = "[BINNACLE ANNUAL REPORT]"
internal const val BODY_TEXT = "Please, review the attached report"

@Singleton
internal class WorkSummaryMailService(
    private val mailService: MailService,
    private val appProperties: AppProperties,
) {
    fun sendReport(report: File) {
        if (!appProperties.binnacle.workSummary.mail.enabled) {
            logger.info("Mailing of work summary is disabled")
            return
        }

        mailService.send(
            appProperties.mail.from,
            appProperties.binnacle.workSummary.mail.to,
            SUBJECT,
            BODY_TEXT,
            report
        ).onFailure { logger.error("Error sending work summary email", it) }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(WorkSummaryMailService::class.java)
    }

}
