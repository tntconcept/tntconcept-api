package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummaryAlert
import com.autentia.tnt.binnacle.core.domain.TimeSummary
import com.autentia.tnt.binnacle.core.domain.UserAnnualWorkSummary
import com.autentia.tnt.binnacle.services.AnnualWorkSummaryService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.services.WorkSummaryMailService
import com.autentia.tnt.binnacle.services.WorkSummaryReportService
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.io.File
import java.time.LocalDate

internal class CreateAnnualSummaryReportUseCaseTest {

    private val userTimeSummaryUseCase = mock<UserTimeSummaryUseCase>()
    private val userService = mock<UserService>()
    private val annualWorkSummaryService = mock<AnnualWorkSummaryService>()
    private val workSummaryReportService = mock<WorkSummaryReportService>()
    private val workSummaryMailService = mock<WorkSummaryMailService>()
    private val appProperties = AppProperties()

    private val createAnnualSummaryReportUseCase = CreateAnnualSummaryReportUseCase(
        userTimeSummaryUseCase,
        userService,
        annualWorkSummaryService,
        workSummaryReportService,
        workSummaryMailService,
        appProperties
    )

    @ParameterizedTest
    @MethodSource("createAnnualSummaryFromYearParametersProvider")
    fun `given two user hired this year who may have alerts and properties send-alerts-only and show-alerts-only`(
        userAlerts: List<AnnualWorkSummaryAlert>,
        userWithDifferentIdAlerts: List<AnnualWorkSummaryAlert>,
        showAlertsOnly: Boolean,
        sendAlertsOnly: Boolean,
        shouldSendEmail: Boolean,
        userShouldBeInEmail: Boolean,
        userWithDifferentIdShouldBeInEmail: Boolean
    ) {
        //Given
        appProperties.binnacle.workSummary.mail.sendAlertsOnly = sendAlertsOnly
        appProperties.binnacle.workSummary.report.showAlertsOnly = showAlertsOnly

        doReturn(listOf(user, userWithDifferentId)).whenever(userService).findActive()

        val timeSummaryForUser = mock<TimeSummary>()
        val summaryForUser = AnnualWorkSummary(YEAR, alerts = userAlerts)

        doReturn(timeSummaryForUser).whenever(userTimeSummaryUseCase).getTimeSummary(date, user)
        doReturn(summaryForUser).whenever(annualWorkSummaryService)
            .createAnnualWorkSummary(user, YEAR, timeSummaryForUser)

        val timeSummaryForUserWithDifferentId = mock<TimeSummary>()
        val summaryForUserWithDifferentId = AnnualWorkSummary(YEAR, alerts = userWithDifferentIdAlerts)

        doReturn(timeSummaryForUserWithDifferentId).whenever(userTimeSummaryUseCase)
            .getTimeSummary(date, userWithDifferentId)
        doReturn(summaryForUserWithDifferentId).whenever(annualWorkSummaryService)
            .createAnnualWorkSummary(userWithDifferentId, YEAR, timeSummaryForUserWithDifferentId)

        val expectedSummaries = mutableMapOf<Long, UserAnnualWorkSummary>()
        if (userShouldBeInEmail) {
            expectedSummaries[user.id] = UserAnnualWorkSummary(user.toDomain(), summaryForUser)
        }
        if (userWithDifferentIdShouldBeInEmail) {
            expectedSummaries[userWithDifferentId.id] =
                UserAnnualWorkSummary(userWithDifferentId.toDomain(), summaryForUserWithDifferentId)
        }
        val report = mock<File>()
        if (shouldSendEmail) {
            whenever(workSummaryReportService.createReport(YEAR, expectedSummaries)).thenReturn(report)
        }

        //When
        createAnnualSummaryReportUseCase.createAnnualSummaryFromYear(YEAR)

        //Then
        if (shouldSendEmail) {
            verify(workSummaryMailService).sendReport(report)
        } else {
            verify(workSummaryMailService, never()).sendReport(any())
        }
    }

    private companion object {
        private const val YEAR = 2022
        private val date = LocalDate.ofYearDay(YEAR, 1)
        private val user = createUser()
        private val userWithDifferentId = user.copy(id = 3L)

        @JvmStatic
        private fun createAnnualSummaryFromYearParametersProvider() = arrayOf(
            arrayOf(
                listOf(AnnualWorkSummaryAlert("alert")),
                listOf(AnnualWorkSummaryAlert("alert")),
                true, true, true, true, true
            ),
            arrayOf(
                listOf(AnnualWorkSummaryAlert("alert")),
                listOf(AnnualWorkSummaryAlert("alert")),
                false, false, true, true, true
            ),
            arrayOf(
                listOf(AnnualWorkSummaryAlert("alert")),
                emptyList<AnnualWorkSummaryAlert>(),
                true, true, true, true, false
            ),
            arrayOf(
                emptyList<AnnualWorkSummaryAlert>(),
                emptyList<AnnualWorkSummaryAlert>(),
                true, true, false, false, false
            ),
            arrayOf(
                emptyList<AnnualWorkSummaryAlert>(),
                emptyList<AnnualWorkSummaryAlert>(),
                false, false, true, true, true
            ),
            arrayOf(
                listOf(AnnualWorkSummaryAlert("alert")),
                emptyList<AnnualWorkSummaryAlert>(),
                true, false, true, true, false
            ),
            arrayOf(
                emptyList<AnnualWorkSummaryAlert>(),
                emptyList<AnnualWorkSummaryAlert>(),
                false, true, false, true, true
            )
        )

    }

}
