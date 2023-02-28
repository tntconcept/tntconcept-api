package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.converters.TimeSummaryConverter
import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import com.autentia.tnt.binnacle.core.domain.MonthlyRoles
import com.autentia.tnt.binnacle.core.domain.Vacation
import com.autentia.tnt.binnacle.core.domain.TimeSummary
import com.autentia.tnt.binnacle.core.utils.toBigDecimalHours
import com.autentia.tnt.binnacle.entities.User
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import kotlin.time.Duration

class TimeSummaryService(
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
        vacationsRequestedThisYear: List<LocalDate>,
        vacationsChargedThisYear: List<Vacation>,
        correspondingVacations: Int,
        activities: List<Activity>,
        previousActivities : List<Activity>
    ): TimeSummary {
        val annualPrevisionWorkingTime = getWorkingPrevision(date.year, user, publicHolidays)

        val annualTargetWork = targetWorkService.getAnnualTargetWork(
            date.year,
            user.hiringDate,
            annualPrevisionWorkingTime,
            user.getAnnualWorkingHoursByYear(date.year),
            annualWorkSummary
        )

        val previousAnnualTargetWork = targetWorkService.getAnnualTargetWork(
            date.year - 1,
            user.hiringDate,
            annualPrevisionWorkingTime,
            user.getAnnualWorkingHoursByYear(date.year - 1),
            annualWorkSummary
        )

        val workedTimeByMonth = getWorked(activities)
        val previousWorkedTimeByMonth = getWorked(previousActivities)

        val monthlyWorkingTime = timeWorkableService.getMonthlyWorkingTime(
            date.year,
            user.hiringDate,
            publicHolidays,
            vacationsRequestedThisYear
        )

        val suggestWorkingTimeByMonth = getRecommendedWork(
            date.year,
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
            }.count { it.year == date.year }

        }


        val notConsumedVacations = Duration.parse(((correspondingVacations - consumedVacationsDays)*8).toString() + "h")
        val consumedVacations = vacationsChargedThisYear.filter { it.isRequestedVacation() }

        val roles = mutableMapOf<Month, List<MonthlyRoles>>()

        activities.groupBy { it.date.month }.map { month ->
            val groupByRole = month.value.groupBy { it.projectRole.id }
                .mapValues { role ->
                    role.value.sumOf { it.duration.toBigDecimalHours() }
                }

            roles[month.key] = groupByRole.map { MonthlyRoles(it.key, Duration.parse("${it.value}h")) }
        }

        return timeSummaryConverter.toTimeSummary(
            workedTimeByMonth,
            annualTargetWork,
            suggestWorkingTimeByMonth,
            notConsumedVacations,
            workableMonthlyHoursList,
            roles,
            previousAnnualTargetWork,
            previousWorkedTimeByMonth,
            consumedVacations
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

    private fun getWorked(activities: List<Activity>): Map<Month, Duration> {
        return workedTimeService.workedTime(activities)
    }

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
