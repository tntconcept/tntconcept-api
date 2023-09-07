package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRoleWithTimeUnit
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.entities.TimeUnit
import io.micronaut.context.MessageSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.format.DateTimeFormatter
import java.util.*

class ApprovedActivityMailServiceTest {

    private val mailService = Mockito.mock<MailService>()
    private val messageSource = Mockito.mock<MessageSource>()
    private val appProperties = AppProperties()

    private val sut = ApprovedActivityMailService(mailService, messageSource, appProperties)

    @AfterEach
    fun resetMocks() {
        reset(mailService)
        reset(messageSource)
    }

    @Test
    fun `should send email to approve users from app properties and from method parameter`() {
        appProperties.mail.enabled = true
        appProperties.mail.from = "fromTest@email.com"

        val activity = createActivity().toDomain()
        val user = createUser()
        val locale = Locale.ENGLISH

        whenever(
            messageSource.getMessage(
                "mail.request.approvedActivity.template",
                locale,
                activity.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                activity.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                activity.description
            )
        ).thenReturn(Optional.of("Message"))

        whenever(messageSource.getMessage("mail.request.approvedActivity.subject", locale)).thenReturn(
            Optional.of("Subject")
        )

        whenever(this.mailService.send(any(), any(), any(), any(), any())).thenReturn(Result.success("Email sent"))

        sut.sendApprovedActivityMail(activity, user, locale)

        val expectedDestination = listOf("jdoe@doe.com")
        verify(this.mailService).send(appProperties.mail.from, expectedDestination, "Subject", "Message")
    }

    @Test
    fun `should send email to approve users without time from app properties and from method parameter when activity is in days`() {
        appProperties.mail.enabled = true
        appProperties.mail.from = "fromTest@email.com"

        val projectRole = createProjectRoleWithTimeUnit(1L, TimeUnit.DAYS)
        val anActivity = createActivity(projectRole = projectRole).toDomain()
        val user = createUser()
        val aLocale = Locale.ENGLISH

        whenever(
            messageSource.getMessage(
                "mail.request.approvedActivity.template",
                aLocale,
                anActivity.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                anActivity.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                anActivity.description
            )
        ).thenReturn(Optional.of("Message"))

        whenever(messageSource.getMessage("mail.request.approvedActivity.subject", aLocale)).thenReturn(
            Optional.of("Subject")
        )

        whenever(this.mailService.send(any(), any(), any(), any(), any())).thenReturn(Result.success("Email sent"))

        sut.sendApprovedActivityMail(anActivity, user, aLocale)

        val expectedDestination = listOf("jdoe@doe.com")
        verify(this.mailService).send(appProperties.mail.from, expectedDestination, "Subject", "Message")
    }
}