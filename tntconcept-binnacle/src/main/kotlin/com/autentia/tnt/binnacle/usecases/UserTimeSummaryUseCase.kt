package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.converters.TimeSummaryConverter
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.TimeSummary
import com.autentia.tnt.binnacle.core.services.TimeSummaryService
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.TimeSummaryDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.AnnualWorkSummaryService
import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.binnacle.services.MyVacationsDetailService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.services.VacationService
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.Month

@Singleton
class UserTimeSummaryUseCase internal constructor(
    private val userService: UserService,
    private val holidayService: HolidayService,
    private val annualWorkSummaryService: AnnualWorkSummaryService,
    private val activityService: ActivityService,
    private val vacationService: VacationService,
    private val myVacationsDetailService: MyVacationsDetailService,
    private val timeSummaryService: TimeSummaryService,
    private val activityResponseConverter: ActivityResponseConverter,
    private val timeSummaryConverter: TimeSummaryConverter
) {
    fun getTimeSummary(date: LocalDate): TimeSummaryDTO {
        val user: User = userService.getAuthenticatedUser()
        val timeSummary = getTimeSummary(date, user)
        return timeSummaryConverter.toTimeSummaryDTO(timeSummary)
    }

    fun getTimeSummary(date: LocalDate, user: User): TimeSummary {
        val startYearDate = LocalDate.of(date.year, Month.JANUARY, 1)
        val endYearDate = LocalDate.of(date.year, Month.DECEMBER, 31)


        val annualWorkSummary = annualWorkSummaryService.getAnnualWorkSummary(user, startYearDate.year - 1)
        val holidays: List<Holiday> = holidayService.findAllBetweenDate(startYearDate, endYearDate)
        val holidaysDates = holidays.map { it.date.toLocalDate() }

        val vacationsRequestedThisYear = vacationService.getVacationsBetweenDates(startYearDate, endYearDate, user)
            .filter { it.isRequestedVacation() }

        val vacationDaysRequestedThisYear =
            vacationsRequestedThisYear.flatMap { it.days }.filter { it.year == startYearDate.year }

        val vacationsChargedThisYear = vacationService.getVacationsByChargeYear(startYearDate.year, user)

        val correspondingVacations =
            myVacationsDetailService.getCorrespondingVacationDaysSinceHiringDate(user, startYearDate.year)

        val activities = activityService.getActivitiesBetweenDates(
            DateInterval.of(startYearDate, endYearDate)
        ).map(Activity::toDomain)

        val previousActivities = activityService.getActivitiesBetweenDates(
            DateInterval.of(startYearDate.minusYears(1), endYearDate.minusYears(1))
        ).map(Activity::toDomain)

        return timeSummaryService.getTimeSummaryBalance(
            date,
            user,
            annualWorkSummary,
            holidaysDates,
            vacationDaysRequestedThisYear,
            vacationsChargedThisYear,
            correspondingVacations,
            activities,
            previousActivities
        )
    }
}
