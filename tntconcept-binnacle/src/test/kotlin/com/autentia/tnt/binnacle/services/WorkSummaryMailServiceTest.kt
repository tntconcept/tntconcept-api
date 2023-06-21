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
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class WorkSummaryMailServiceTest {

    @Nested
    @DisplayName("Send emails test")
    inner class ServiceTest {

        private val mailService = mock<MailService>()
        private val workSummaryMailBuilder: WorkSummaryMailBuilder = mock()
        private val appProperties = AppProperties().apply {
            mail.from = "from@test.com"
            binnacle.workSummary.mail.to = listOf("to@test.com")
            binnacle.workSummary.mail.enabled = true
        }

        private val workSummaryMailService = WorkSummaryMailService(mailService, workSummaryMailBuilder, appProperties)

        @Test
        fun `given report should try to send email`() {
            val report: File = mock()
            val mailToSend = Mail("subject", "body")
            whenever(workSummaryMailBuilder.buildMail()).thenReturn(mailToSend)

            workSummaryMailService.sendReport(report)

            verify(mailService).send(
                appProperties.mail.from,
                appProperties.binnacle.workSummary.mail.to,
                mailToSend.subject,
                mailToSend.body,
                report
            )

        }

        @Test
        fun `should not send email when not enabled`() {
            val report: File = mock()
            whenever(workSummaryMailBuilder.buildMail()).thenReturn(Mail("subject", "body"))
            appProperties.binnacle.workSummary.mail.enabled = false

            workSummaryMailService.sendReport(report)

            verifyNoInteractions(mailService)
        }
    }

    @Nested
    @DisplayName("Message builder test")
    inner class BuilderTest {
        private val subjectKey = "mail.request.workSummary.subject"
        private val bodyKey = "mail.request.workSummary.template"
        private val messageSource: MessageSource = mock()
        private val workSummaryMailBuilder = WorkSummaryMailBuilder(messageSource = messageSource)

        @Test
        fun `test build mail`() {
            val subject = "Subject"
            val body = "Body"
            val locale = Locale.ENGLISH
            whenever(messageSource.getMessage(subjectKey, locale)).thenReturn(Optional.of(subject))
            whenever(messageSource.getMessage(bodyKey, locale)).thenReturn(Optional.of(body))

            val mail = workSummaryMailBuilder.buildMail()

            assertThat(mail.subject).isEqualTo(subject)
            assertThat(mail.body).isEqualTo(body)
        }
    }
}
