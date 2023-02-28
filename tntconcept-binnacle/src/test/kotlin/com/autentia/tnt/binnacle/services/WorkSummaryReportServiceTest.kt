package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.UserAnnualWorkSummary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.FileNotFoundException

internal class WorkSummaryReportServiceTest {

    private val appProperties = AppProperties()
    private val reportPrinter = mock<ReportPrinter>()

    private var workSummaryReportService = WorkSummaryReportService(appProperties, reportPrinter)

    @Test
    fun `given path with permissions should return file created`() {
        //Given
        val year = 2022
        val summaries = mapOf<Long, UserAnnualWorkSummary>(mock())
        val path = "target/summaries/${year}"
        val suffix = "suffix"

        appProperties.binnacle.workSummary.report.path = path
        appProperties.binnacle.workSummary.report.nameSuffix = suffix

        //When
        val report = workSummaryReportService.createReport(year, summaries)

        //Then
        assertTrue(report.isFile)
        assertEquals("${path}/${year}${suffix}.csv", report.path)
        verify(reportPrinter).print(any(), any())
    }

    @Test
    fun `given path without permissions should return error`() {
        val year = 2022
        val summaries = emptyMap<Long, UserAnnualWorkSummary>()
        appProperties.binnacle.workSummary.report.path = "/"

        assertThrows<FileNotFoundException> {
            workSummaryReportService.createReport(year, summaries)
        }
    }
}
