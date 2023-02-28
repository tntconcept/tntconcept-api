package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.core.utils.isHoliday
import com.autentia.tnt.binnacle.core.utils.isWeekend
import com.autentia.tnt.binnacle.core.utils.myDatesUntil
import com.autentia.tnt.binnacle.entities.User
import java.time.LocalDate
import java.time.Month
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TimeWorkableService {
    fun getAnnualPrevisionWorkingTime(
        year: Int,
        user: User,
        publicHolidaysInYear: List<LocalDate>,
    ): Duration {
        val totalWorkableTime = getTotalWorkableTime(year, user.hiringDate, publicHolidaysInYear)
        val vacations = getEarnedVacationsSinceHiringDate(user, year)
        return getWorkableTime(totalWorkableTime, vacations)
    }

    fun getMonthlyWorkingTime(
        year: Int,
        hiringDate: LocalDate,
        publicHolidaysInYear: List<LocalDate>,
        vacationsRequested: List<LocalDate>,
    ): Map<Month, Duration> {
        val initDate = getInitDate(year, hiringDate)
        val lastDayOfYear = LocalDate.of(year, Month.DECEMBER, 31)
        return getWorkableTimeBetweenDatesGroupedByMonth(
            initDate,
            lastDayOfYear,
            publicHolidaysInYear,
            vacationsRequested
        )
    }

    fun getEarnedVacationsSinceHiringDate(
        user: User,
        year: Int,
    ): Int {

        val adjustedVacationsOnYearAgreement = user.getAgreementTermsByYear(year).vacation
        return when {
            year < user.hiringDate.year -> 0
            year == user.hiringDate.year -> {
                val ratio = adjustedVacationsOnYearAgreement / 360.0
                val dayContract = user.hiringDate.dayOfYear
                val workedDays = 360 - dayContract
                val vacationDays = workedDays * ratio
                return vacationDays.roundToInt()
            }
            else -> adjustedVacationsOnYearAgreement
        }
    }

    private fun getWorkableTime(workableTime: Duration, vacations: Int): Duration {
        return workableTime - (vacations * 8).toDuration(DurationUnit.HOURS)
    }

    private fun getTotalWorkableTime(year: Int, hiringDate: LocalDate, holidaysInYear: List<LocalDate>): Duration {
        val initDate = getInitDate(year, hiringDate)
        val lastDayOfYear = LocalDate.of(year, Month.DECEMBER, 31)
        return getWorkableTimeBetweenDates(initDate, lastDayOfYear, holidaysInYear, emptyList())
    }

    private fun getInitDate(year: Int, hiringDate: LocalDate): LocalDate {
        val limitDate = LocalDate.of(year, Month.JANUARY, 1)
        return if (hiringDate > limitDate) hiringDate else limitDate
    }

    private fun getWorkableTimeBetweenDates(
        beginDate: LocalDate,
        finalDate: LocalDate,
        holidaysBetweenDates: List<LocalDate>,
        vacations: List<LocalDate>,
    ): Duration {
        return getWorkableTimeBetweenDatesGroupedByMonth(beginDate, finalDate, holidaysBetweenDates, vacations).values
            .fold(Duration.ZERO, Duration::plus)
    }

    private fun getWorkableTimeBetweenDatesGroupedByMonth(
        beginDate: LocalDate,
        finalDate: LocalDate,
        holidaysBetweenDates: List<LocalDate>,
        vacations: List<LocalDate>,
    ): Map<Month, Duration> {
        val totalAnnualMonths = 12

        val workableTime = beginDate.myDatesUntil(finalDate)
            .filterNot { it.isWeekend() || it.isHoliday(holidaysBetweenDates + vacations) }
            .groupBy { it.month }
            .mapValues { (it.value.size * 8).toDuration(DurationUnit.HOURS) }

        return if (workableTime.size != totalAnnualMonths) {
            getWorkableHoursInAMonth(workableTime)
        } else {
            workableTime
        }


    }

    private fun getWorkableHoursInAMonth(
        workingTime: Map<Month, Duration>
    ): Map<Month, Duration> {
        val monthlyWorkingTime =
            mutableMapOf(
                Month.JANUARY to Duration.parse("0h"),
                Month.FEBRUARY to Duration.parse("0h"),
                Month.MARCH to Duration.parse("0h"),
                Month.APRIL to Duration.parse("0h"),
                Month.MAY to Duration.parse("0h"),
                Month.JUNE to Duration.parse("0h"),
                Month.JULY to Duration.parse("0h"),
                Month.AUGUST to Duration.parse("0h"),
                Month.SEPTEMBER to Duration.parse("0h"),
                Month.OCTOBER to Duration.parse("0h"),
                Month.NOVEMBER to Duration.parse("0h"),
                Month.DECEMBER to Duration.parse("0h"),
            )

        val mutableWorkingTime = workingTime as MutableMap<Month, Duration>

        monthlyWorkingTime.map { (key) ->
            if (mutableWorkingTime[key] == null) {
                mutableWorkingTime[key] = Duration.parse("0h")
            }
        }
        return mutableWorkingTime.toSortedMap()
    }

}
