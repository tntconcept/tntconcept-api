package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.converters.VacationConverter
import com.autentia.tnt.binnacle.core.domain.*
import com.autentia.tnt.binnacle.core.utils.maxDate
import com.autentia.tnt.binnacle.core.utils.minDate
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.exception.MaxNextYearRequestVacationException
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
    private val myVacationsDetailService: MyVacationsDetailService,
    private val vacationConverter: VacationConverter,
    private val calendarFactory: CalendarFactory,
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
    fun createVacationPeriod(requestVacation: RequestVacation, user: User): MutableList<CreateVacationResponse> {

        val currentYear = LocalDate.now().year
        val lastYear = currentYear - 1
        val nextYear = currentYear + 1

        val lastYearFirstDay = LocalDate.of(lastYear, Month.JANUARY, 1)
        val nextYearLastDay = LocalDate.of(nextYear, Month.DECEMBER, 31)

        val vacationsByYear: Map<Int, List<VacationDomain>> =
            getVacationsByYear(lastYearFirstDay, nextYearLastDay)

        val lastYearRemainingVacations = myVacationsDetailService
            .getRemainingVacations(lastYear, vacationsByYear.getOrElse(lastYear) { listOf() }, user)
        val currentYearRemainingVacations = myVacationsDetailService
            .getRemainingVacations(currentYear, vacationsByYear.getOrElse(currentYear) { listOf() }, user)
        val nextYearRemainingVacations = myVacationsDetailService
            .getRemainingVacations(nextYear, vacationsByYear.getOrElse(nextYear) { listOf() }, user)

        var selectedDays = getRequestedVacationsSelectedYear(lastYearFirstDay, nextYearLastDay, requestVacation)

        val remainingHolidaysLastAndCurrentYear = lastYearRemainingVacations + currentYearRemainingVacations

        val vacationPeriods = mutableListOf<CreateVacationResponse>()

        when {
            remainingHolidaysLastAndCurrentYear >= selectedDays.size -> {
                if (lastYearRemainingVacations > 0) {
                    vacationPeriods += chargeDaysIntoYear(selectedDays, lastYear, lastYearRemainingVacations)
                    selectedDays = selectedDays.drop(lastYearRemainingVacations)
                }

                if (currentYearRemainingVacations > 0 && selectedDays.isNotEmpty()) {
                    vacationPeriods += chargeDaysIntoYear(selectedDays, currentYear, currentYearRemainingVacations)
                }
            }

            else -> {
                if (currentYearRemainingVacations > 0) {
                    vacationPeriods += chargeDaysIntoYear(selectedDays, currentYear, currentYearRemainingVacations)
                    selectedDays = selectedDays.drop(currentYearRemainingVacations)
                }

                if (cantRequestPeriodUsingVacationDaysOfNextYear(
                        selectedDays.size,
                        nextYearRemainingVacations,
                        user.getAgreementTermsByYear(nextYear).vacation
                    )
                ) {
                    throw MaxNextYearRequestVacationException("You can't charge more than 5 days of the next year vacations in the current year")
                } else if (nextYearRemainingVacations > 0 && selectedDays.isNotEmpty()) {
                    vacationPeriods += chargeDaysIntoYear(selectedDays, nextYear, nextYearRemainingVacations)
                }
            }
        }

        val vacationsToSave = vacationPeriods.map {
            Vacation(
                id = null,
                startDate = it.startDate,
                endDate = it.endDate,
                description = requestVacation.description.orEmpty(),
                state = VacationState.PENDING,
                userId = user.id,
                departmentId = user.departmentId,
                observations = "",
                chargeYear = LocalDate.of(it.chargeYear, Month.JANUARY, 1)
            )
        }

        vacationRepository.saveAll(vacationsToSave)

        return vacationPeriods
    }

    private fun getRequestedVacationsSelectedYear(
        lastYearFirstDay: LocalDate,
        nextYearLastDay: LocalDate,
        requestVacation: RequestVacation
    ): List<LocalDate> {
        val calendar = calendarFactory.create(DateInterval.of(lastYearFirstDay, nextYearLastDay))
        return calendar.getWorkableDays(DateInterval.of(requestVacation.startDate, requestVacation.endDate))
    }

    private fun getVacationsByYear(
        lastYearFirstDay: LocalDate,
        nextYearLastDay: LocalDate
    ): Map<Int, List<com.autentia.tnt.binnacle.core.domain.Vacation>> {
        val vacations = vacationRepository.findBetweenChargeYears(lastYearFirstDay, nextYearLastDay)
        return getVacationsWithWorkableDays(vacations).groupBy { it.chargeYear.year }
    }

    @Transactional
    fun updateVacationPeriod(
        requestVacation: RequestVacation,
        user: User,
        vacation: Vacation
    ): MutableList<CreateVacationResponse> {

        val calendar = calendarFactory.create(
            DateInterval.of(
                minDate(vacation.startDate, requestVacation.startDate),
                maxDate(vacation.endDate, requestVacation.endDate)
            )
        )

        val oldCorrespondingDays = calendar.getWorkableDays(DateInterval.of(vacation.startDate, vacation.endDate)).size
        val newCorrespondingDays =
            calendar.getWorkableDays(DateInterval.of(requestVacation.startDate, requestVacation.endDate)).size

        var vacationPeriods = mutableListOf<CreateVacationResponse>()

        if (oldCorrespondingDays == newCorrespondingDays) {
            val newPeriod = vacation.copy(
                startDate = requestVacation.startDate,
                endDate = requestVacation.endDate,
                description = requestVacation.description.orEmpty()
            )

            val savedPeriod = vacationRepository.update(newPeriod)

            vacationPeriods.plusAssign(
                CreateVacationResponse(
                    startDate = savedPeriod.startDate,
                    endDate = savedPeriod.endDate,
                    days = oldCorrespondingDays,
                    chargeYear = savedPeriod.chargeYear.year
                )
            )
        } else {
            // Delete the request period first
            vacationRepository.deleteById(vacation.id!!)

            vacationPeriods = createVacationPeriod(requestVacation, user)
        }

        return vacationPeriods
    }

    fun cantRequestPeriodUsingVacationDaysOfNextYear(
        selectedDays: Int,
        nextYearRemainingHolidays: Int,
        holidaysQuantity: Int
    ): Boolean {
        val maxVacationDaysOfNextYearToCharge = 5
        val alreadyRequested5DaysInNextYear =
            nextYearRemainingHolidays <= holidaysQuantity - maxVacationDaysOfNextYearToCharge
        val days = nextYearRemainingHolidays - (holidaysQuantity - maxVacationDaysOfNextYearToCharge)

        return alreadyRequested5DaysInNextYear || selectedDays > days
    }

    fun chargeDaysIntoYear(
        selectedDays: List<LocalDate>,
        year: Int,
        remainingHolidays: Int
    ): CreateVacationResponse {
        return if (remainingHolidays > selectedDays.size) {
            CreateVacationResponse(
                startDate = selectedDays[0],
                endDate = selectedDays[selectedDays.size - 1],
                days = selectedDays.size,
                chargeYear = year
            )
        } else {
            CreateVacationResponse(
                startDate = selectedDays[0],
                endDate = selectedDays[remainingHolidays - 1],
                days = remainingHolidays,
                chargeYear = year
            )
        }
    }

    @Transactional
    fun deleteVacationPeriod(id: Long, userId: Long) {
        vacationRepository.deleteById(id)
    }
}
