package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import io.micronaut.context.MessageSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import java.util.Locale

@Singleton
internal class ActivityEvidenceMailService(
    private val mailService: MailService,
    private val messageSource: MessageSource,
    private val appProperties: AppProperties
) {
    fun sendActivityEvidenceMail(
        activity: com.autentia.tnt.binnacle.core.domain.Activity,
        username: String,
        locale: Locale
    ) {
        if (!appProperties.mail.enabled) {
            logger.info("Mailing of activity evidence is disabled")
            return
        }
        val body = messageSource
            .getMessage(
                "mail.activity.evidence.template",
                locale,
                username,
                activity.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                activity.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                activity.description
            )
            .orElse(null) ?: error("Cannot find message mail.activity.evidence.template")

        val subject = messageSource
            .getMessage("mail.activity.evidence.subject", locale, username)
            .orElse(null) ?: error("Cannot find message mail.activity.evidence.subject")

        mailService.send(appProperties.mail.from, appProperties.binnacle.activitiesApprovers, subject, body)
            .onFailure { logger.error("Error sending evidence activity email", it) }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(ActivityEvidenceMailService::class.java)
    }
}