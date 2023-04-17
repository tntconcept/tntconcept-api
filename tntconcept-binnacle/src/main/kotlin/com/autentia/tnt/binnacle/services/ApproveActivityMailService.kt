package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import io.micronaut.context.MessageSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import java.util.*

@Singleton
internal class ApproveActivityMailService(
    private val mailService: MailService,
    private val messageSource: MessageSource,
    private val appProperties: AppProperties
) {
    fun sendApprovalActivityMail(activity: ActivityResponse, username: String, locale: Locale){
        if (!appProperties.mail.enabled) {
            logger.info("Mailing of vacations is disabled")
            return
        }
        val body = messageSource
            .getMessage(
                "mail.request.approveActivity.template",
                locale,
                username,
                activity.start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                activity.end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                activity.description
            )
            .orElse(null) ?: error("Cannot find message mail.request.approveActivity.template")

        val subject = messageSource
            .getMessage("mail.request.approveActivity.subject", locale, activity.userId)
            .orElse(null) ?: error("Cannot find message mail.request.approveActivity.subject")

        mailService.send(appProperties.mail.from, appProperties.binnacle.vacationsApprovers, subject, body)
            .onFailure { logger.error("Error sending activity approve email", it) }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(ApproveActivityMailService::class.java)
    }
}