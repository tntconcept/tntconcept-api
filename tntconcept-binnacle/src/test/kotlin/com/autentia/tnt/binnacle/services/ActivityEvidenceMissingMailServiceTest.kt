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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActivityEvidenceMissingMailServiceTest {

    @Nested
    @DisplayName("Send emails test")
    inner class ServiceTest {
        private val mailService = mock<MailService>()
        private val activityEvidenceMissingMailBuilder = mock<ActivityEvidenceMissingMailBuilder>()
        private val appProperties = AppProperties()

        private val sut =
            ActivityEvidenceMissingMailService(mailService, activityEvidenceMissingMailBuilder, appProperties)

        @AfterEach
        fun resetMocks() = reset(mailService, activityEvidenceMissingMailBuilder)

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
            val date = LocalDateTime.now()
            val locale = Locale.ENGLISH

            // When
            sut.sendEmail(organizationName, projectName, role, evidence, date, email, locale)

            // Then
            verifyNoInteractions(mailService, activityEvidenceMissingMailBuilder)
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
            val date = LocalDateTime.now()

            doReturn(Mail("Subject", "Body")).`when`(activityEvidenceMissingMailBuilder)
                .buildMessage(locale, organizationName, projectName, role, evidence, date)
            doReturn(Result.success("OK")).`when`(mailService)
                .send(anyString(), anyList(), anyString(), anyString(), anyOrNull())

            // When
            sut.sendEmail(organizationName, projectName, role, evidence, date, toUserEmail, locale)

            // Then
            val expectedFrom = "fromTest@email.com"
            val expectedTo = listOf(toUserEmail)
            val expectedSubject = "Subject"
            val expectedBody = "Body"

            verify(mailService).send(
                eq(expectedFrom), eq(expectedTo), eq(expectedSubject), eq(expectedBody), anyOrNull()
            )

            verify(activityEvidenceMissingMailBuilder).buildMessage(
                locale,
                organizationName,
                projectName,
                role,
                evidence,
                date
            )
        }
    }

    @Nested
    @DisplayName("Build evidence activity email")
    inner class MailBuilderTest {
        private val messageSource = mock<MessageSource>()

        private val sut = ActivityEvidenceMissingMailBuilder(messageSource)

        @AfterEach
        fun resetMocks() = reset(messageSource)

        @Test
        fun `should build weekly localized email content`() {
            // Given
            val locale = Locale.forLanguageTag("es")
            val organizationName = "Organization"
            val projectName = "Project"
            val evidence = RequireEvidence.WEEKLY
            val roleName = "Role one"
            val date = LocalDateTime.now()

            val subjectMsg = "Sujeto de la evidencia"
            val bodyMsg = "Cuerpo de la evidencia"

            doReturn(Optional.of(subjectMsg)).`when`(messageSource)
                .getMessage("mail.request.evidenceActivity.subject", locale, organizationName, projectName, roleName)

            doReturn(Optional.of(bodyMsg)).`when`(messageSource)
                .getMessage("mail.request.evidenceActivity.template", locale, "weekly", projectName, roleName)

            doReturn(Optional.of("weekly")).`when`(messageSource)
                .getMessage("mail.request.evidenceActivity.frequency.weekly", locale)

            // When
            val message = sut.buildMessage(locale, organizationName, projectName, roleName, evidence, date)

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

        @Test
        fun `should build once localized email content`() {
            // Given
            val locale = Locale.forLanguageTag("es")
            val organizationName = "Organization"
            val projectName = "Project"
            val evidence = RequireEvidence.ONCE
            val roleName = "Role one"
            val date = LocalDateTime.now()
            val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val subjectMsg = "Sujeto de la evidencia"
            val bodyMsg = "Cuerpo de la evidencia"

            doReturn(Optional.of(subjectMsg)).`when`(messageSource)
                .getMessage("mail.request.evidenceActivity.subject", locale, organizationName, projectName, roleName)

            doReturn(Optional.of(bodyMsg)).`when`(messageSource)
                .getMessage("mail.request.evidenceActivity.template", locale, "once", projectName, roleName)

            doReturn(Optional.of("once")).`when`(messageSource)
                .getMessage("mail.request.evidenceActivity.frequency.once", locale, dateString)

            // When
            val message = sut.buildMessage(locale, organizationName, projectName, roleName, evidence, date)

            // Then
            assertThat(message.subject).isEqualTo(subjectMsg)
            assertThat(message.body).isEqualTo(bodyMsg)

            verify(messageSource).getMessage(
                "mail.request.evidenceActivity.subject", locale, organizationName, projectName, roleName
            )
            verify(messageSource).getMessage(
                "mail.request.evidenceActivity.template", locale, "once", projectName, roleName
            )
            verify(messageSource).getMessage("mail.request.evidenceActivity.frequency.once", locale, dateString)
        }

    }
}