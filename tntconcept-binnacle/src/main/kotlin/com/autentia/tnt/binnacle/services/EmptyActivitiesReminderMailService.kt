package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Mail
import io.micronaut.context.MessageSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*

@Singleton
internal class EmptyActivitiesReminderMailService(
    private val mailService: MailService,
    private val emptyActivitiesReminderMailBuilder: EmptyActivitiesReminderMailBuilder,
    private val appProperties: AppProperties
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(EmptyActivitiesReminderMailService::class.java)
    }

    fun sendEmail(
        workableDays: List<LocalDate>,
        toUserEmail: String,
        locale: Locale
    ) {
        require(workableDays.isNotEmpty())
        require(toUserEmail.isNotEmpty())

        if (!appProperties.binnacle.emptyActivitiesReminder.enabled) {
            logger.info("Mailing of empty activities reminder is disabled")
            return
        }

        val mail = emptyActivitiesReminderMailBuilder.buildMessage(
            locale,
            workableDays,
        )

        mailService.send(appProperties.mail.from, listOf(toUserEmail), mail.subject, mail.body)
            .onFailure { logger.error("Error sending empty activities reminder email", it) }
    }
}

@Singleton
internal class EmptyActivitiesReminderMailBuilder(private val messageSource: MessageSource) {

    private companion object {
        private const val subjectKey = "mail.request.emptyActivitiesReminder.subject"
        private const val bodyKey = "mail.request.emptyActivitiesReminder.template"
    }

    fun buildMessage(
        locale: Locale,
        workableDays: List<LocalDate>
    ): Mail {
        val subject =
            messageSource.getMessage(subjectKey, locale).orElse(null)
                ?: error("Cannot find message $subjectKey")

        //TODO CREATE MESSAGE WITH workableDays
        val workableDaysMessage = workableDays.toString()

        val body = messageSource.getMessage(bodyKey, locale, workableDaysMessage).orElse(null)
            ?: error("Cannot find message $bodyKey")

        return Mail(subject, body)
    }

}