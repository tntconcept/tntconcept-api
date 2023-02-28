package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.UserAnnualWorkSummary
import jakarta.inject.Singleton
import java.io.File
import java.io.FileWriter

@Singleton
internal class WorkSummaryReportService(
    private val appProperties: AppProperties,
    private val reportPrinter: ReportPrinter,
) {

    fun createReport(year: Int, summaries: Map<Long, UserAnnualWorkSummary>): File {
        val filename = "${year}${appProperties.binnacle.workSummary.report.nameSuffix}.csv"
        val file = File("${appProperties.binnacle.workSummary.report.path}/$filename")
        file.parentFile.mkdirs()
        FileWriter(file, false).use { writer -> reportPrinter.print(writer, summaries) }
        return file
    }

}
