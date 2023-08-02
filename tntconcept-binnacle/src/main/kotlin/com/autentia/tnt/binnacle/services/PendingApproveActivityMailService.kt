package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Activity
import io.micronaut.context.MessageSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import java.util.Locale

@Singleton
internal class PendingApproveActivityMailService(
        private val mailService: MailService,
        private val messageSource: MessageSource,
        private val appProperties: AppProperties
) {
    fun sendApprovalActivityMail(activity: Activity, username: String, locale: Locale,
                                 listOfActivityApprovalUserEmails: List<String>) {
        if (!appProperties.mail.enabled) {
            logger.info("Mailing of approval activities is disabled")
            return
        }
        val body = messageSource
                .getMessage(
                        "mail.request.pendingApproveActivity.template",
                        locale,
                        activity.projectRole.project.name,
                        activity.projectRole.name,
                        username,
                        activity.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        activity.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        activity.description
                )
                .orElse(null) ?: error("Cannot find message mail.request.pendingApproveActivity.template")

        val subject = messageSource
                .getMessage("mail.request.pendingApproveActivity.subject", locale, username)
                .orElse(null) ?: error("Cannot find message mail.request.pendingApproveActivity.subject")


        val emailDestinations = mergeAndRemoveDuplicates(appProperties.binnacle.activitiesApprovers,
                listOfActivityApprovalUserEmails)

        mailService.send(appProperties.mail.from, emailDestinations.toList(), subject, body)
                .onFailure { logger.error("Error sending activity approve email", it) }
    }

    fun mergeAndRemoveDuplicates(list1: List<String>, list2: List<String>): List<String> {
        val mergedList = list1 + list2
        return mergedList.distinct()
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(PendingApproveActivityMailService::class.java)
    }
}