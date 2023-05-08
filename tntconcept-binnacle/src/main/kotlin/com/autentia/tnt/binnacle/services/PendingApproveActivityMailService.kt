package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import io.micronaut.context.MessageSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import java.util.*

@Singleton
internal class PendingApproveActivityMailService(
    private val mailService: MailService,
    private val messageSource: MessageSource,
    private val appProperties: AppProperties
) {
    fun sendApprovalActivityMail(activity: ActivityResponse, username: String, locale: Locale){
        if (!appProperties.mail.enabled) {
            logger.info("Mailing of approval activities is disabled")
            return
        }
        val body = messageSource
            .getMessage(
                "mail.request.pendingApproveActivity.template",
                locale,
                username,
                activity.start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                activity.end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                activity.description
            )
            .orElse(null) ?: error("Cannot find message mail.request.pendingApproveActivity.template")

        val subject = messageSource
            .getMessage("mail.request.pendingApproveActivity.subject", locale, username)
            .orElse(null) ?: error("Cannot find message mail.request.pendingApproveActivity.subject")

        mailService.send(appProperties.mail.from, appProperties.binnacle.activitiesApprovers, subject, body)
            .onFailure { logger.error("Error sending activity approve email", it) }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(PendingApproveActivityMailService::class.java)
    }
}