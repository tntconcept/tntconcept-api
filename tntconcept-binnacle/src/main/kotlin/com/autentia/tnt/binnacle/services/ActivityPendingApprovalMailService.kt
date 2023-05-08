package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import io.micronaut.context.MessageSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import java.util.*

@Singleton
internal class ActivityPendingApprovalMailService(
    private val mailService: MailService,
    private val messageSource: MessageSource,
    private val appProperties: AppProperties,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(ActivityPendingApprovalMailService::class.java)
    }

    fun sendActivityEvidenceMail(activity: ActivityResponse, username: String, locale: Locale) {
        if (!appProperties.mail.enabled) {
            logger.info("Mailing of activity evidence is disabled")
            return
        }
        val body = messageSource
            .getMessage(
                "mail.activity.evidence.template",
                locale,
                username,
                activity.start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                activity.end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                activity.description
            )
            .orElse(null) ?: error("Cannot find message mail.activity.evidence.template")

        val subject = messageSource
            .getMessage("mail.activity.evidence.subject", locale, username)
            .orElse(null) ?: error("Cannot find message mail.activity.evidence.subject")

        mailService.send(appProperties.mail.from, appProperties.binnacle.activitiesApprovers, subject, body)
            .onFailure { logger.error("Error sending evidence activity email", it) }
    }
}