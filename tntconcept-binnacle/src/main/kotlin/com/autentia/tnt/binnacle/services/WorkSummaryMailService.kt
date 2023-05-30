package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Mail
import io.micronaut.context.MessageSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

@Singleton
internal class WorkSummaryMailService(
    private val mailService: MailService,
    private val workSummaryMailBuilder: WorkSummaryMailBuilder,
    private val appProperties: AppProperties,
) {
    fun sendReport(report: File) {
        if (!appProperties.binnacle.workSummary.mail.enabled) {
            logger.info("Mailing of work summary is disabled")
            return
        }

        val emailToSend: Mail = workSummaryMailBuilder.buildMail()

        mailService.send(
            appProperties.mail.from,
            appProperties.binnacle.workSummary.mail.to,
            emailToSend.subject,
            emailToSend.body,
            report
        ).onFailure { logger.error("Error sending work summary email", it) }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(WorkSummaryMailService::class.java)
    }

}

@Singleton
internal class WorkSummaryMailBuilder(private val messageSource: MessageSource) {
    private companion object {
        private const val subjectKey = "mail.request.workSummary.subject"
        private const val bodyKey = "mail.request.workSummary.template"
    }

    fun buildMail(): Mail {
        val locale = Locale.ENGLISH
        val subject = messageSource.getMessage(subjectKey, locale)
            .orElseThrow { IllegalStateException("Missing subject configuration ") }
        val body = messageSource.getMessage(bodyKey, locale)
            .orElseThrow { java.lang.IllegalStateException("Missing body configuration") }
        return Mail(subject, body)
    }

}
