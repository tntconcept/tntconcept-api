package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.entities.RequireEvidence
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
        roleName: String,
        roleRequireEvidence: RequireEvidence,
        toUserEmail: String,
        locale: Locale
    ) {
        require(organizationName.isNotEmpty())
        require(projectName.isNotEmpty())
        require(toUserEmail.isNotEmpty())
        require(roleName.isNotEmpty())

        if (roleRequireEvidence == RequireEvidence.NO) {
            throw IllegalArgumentException("Require evidence NO is not supported")
        }

        if (!appProperties.binnacle.missingEvidencesNotification.enabled) {
            logger.info("Mailing of activity evidence is disabled")
            return
        }

        val message = messageBuilder.buildMessage(locale, organizationName, projectName, roleName, roleRequireEvidence)

        mailService.send(appProperties.mail.from, listOf(toUserEmail), message.subject, message.body)
            .onFailure { logger.error("Error sending activity evidence email", it) }
    }
}

@Singleton
internal class ActivityEvidenceMissingMessageBuilder(private val messageSource: MessageSource) {

    private companion object {
        private const val subjectKey = "mail.request.evidenceActivity.subject"
        private const val bodyKey = "mail.request.evidenceActivity.template"
    }

    fun buildMessage(
        locale: Locale,
        organizationName: String,
        projectName: String,
        projectRoleName: String,
        requireEvidence: RequireEvidence
    ): ActivityEvidenceMissingMessageContent {
        val subject =
            messageSource.getMessage(subjectKey, locale, organizationName, projectName, projectRoleName).orElse(null)
                ?: error("Cannot find message $subjectKey")

        val frequency = this.getFrequencyTextByLocale(locale, requireEvidence)

        val body = messageSource.getMessage(bodyKey, locale, frequency, projectName, projectRoleName).orElse(null)
            ?: error("Cannot find message $bodyKey")

        return ActivityEvidenceMissingMessageContent(subject, body)
    }

    private fun getFrequencyTextByLocale(locale: Locale, requireEvidence: RequireEvidence): String {
        val property = when (requireEvidence) {
            RequireEvidence.WEEKLY -> "weekly"
            RequireEvidence.ONCE -> "once"
            RequireEvidence.NO -> "no"
        }

        val key = "mail.request.evidenceActivity.frequency.$property"

        return messageSource.getMessage(key, locale).orElse(null) ?: error("Cannot find frequency message $key")
    }
}

internal data class ActivityEvidenceMissingMessageContent(val subject: String, val body: String)