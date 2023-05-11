package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummaryAlert
import com.autentia.tnt.binnacle.core.domain.User
import com.autentia.tnt.binnacle.core.domain.UserAnnualWorkSummary
import com.autentia.tnt.binnacle.core.utils.toBigDecimalHours
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.time.Duration

internal class CsvReportPrinterTest {

    private lateinit var sut: CsvReportPrinter

    @BeforeEach
    fun setUp() {
        sut = CsvReportPrinter()
    }

    @Test
    fun `given file and summaries should print csv`() {
        val file = File("target/test-report.csv")
        val fileWriter = FileWriter(file, false)
        val user1 = createDomainUser(1L, "userName1")
        val user2 = createDomainUser(2L, "userName2")
        val summaries = mapOf(
            user1.id to UserAnnualWorkSummary(
                user1,
                AnnualWorkSummary(
                    year = 2021,
                    workedTime = Duration.parse("1000h"),
                    targetWorkingTime = Duration.parse("1500h"),
                    earnedVacations = 22,
                    consumedVacations = 20,
                    alerts = emptyList()
                )
            ),
            user2.id to UserAnnualWorkSummary(
                user2,
                AnnualWorkSummary(
                    year = 2021,
                    workedTime = Duration.parse("1000h 40m"),
                    targetWorkingTime = Duration.parse("1300h"),
                    earnedVacations = 24,
                    consumedVacations = 23,
                    listOf(AnnualWorkSummaryAlert("alert1"), AnnualWorkSummaryAlert("alert2"))
                )
            )
        )

        //When
        sut.print(fileWriter, summaries)

        //Then
        FileReader(file).use { reader ->
            val csvParser = CSVFormat.DEFAULT.parse(reader).toList()
            assertEquals(CsvReportPrinter.Headers.values().map { it.name }, csvParser[0].toList())
            assertUser(user1, csvParser[1], summaries[user1.id]!!.summary)
            assertUser(user2, csvParser[2], summaries[user2.id]!!.summary)
        }
    }

    private fun assertUser(user: User, csvRecord: CSVRecord, annualWorkSummary: AnnualWorkSummary) {
        assertEquals(user.name, csvRecord[0])
        assertEquals(annualWorkSummary.targetWorkingTime.toBigDecimalHours().toString(), csvRecord[1])
        assertEquals(annualWorkSummary.workedTime.toBigDecimalHours().toString(), csvRecord[2])
        assertEquals(
            annualWorkSummary.workedTime.minus(annualWorkSummary.targetWorkingTime).toBigDecimalHours().toString(),
            csvRecord[3]
        )
        assertEquals(annualWorkSummary.earnedVacations.toString(), csvRecord[4])
        assertEquals(annualWorkSummary.consumedVacations.toString(), csvRecord[5])
        assertEquals((annualWorkSummary.earnedVacations - annualWorkSummary.consumedVacations).toString(), csvRecord[6])

        if (annualWorkSummary.alerts.isEmpty()) {
            assertEquals(EMPTY_ALERTS_MESSAGE, csvRecord[7])
        } else {
            assertEquals(
                annualWorkSummary.alerts.joinToString(ALERT_MESSAGES_SEPARATOR) { it.description },
                csvRecord[7]
            )
        }
    }

}
