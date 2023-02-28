package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.UserAnnualWorkSummary
import com.autentia.tnt.binnacle.core.utils.toBigDecimalHours
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

internal const val EMPTY_ALERTS_MESSAGE = "OK"
internal const val ALERT_MESSAGES_SEPARATOR = ". "

internal class CsvReportPrinter : ReportPrinter {

    override fun print(appendable: Appendable, summaries: Map<Long, UserAnnualWorkSummary>) =
        CSVPrinter(appendable, CSVFormat.DEFAULT.builder().setHeader(Headers::class.java).build())
            .use { printer ->
                summaries.values.forEach { userSummary ->
                    printer.printRecord(
                        userSummary.user.name,
                        userSummary.summary.targetWorkingTime.toBigDecimalHours().toString(),
                        userSummary.summary.workedTime.toBigDecimalHours().toString(),
                        userSummary.summary.workedTime.minus(userSummary.summary.targetWorkingTime).toBigDecimalHours()
                            .toString(),
                        userSummary.summary.earnedVacations,
                        userSummary.summary.consumedVacations,
                        userSummary.summary.earnedVacations - userSummary.summary.consumedVacations,
                        userSummary.summary.alerts.map { it.description }.ifEmpty { listOf(EMPTY_ALERTS_MESSAGE) }
                            .joinToString(ALERT_MESSAGES_SEPARATOR)
                    )
                }
            }

    enum class Headers {
        USER_NAME, TARGET, WORKED, HOURS_BALANCE, EARNED_VACATIONS, CONSUMED_VACATIONS, VACATIONS_BALANCE, SUMMARY
    }

}
