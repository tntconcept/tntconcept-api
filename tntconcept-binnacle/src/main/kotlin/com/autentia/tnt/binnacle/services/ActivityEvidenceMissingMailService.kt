package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import io.micronaut.context.MessageSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*

@Singleton
internal class ActivityEvidenceMissingMailService(
    private val mailService: MailService,
    private val messageBuilder: ActivityEvidenceMissingMessageBuilder,
    private val appProperties: AppProperties
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(ActivityEvidenceMissingMailService::class.java)
    }

    fun sendEmail(
        organizationName: String,
        projectName: String,
        projectRoleNames: Set<String>,
        toUserEmail: String,
        locale: Locale
    ) {
        require(organizationName.isNotEmpty())
        require(projectName.isNotEmpty())
        require(toUserEmail.isNotEmpty())
        require(projectRoleNames.isNotEmpty())

        if (!appProperties.binnacle.missingEvidencesNotification.enabled) {
            logger.info("Mailing of activity evidence is disabled")
            return
        }

        val subject = messageBuilder.buildSubject(locale, organizationName, projectName)
        val body = messageBuilder.buildBody(locale, organizationName, projectName, projectRoleNames)

        mailService.send(appProperties.mail.from, listOf(toUserEmail), subject, body)
            .onFailure { logger.error("Error sending activity evidence email", it) }
    }
}

@Singleton
internal class ActivityEvidenceMissingMessageBuilder(private val messageSource: MessageSource) {

    private companion object {
        private const val subjectKey = "mail.request.evidenceActivity.subject"
        private const val bodyKey = "mail.request.evidenceActivity.template"
    }

    fun buildSubject(locale: Locale, organizationName: String, projectName: String): String =
        messageSource.getMessage(subjectKey, locale, organizationName, projectName)
            .orElse(null) ?: error("Cannot find message $subjectKey")

    fun buildBody(
        locale: Locale,
        organizationName: String,
        projectName: String,
        projectRoleNames: Set<String>,
    ): String =
        messageSource.getMessage(bodyKey, locale, getRoleLines(organizationName, projectName, projectRoleNames))
            .orElse(null) ?: error("Cannot find message $bodyKey")

    private fun getRoleLines(organizationName: String, projectName: String, projectRoleNames: Set<String>): String =
        projectRoleNames.joinToString("\n") { "$organizationName - $projectName - $it" }

}