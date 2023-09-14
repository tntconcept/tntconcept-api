package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.converters.VacationConverter
import com.autentia.tnt.binnacle.core.domain.*
import com.autentia.tnt.binnacle.core.utils.maxDate
import com.autentia.tnt.binnacle.core.utils.minDate
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.exception.NoMoreDaysLeftInYearException
import com.autentia.tnt.binnacle.repositories.VacationRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.Month
import javax.transaction.Transactional
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain

@Singleton
internal class VacationService(
    private val vacationRepository: VacationRepository,
    private val vacationConverter: VacationConverter,
    private val calendarFactory: CalendarFactory,
    private val remainingVacationService: RemainingVacationService
) {
    @Transactional
    @ReadOnly
    fun getVacationsBetweenDates(beginDate: LocalDate, finalDate: LocalDate): List<VacationDomain> {
        val vacations: List<Vacation> = vacationRepository.find(beginDate, finalDate)
        return getVacationsWithWorkableDays(vacations)
    }

    @Transactional
    @ReadOnly
    fun getVacationsBetweenDates(beginDate: LocalDate, finalDate: LocalDate, user: User): List<VacationDomain> {
        val vacations: List<Vacation> = vacationRepository.findWithoutSecurity(beginDate, finalDate, user.id)
        return getVacationsWithWorkableDays(vacations)
    }

    @Transactional
    @ReadOnly
    fun getVacationsByChargeYear(chargeYear: Int): List<VacationDomain> {
        val vacations: List<Vacation> = vacationRepository.findBetweenChargeYears(
            LocalDate.of(chargeYear, 1, 1),
            LocalDate.of(chargeYear, 1, 1)
        )
        return getVacationsWithWorkableDays(vacations)
    }

    @Transactional
    @ReadOnly
    fun getVacationsByChargeYear(chargeYear: Int, user: User): List<VacationDomain> {
        val vacations: List<Vacation> = vacationRepository.findBetweenChargeYearsWithoutSecurity(
            LocalDate.of(chargeYear, 1, 1),
            LocalDate.of(chargeYear, 1, 1),
            user.id
        )
        return getVacationsWithWorkableDays(vacations)
    }

    fun getVacationsWithWorkableDays(vacations: List<Vacation>): List<VacationDomain> {
        return if (vacations.isEmpty()) {
            emptyList()
        } else {
            val start: LocalDate? = vacations.minOfOrNull(Vacation::startDate)
            val end: LocalDate? = vacations.maxOfOrNull(Vacation::endDate)
            val dateInterval = DateInterval.of(start!!, end!!)
            val calendar = calendarFactory.create(dateInterval)
            getVacationsWithWorkableDays(calendar, vacations)
        }
    }

    private fun getVacationsWithWorkableDays(calendar: Calendar, vacations: List<Vacation>): List<VacationDomain> =
        vacations.map {
            val days = calendar.getWorkableDays(DateInterval.of(it.startDate, it.endDate))
            vacationConverter.toVacationDomain(it, days)
        }

    @Transactional
    fun createVacationPeriod(requestVacation: RequestVacation, user: User): CreateVacationResponse {

        val currentYear = requestVacation.chargeYear

        val currentYearRemainingVacations = remainingVacationService
            .getRemainingVacations(requestVacation.chargeYear, user)

        val selectedDays = remainingVacationService.getRequestedVacationsSelectedYear(requestVacation)

        if (selectedDays.isNotEmpty() &&
            currentYearRemainingVacations < selectedDays.size) {
            throw NoMoreDaysLeftInYearException()
        }

        val vacationPeriod = chargeDaysIntoYear(selectedDays, currentYear, currentYearRemainingVacations)

        val vacationToSave = Vacation(
            id = null,
            startDate = vacationPeriod.startDate,
            endDate = vacationPeriod.endDate,
            description = requestVacation.description.orEmpty(),
            state = VacationState.PENDING,
            userId = user.id,
            departmentId = user.departmentId,
            observations = "",
            chargeYear = LocalDate.of(vacationPeriod.chargeYear, Month.JANUARY, 1)
        )

        vacationRepository.save(vacationToSave)

        return vacationPeriod
    }

    @Transactional
    fun updateVacationPeriod(
        requestVacation: RequestVacation,
        user: User,
        vacation: Vacation
    ): CreateVacationResponse {

        val calendar = calendarFactory.create(
            DateInterval.of(
                minDate(vacation.startDate, requestVacation.startDate),
                maxDate(vacation.endDate, requestVacation.endDate)
            )
        )

        val oldCorrespondingDays = calendar.getWorkableDays(DateInterval.of(vacation.startDate, vacation.endDate)).size
        val newCorrespondingDays =
            calendar.getWorkableDays(DateInterval.of(requestVacation.startDate, requestVacation.endDate)).size

        if (oldCorrespondingDays == newCorrespondingDays) {
            val newPeriod = vacation.copy(
                startDate = requestVacation.startDate,
                endDate = requestVacation.endDate,
                description = requestVacation.description.orEmpty()
            )

            val savedPeriod = vacationRepository.update(newPeriod)

            return CreateVacationResponse(
                    startDate = savedPeriod.startDate,
                    endDate = savedPeriod.endDate,
                    days = oldCorrespondingDays,
                    chargeYear = savedPeriod.chargeYear.year
            )
        }

        // Delete the request period first
        vacationRepository.deleteById(vacation.id!!)

        return createVacationPeriod(requestVacation, user)
    }

    fun chargeDaysIntoYear(
        selectedDays: List<LocalDate>,
        year: Int,
        remainingHolidays: Int
    ): CreateVacationResponse {
        return CreateVacationResponse(
                startDate = selectedDays.first(),
                endDate = selectedDays.last(),
                days = selectedDays.size,
                chargeYear = year
            )
    }

}
