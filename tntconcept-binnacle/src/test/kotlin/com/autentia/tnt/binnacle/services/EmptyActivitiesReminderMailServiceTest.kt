package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Mail
import io.micronaut.context.MessageSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EmptyActivitiesReminderMailServiceTest {

    @Nested
    @DisplayName("Send emails test")
    inner class ServiceTest {
        private val mailService = mock<MailService>()
        private val emptyActivitiesReminderMailBuilder: EmptyActivitiesReminderMailBuilder = mock()
        private val appProperties = AppProperties().apply {
            mail.from = "from@test.com"
            binnacle.emptyActivitiesReminder.enabled = true
        }
        private val emptyActivitiesReminderMailService =
            EmptyActivitiesReminderMailService(mailService, emptyActivitiesReminderMailBuilder, appProperties)

        @Test
        fun `given reminder should try to send email`() {
            val mailToSend = Mail("subject", "body")
            val locale = Locale.ENGLISH
            val date = LocalDate.now()
            whenever(
                emptyActivitiesReminderMailBuilder.buildMessage(locale, listOf(date))
            ).thenReturn(mailToSend)

            emptyActivitiesReminderMailService.sendEmail(listOf(date), "user@mail.com", locale)

            verify(mailService).send(
                appProperties.mail.from,
                listOf("user@mail.com"),
                mailToSend.subject,
                mailToSend.body
            )
        }

    }

    @Nested
    @DisplayName("Message builder test")
    inner class BuilderTest {
        private val subjectKey = "mail.request.emptyActivitiesReminder.subject"
        private val headerKey = "mail.request.emptyActivitiesReminder.header"
        private val elementKey = "mail.request.emptyActivitiesReminder.element"
        private val footerKey = "mail.request.emptyActivitiesReminder.footer"
        private val messageSource: MessageSource = mock()
        private val emptyActivitiesReminderMailBuilder =
            EmptyActivitiesReminderMailBuilder(messageSource = messageSource)

        @Test
        fun `test build mail`() {
            val locale = Locale.ENGLISH
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)
            val date = LocalDate.now()
            whenever(messageSource.getMessage(subjectKey, locale)).thenReturn(Optional.of("subject"))
            whenever(messageSource.getMessage(headerKey, locale)).thenReturn(Optional.of("header"))
            whenever(
                messageSource.getMessage(
                    elementKey,
                    locale,
                    date.format(formatter)
                )
            ).thenReturn(Optional.of("element"))
            whenever(messageSource.getMessage(footerKey, locale)).thenReturn(Optional.of("footer"))

            val mail = emptyActivitiesReminderMailBuilder.buildMessage(locale, listOf(date, date));

            assertThat(mail.subject).isEqualTo("subject")
            assertThat(mail.body).isEqualTo("headerelementelementfooter")
        }
    }
}
