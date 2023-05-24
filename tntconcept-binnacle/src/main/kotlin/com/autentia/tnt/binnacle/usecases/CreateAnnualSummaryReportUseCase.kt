package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import com.autentia.tnt.binnacle.core.domain.UserAnnualWorkSummary
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.services.AnnualWorkSummaryService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.services.WorkSummaryMailService
import com.autentia.tnt.binnacle.services.WorkSummaryReportService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class CreateAnnualSummaryReportUseCase internal constructor(
    private val userTimeSummaryUseCase: UserTimeSummaryUseCase,
    private val userService: UserService,
    private val annualWorkSummaryService: AnnualWorkSummaryService,
    private val workSummaryReportService: WorkSummaryReportService,
    private val workSummaryMailService: WorkSummaryMailService,
    private val appProperties: AppProperties,
) {
    fun createAnnualSummaryFromYear(year: Int) {
        val date = LocalDate.ofYearDay(year, 1)
        val summaries = mutableMapOf<Long, UserAnnualWorkSummary>()
        userService.getActiveUsersWithoutSecurity()
            .filter { isUserHiredInYear(it, year) }
            .forEach { user ->
                val workingTime = userTimeSummaryUseCase.getTimeSummary(date, user)
                val summary = annualWorkSummaryService.createAnnualWorkSummary(user, year, workingTime)
                if (isShouldBeShown(summary)) {
                    summaries[user.id] = UserAnnualWorkSummary(user.toDomain(), summary)
                }
            }


        if (isShouldBeSent(summaries)) {
            val report = workSummaryReportService.createReport(year, summaries)
            workSummaryMailService.sendReport(report)
        }
    }

    private fun isShouldBeShown(summary: AnnualWorkSummary) =
        !appProperties.binnacle.workSummary.report.showAlertsOnly || summary.alerts.isNotEmpty()

    private fun isShouldBeSent(summaries: Map<Long, UserAnnualWorkSummary>) =
        !appProperties.binnacle.workSummary.mail.sendAlertsOnly
                || summaries.values.any { it.summary.alerts.isNotEmpty() }

    private fun isUserHiredInYear(user: User, year: Int) =
        user.hiringDate.year <= year

}
