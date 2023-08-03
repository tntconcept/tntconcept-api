package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.User
import io.micronaut.context.MessageSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import java.util.*

@Singleton
internal class ApprovedActivityMailService(
    private val mailService: MailService,
    private val messageSource: MessageSource,
    private val appProperties: AppProperties,
) {
    fun sendApprovedActivityMail(activity: Activity, user: User, locale: Locale) {
        if (!appProperties.mail.enabled) {
            logger.info("Mailing of approval activities is disabled")
            return
        }

        val datePattern = getPatternOfActivityByTimeUnit(activity)

        val body = messageSource
            .getMessage(
                "mail.request.approvedActivity.template",
                locale,
                activity.timeInterval.start.format(DateTimeFormatter.ofPattern(datePattern)),
                activity.timeInterval.end.format(DateTimeFormatter.ofPattern(datePattern)),
                activity.description
            )
            .orElse(null) ?: error("Cannot find message mail.request.approvedActivity.template")

        val subject = messageSource
            .getMessage("mail.request.approvedActivity.subject", locale)
            .orElse(null) ?: error("Cannot find message mail.request.approvedActivity.subject")

        mailService.send(appProperties.mail.from, listOf(user.email), subject, body)
            .onFailure { logger.error("Error sending activity approve email", it) }
    }

    fun getPatternOfActivityByTimeUnit(activity: Activity) =
        if (activity.timeUnit == TimeUnit.MINUTES) "yyyy-MM-dd HH:mm:ss" else "yyyy-MM-dd"

    private companion object {
        private val logger = LoggerFactory.getLogger(ApprovedActivityMailService::class.java)
    }
}