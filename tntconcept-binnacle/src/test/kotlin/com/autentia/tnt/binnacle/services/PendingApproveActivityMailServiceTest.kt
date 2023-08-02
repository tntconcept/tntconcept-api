package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRoleWithTimeUnit
import com.autentia.tnt.binnacle.entities.TimeUnit
import io.micronaut.context.MessageSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.format.DateTimeFormatter
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PendingApproveActivityMailServiceTest {

    private val mailService = mock<MailService>()
    private val messageSource = mock<MessageSource>()
    private val appProperties = AppProperties()

    private val sut = PendingApproveActivityMailService(mailService, messageSource, appProperties)

    @AfterEach
    fun resetMocks() {
        reset(mailService)
        reset(messageSource)
    }

    @Test
    fun `should send email to approve users from app properties and from method parameter`() {
        // Given
        appProperties.mail.enabled = true
        appProperties.mail.from = "fromTest@email.com"
        appProperties.binnacle.activitiesApprovers = listOf("approver@email.com")

        val anActivity = createActivity().toDomain()
        val username = "myuser"
        val projectName = anActivity.projectRole.project.name
        val projectRoleName = anActivity.projectRole.name
        val aLocale = Locale.ENGLISH
        val listOfApprovalUsers = listOf("approver@email.com", "two@email.com", "three@email.com")

        whenever(
            messageSource.getMessage(
                "mail.request.pendingApproveActivity.template",
                aLocale,
                projectName,
                projectRoleName,
                username,
                anActivity.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                anActivity.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                anActivity.description
            )
        ).thenReturn(Optional.of("Message"))
        whenever(messageSource.getMessage("mail.request.pendingApproveActivity.subject", aLocale, username)).thenReturn(
            Optional.of("Subject")
        )

        whenever(this.mailService.send(any(), any(), any(), any(), any())).thenReturn(Result.success("Email sent"))

        // When
        sut.sendApprovalActivityMail(anActivity, username, aLocale, listOfApprovalUsers)

        // Then
        val expectedDestination = listOf("approver@email.com", "two@email.com", "three@email.com")
        verify(this.mailService).send(appProperties.mail.from, expectedDestination, "Subject", "Message")
    }

    @Test
    fun `should not send time if activity is in days`() {
        // Given
        appProperties.mail.enabled = true
        appProperties.mail.from = "fromTest@email.com"
        appProperties.binnacle.activitiesApprovers = listOf("approver@email.com")

        val projectRole = createProjectRoleWithTimeUnit(1L, TimeUnit.DAYS)
        val anActivity = createActivity(projectRole = projectRole).toDomain()
        val projectName = anActivity.projectRole.project.name
        val projectRoleName = anActivity.projectRole.name
        val username = "myuser"
        val aLocale = Locale.ENGLISH
        val listOfApprovalUsers = listOf("approver@email.com", "two@email.com", "three@email.com")

        whenever(
            messageSource.getMessage(
                "mail.request.pendingApproveActivity.template",
                aLocale,
                projectName,
                projectRoleName,
                username,
                anActivity.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                anActivity.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                anActivity.description
            )
        ).thenReturn(Optional.of("Message"))
        whenever(messageSource.getMessage("mail.request.pendingApproveActivity.subject", aLocale, username)).thenReturn(
            Optional.of("Subject")
        )

        whenever(this.mailService.send(any(), any(), any(), any(), any())).thenReturn(Result.success("Email sent"))

        // When
        sut.sendApprovalActivityMail(anActivity, username, aLocale, listOfApprovalUsers)

        // Then
        val expectedDestination = listOf("approver@email.com", "two@email.com", "three@email.com")
        verify(this.mailService).send(appProperties.mail.from, expectedDestination, "Subject", "Message")
    }
}