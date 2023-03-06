package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class WorkSummaryMailServiceTest {

    private val mailService = mock<MailService>()
    private val appProperties = AppProperties().apply {
        mail.from = FROM
        binnacle.workSummary.mail.to = TO
    }

    private val sut = WorkSummaryMailService(mailService, appProperties)

    private fun sendReportParametersProvider() = arrayOf(
        arrayOf(true, Result.success("OK")),
        arrayOf(true, Result.failure<String>(RuntimeException("FAIL"))),
        arrayOf(false, Result.failure<String>(RuntimeException("FAIL")))
    )

    @ParameterizedTest
    @MethodSource("sendReportParametersProvider")
    fun `given report should try to send email`(enabled: Boolean, mailResult: Result<String>) {
        val report = mock<File>()

        appProperties.binnacle.workSummary.mail.enabled = enabled
        doReturn(mailResult).whenever(mailService).send(FROM, TO, SUBJECT, BODY_TEXT, report)

        sut.sendReport(report)

        val invocations = if(enabled) 1 else 0
        verify(mailService, times(invocations)).send(FROM, TO, SUBJECT, BODY_TEXT, report)
    }

    private companion object {
        private const val FROM = "from@test.com"
        private val TO = listOf("to@test.com")
    }

}
