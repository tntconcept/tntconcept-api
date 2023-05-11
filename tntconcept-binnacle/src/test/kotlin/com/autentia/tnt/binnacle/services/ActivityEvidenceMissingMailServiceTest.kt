package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
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
            val roles = setOf("Role one")
            val email = "userEmail@email.com"
            val locale = Locale.ENGLISH

            // When
            sut.sendEmail(organizationName, projectName, roles, email, locale)

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
            val roles = setOf("Role one")
            val toUserEmail = "userEmail@email.com"
            val locale = Locale.ENGLISH

            doReturn("Subject").`when`(messageBuilder).buildSubject(locale, organizationName, projectName)
            doReturn("Body").`when`(messageBuilder).buildBody(locale, organizationName, projectName, roles)
            doReturn(Result.success("OK")).`when`(mailService)
                .send(anyString(), anyList(), anyString(), anyString(), anyOrNull())

            // When
            sut.sendEmail(organizationName, projectName, roles, toUserEmail, locale)

            // Then
            val expectedFrom = "fromTest@email.com"
            val expectedTo = listOf(toUserEmail)
            val expectedSubject = "Subject"
            val expectedBody = "Body"

            verify(mailService).send(
                eq(expectedFrom),
                eq(expectedTo),
                eq(expectedSubject),
                eq(expectedBody),
                anyOrNull()
            )
            verify(messageBuilder).buildSubject(locale, organizationName, projectName)
            verify(messageBuilder).buildBody(locale, organizationName, projectName, roles)
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
        fun `should build localized email subject`() {
            // Given
            val locale = Locale.forLanguageTag("es")
            val organizationName = "Organization"
            val projectName = "Project"

            doReturn(Optional.of("Falta evidencia $organizationName - $projectName")).`when`(messageSource)
                .getMessage("mail.request.evidenceActivity.subject", locale, organizationName, projectName)

            // When
            val subject = sut.buildSubject(locale, organizationName, projectName)

            // Then
            assertThat(subject).isEqualTo("Falta evidencia $organizationName - $projectName")
            verify(messageSource).getMessage(
                "mail.request.evidenceActivity.subject",
                locale,
                organizationName,
                projectName
            )
        }

        @Test
        fun `should build localized email body with one role`() {
            // Given
            val locale = Locale.forLanguageTag("es")
            val organizationName = "Organization"
            val projectName = "Project"
            val roleNames = setOf("Role one")

            val evidenceBody = """
                Cada día es necesario adjuntar al menos una evidencia de tu participación en el proyecto.
                Como por ejemplo una captura de pantalla o foto de tu histórico en el repositorio de código o de tus issues en la herramienta de seguimiento de proyecto.
                Organization - Project - Role one
                ¡Gracias!
            """.trimIndent()

            doReturn(Optional.of(evidenceBody)).`when`(messageSource)
                .getMessage(eq("mail.request.evidenceActivity.template"), eq(locale), anyString())

            // When
            val body = sut.buildBody(locale, organizationName, projectName, roleNames)

            // Then
            assertThat(body).isEqualTo(evidenceBody)
            verify(messageSource).getMessage(
                "mail.request.evidenceActivity.template",
                locale,
                "Organization - Project - Role one"
            )
        }

        @Test
        fun `should build localized email body with multiple roles`() {
            // Given
            val locale = Locale.forLanguageTag("es")
            val organizationName = "Organization"
            val projectName = "Project"
            val roleNames = setOf("Role one", "Role two", "Role three")

            val evidenceBody = """
                Cada día es necesario adjuntar al menos una evidencia de tu participación en el proyecto.
                Como por ejemplo una captura de pantalla o foto de tu histórico en el repositorio de código o de tus issues en la herramienta de seguimiento de proyecto.
                Organization - Project - Role one
                Organization - Project - Role two
                Organization - Project - Role three
                ¡Gracias!
            """.trimIndent()

            doReturn(Optional.of(evidenceBody)).`when`(messageSource)
                .getMessage(eq("mail.request.evidenceActivity.template"), eq(locale), anyString())

            // When
            val body = sut.buildBody(locale, organizationName, projectName, roleNames)

            // Then
            val expectedRoleLines = """
                Organization - Project - Role one
                Organization - Project - Role two
                Organization - Project - Role three
            """.trimIndent()
            assertThat(body).isEqualTo(evidenceBody)
            verify(messageSource).getMessage("mail.request.evidenceActivity.template", locale, expectedRoleLines)
        }
    }
}