package com.autentia.tnt.binnacle.config.worksummary

import com.autentia.tnt.binnacle.services.CsvReportPrinter
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Prototype
import jakarta.inject.Singleton

@Prototype
@Factory
internal class WorkSummaryReportFactory {

    @Singleton
    fun reportPrinter() = CsvReportPrinter()

}
