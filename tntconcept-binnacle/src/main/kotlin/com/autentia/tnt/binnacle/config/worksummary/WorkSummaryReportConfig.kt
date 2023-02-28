package com.autentia.tnt.binnacle.config.worksummary

import com.autentia.tnt.binnacle.services.CsvReportPrinter
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
internal class WorkSummaryReportConfig {

    @Singleton
    fun reportPrinter() = CsvReportPrinter()

}
