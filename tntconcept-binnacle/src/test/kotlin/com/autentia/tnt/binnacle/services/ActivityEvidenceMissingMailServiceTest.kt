package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Mail
import com.autentia.tnt.binnacle.entities.RequireEvidence
import io.micronaut.context.MessageSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActivityEvidenceMissingMailServiceTest {

    @Nested
    @DisplayName("Send emails test")
    inner class ServiceTest {
        private val mailService = mock<MailService>()
        private val messageBuilder = mock<ActivityEvidenceMissingMessageBuilder>()
        private val appProperties = AppProperties()

        private val sut = ActivityEvidenceMissingMailService(mailService, messageBuilder, appProperties)

        @AfterEach
        fun resetMocks() = reset(mailService, messageBuilder)

        @Test
        fun `should not send email when enabled property is set to false`() {
            // Given
            appProperties.binnacle.missingEvidencesNotification.enabled = false
            appProperties.mail.from = "fromTest@email.com"

            val organizationName = "Organization"
            val projectName = "Test project"
            val role = "Role one"
            val evidence = RequireEvidence.WEEKLY
            val email = "userEmail@email.com"
            val locale = Locale.ENGLISH

            // When
            sut.sendEmail(organizationName, projectName, role, evidence, email, locale)

            // Then
            verifyNoInteractions(mailService, messageBuilder)
        }

        @Test
        fun `should send email when enabled property is set to true`() {
            // Given
            appProperties.binnacle.missingEvidencesNotification.enabled = true
            appProperties.mail.from = "fromTest@email.com"

            val organizationName = "Organization"
            val projectName = "Test project"
            val role = "Role one"
            val toUserEmail = "userEmail@email.com"
            val locale = Locale.ENGLISH
            val evidence = RequireEvidence.WEEKLY

            doReturn(Mail("Subject", "Body")).`when`(messageBuilder)
                .buildMessage(locale, organizationName, projectName, role, evidence)
            doReturn(Result.success("OK")).`when`(mailService)
                .send(anyString(), anyList(), anyString(), anyString(), anyOrNull())

            // When
            sut.sendEmail(organizationName, projectName, role, evidence, toUserEmail, locale)

            // Then
            val expectedFrom = "fromTest@email.com"
            val expectedTo = listOf(toUserEmail)
            val expectedSubject = "Subject"
            val expectedBody = "Body"

            verify(mailService).send(
                eq(expectedFrom), eq(expectedTo), eq(expectedSubject), eq(expectedBody), anyOrNull()
            )

            verify(messageBuilder).buildMessage(locale, organizationName, projectName, role, evidence)
        }
    }

    @Nested
    @DisplayName("Build evidence activity email")
    inner class MessageBuilderTest {
        private val messageSource = mock<MessageSource>()

        private val sut = ActivityEvidenceMissingMessageBuilder(messageSource)

        @AfterEach
        fun resetMocks() = reset(messageSource)

        @Test
        fun `should build localized email content`() {
            // Given
            val locale = Locale.forLanguageTag("es")
            val organizationName = "Organization"
            val projectName = "Project"
            val evidence = RequireEvidence.WEEKLY
            val roleName = "Role one"

            val subjectMsg = "Sujeto de la evidencia"
            val bodyMsg = "Cuerpo de la evidencia"

            doReturn(Optional.of(subjectMsg)).`when`(messageSource)
                .getMessage("mail.request.evidenceActivity.subject", locale, organizationName, projectName, roleName)

            doReturn(Optional.of(bodyMsg)).`when`(messageSource)
                .getMessage("mail.request.evidenceActivity.template", locale, "weekly", projectName, roleName)

            doReturn(Optional.of("weekly")).`when`(messageSource)
                .getMessage("mail.request.evidenceActivity.frequency.weekly", locale)

            // When
            val message = sut.buildMessage(locale, organizationName, projectName, roleName, evidence)

            // Then
            assertThat(message.subject).isEqualTo(subjectMsg)
            assertThat(message.body).isEqualTo(bodyMsg)

            verify(messageSource).getMessage(
                "mail.request.evidenceActivity.subject", locale, organizationName, projectName, roleName
            )
            verify(messageSource).getMessage(
                "mail.request.evidenceActivity.template", locale, "weekly", projectName, roleName
            )
            verify(messageSource).getMessage("mail.request.evidenceActivity.frequency.weekly", locale)
        }

    }
}