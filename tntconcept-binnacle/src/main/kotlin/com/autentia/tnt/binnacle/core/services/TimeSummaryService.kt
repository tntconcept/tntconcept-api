package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.converters.TimeSummaryConverter
import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.TimeSummary
import com.autentia.tnt.binnacle.core.domain.Vacation
import com.autentia.tnt.binnacle.entities.User
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import kotlin.time.Duration

internal class TimeSummaryService(
    private val targetWorkService: TargetWorkService,
    private val timeWorkableService: TimeWorkableService,
    private val workedTimeService: WorkedTimeService,
    private val workRecommendationService: WorkRecommendationService,
    private val timeSummaryConverter: TimeSummaryConverter
) {

    fun getTimeSummaryBalance(
        date: LocalDate,
        user: User,
        annualWorkSummary: AnnualWorkSummary,
        publicHolidays: List<LocalDate>,
        vacationsConsumedThisYear: List<LocalDate>,
        vacationsChargedThisYear: List<Vacation>,
        correspondingVacations: Int,
        activities: List<Activity>,
        previousActivities : List<Activity>
    ): TimeSummary {

        val year = date.year
        val previousYear = date.year - 1

        val yearInterval = DateInterval.ofYear(year)
        val previousYearInterval = DateInterval.ofYear(previousYear)

        val annualPrevisionWorkingTime = getWorkingPrevision(year, user, publicHolidays)

        val annualTargetWork = targetWorkService.getAnnualTargetWork(
            date.year,
            user.hiringDate,
            annualPrevisionWorkingTime,
            user.getAnnualWorkingHoursByYear(year),
            annualWorkSummary
        )

        val previousAnnualTargetWork = targetWorkService.getAnnualTargetWork(
            date.year - 1,
            user.hiringDate,
            annualPrevisionWorkingTime,
            user.getAnnualWorkingHoursByYear(previousYear),
            annualWorkSummary
        )

        val workedTimeByMonth = getWorked(yearInterval, activities)

        val previousWorkedTimeByMonth = getWorked(previousYearInterval, previousActivities)

        val monthlyWorkingTime = timeWorkableService.getMonthlyWorkingTime(
            year, user.hiringDate, publicHolidays, vacationsConsumedThisYear
        )

        val suggestWorkingTimeByMonth = getRecommendedWork(
            year,
            date.monthValue,
            user,
            annualTargetWork,
            workedTimeByMonth,
            monthlyWorkingTime
        )

        val workableMonthlyHoursList: List<Duration> =
            monthlyWorkingTime.values.map {
                Duration.parse(it.toIsoString())
            }

        var consumedVacationsDays = 0
        if (vacationsChargedThisYear.isNotEmpty()) {
            consumedVacationsDays = vacationsChargedThisYear.filter { it.isRequestedVacation() }.flatMap {
                it.days
            }.count { it.year == year }

        }

        val notConsumedVacations =
            Duration.parse(((correspondingVacations - consumedVacationsDays) * 8).toString() + "h")
        val vacationsChargedThisYear = vacationsChargedThisYear.filter { it.isRequestedVacation() }


        val roles = getWorkedByRoles(yearInterval, activities)

        return timeSummaryConverter.toTimeSummary(
            workedTimeByMonth,
            annualTargetWork,
            suggestWorkingTimeByMonth,
            notConsumedVacations,
            workableMonthlyHoursList,
            roles,
            previousAnnualTargetWork,
            previousWorkedTimeByMonth,
            vacationsChargedThisYear,
            vacationsConsumedThisYear
        )
    }

    private fun getWorkingPrevision(
        year: Int,
        user: User,
        publicHolidays: List<LocalDate>
    ): Duration {
        return timeWorkableService.getAnnualPrevisionWorkingTime(
            year,
            user,
            publicHolidays
        )
    }

    private fun getWorked(dateInterval: DateInterval, activities: List<Activity>) =
        workedTimeService.workedTime(dateInterval, activities)


    private fun getWorkedByRoles(dateInterval: DateInterval, activities: List<Activity>) =
        workedTimeService.getWorkedTimeByRoles(dateInterval, activities)

    private fun getRecommendedWork(
        year: Int,
        month: Int,
        user: User,
        annualTargetWork: Duration,
        workedTimeByMonth: Map<Month, Duration>,
        monthlyWorkingTime: Map<Month, Duration>
    ): Map<Month, Duration> {

        return workRecommendationService.suggestWorkingTimeByMonth(
            YearMonth.of(year, month),
            user.hiringDate,
            annualTargetWork,
            workedTimeByMonth,
            monthlyWorkingTime
        )
    }


}
