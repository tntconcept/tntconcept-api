package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Mail
import io.micronaut.context.MessageSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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
        if (workableDays.isNotEmpty() && toUserEmail.isNotEmpty()) {
            val mail = emptyActivitiesReminderMailBuilder.buildMessage(locale, workableDays)
            mailService.send(appProperties.mail.from, listOf(toUserEmail), mail.subject, mail.body)
                .onFailure { logger.error("Error sending empty activities reminder email", it) }
        }
    }
}

@Singleton
internal class EmptyActivitiesReminderMailBuilder(private val messageSource: MessageSource) {

    private companion object {
        private const val subjectKey = "mail.request.emptyActivitiesReminder.subject"
        private const val headerKey = "mail.request.emptyActivitiesReminder.template.header"
        private const val elementKey = "mail.request.emptyActivitiesReminder.template.element"
        private const val footerKey = "mail.request.emptyActivitiesReminder.template.footer"
    }

    fun buildMessage(
        locale: Locale,
        workableDays: List<LocalDate>
    ): Mail {
        val subject =
            messageSource.getMessage(subjectKey, locale).orElse(null)
                ?: error("Cannot find message $subjectKey")
        val bodyMessage = generateBody(workableDays, locale)
        return Mail(subject, bodyMessage)
    }

    private fun generateBody(workableDays: List<LocalDate>, locale: Locale): String {
        val bodyMessage = StringBuilder()
        val headerMessage = messageSource.getMessage(headerKey, locale).orElse(null)
            ?: error("Cannot find message $headerKey")
        bodyMessage.append(headerMessage)
        workableDays.forEach {
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)
            val elementoFormateado = messageSource.getMessage(elementKey, locale, it.format(formatter))
                .orElse(null) ?: error("Cannot find message $elementKey")
            bodyMessage.append(elementoFormateado)
        }
        val footerMessage = messageSource.getMessage(footerKey, locale).orElse(null)
            ?: error("Cannot find message $footerKey")
        bodyMessage.append(footerMessage)
        return bodyMessage.toString()
    }

}
