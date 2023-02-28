package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.services.TimeWorkableService
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.VacationState
import jakarta.inject.Singleton
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain

@Singleton
internal class MyVacationsDetailService(val timeWorkableService: TimeWorkableService) {

    fun getCorrespondingVacationDaysSinceHiringDate(user: User, year: Int): Int {
        return timeWorkableService.getEarnedVacationsSinceHiringDate(user, year)
    }

    fun getRemainingVacations(year: Int, vacations: List<VacationDomain>, user: User): Int {
        val requestedVacations = vacations
            .filter { it.state === VacationState.ACCEPT || it.state === VacationState.PENDING }
            .fold(0) { acc, privateHolidayDTO -> acc + privateHolidayDTO.days.size }

        val correspondingVacations =
            getCorrespondingVacationDaysSinceHiringDate(user, year)

        return correspondingVacations - requestedVacations
    }

}
