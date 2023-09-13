package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.converters.VacationConverter
import com.autentia.tnt.binnacle.core.domain.Calendar
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.RequestVacation
import com.autentia.tnt.binnacle.core.domain.Vacation
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.repositories.VacationRepository
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
internal class RemainingVacationService(
    private val vacationRepository: VacationRepository,
    private val myVacationsDetailService: MyVacationsDetailService,
    private val vacationConverter: VacationConverter,
    private val calendarFactory: CalendarFactory,
) {

    private fun getVacationsWithWorkableDays(vacations: List<com.autentia.tnt.binnacle.entities.Vacation>): List<Vacation> {
        return if (vacations.isEmpty()) {
            emptyList()
        } else {
            val start: LocalDate? = vacations.minOfOrNull(com.autentia.tnt.binnacle.entities.Vacation::startDate)
            val end: LocalDate? = vacations.maxOfOrNull(com.autentia.tnt.binnacle.entities.Vacation::endDate)
            val dateInterval = DateInterval.of(start!!, end!!)
            val calendar = calendarFactory.create(dateInterval)
            getVacationsWithWorkableDays(calendar, vacations)
        }
    }

    private fun getVacationsWithWorkableDays(calendar: Calendar, vacations: List<com.autentia.tnt.binnacle.entities.Vacation>): List<Vacation> =
        vacations.map {
            val days = calendar.getWorkableDays(DateInterval.of(it.startDate, it.endDate))
            vacationConverter.toVacationDomain(it, days)
        }

    private fun getVacationsByChargeYear(
        chargeYear: LocalDate
    ): List<Vacation> {
        val vacations = vacationRepository.findByChargeYear(chargeYear)
        return getVacationsWithWorkableDays(vacations)
    }

    fun getRemainingVacations(chargeYear: Int, user: User) : Int {
        val vacations = getVacationsByChargeYear(LocalDate.of(chargeYear, 1, 1))
        return myVacationsDetailService
            .getRemainingVacations(chargeYear, vacations, user)
    }

    fun getRequestedVacationsSelectedYear(
        lastYearFirstDay: LocalDate,
        nextYearLastDay: LocalDate,
        requestVacation: RequestVacation
    ): List<LocalDate> {
        val calendar = calendarFactory.create(DateInterval.of(lastYearFirstDay, nextYearLastDay))
        return calendar.getWorkableDays(DateInterval.of(requestVacation.startDate, requestVacation.endDate))
    }

}