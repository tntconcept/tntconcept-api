package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.converters.VacationConverter
import com.autentia.tnt.binnacle.core.domain.CreateVacationResponse
import com.autentia.tnt.binnacle.core.domain.RequestVacation
import com.autentia.tnt.binnacle.core.utils.isHoliday
import com.autentia.tnt.binnacle.core.utils.isWeekend
import com.autentia.tnt.binnacle.core.utils.myDatesUntil
import com.autentia.tnt.binnacle.entities.Holiday
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
    private val holidayService: HolidayService,
    private val myVacationsDetailService: MyVacationsDetailService,
    private val vacationConverter: VacationConverter
) {
    @Transactional
    @ReadOnly
    fun getVacationsBetweenDates(beginDate: LocalDate, finalDate: LocalDate, user: User): List<VacationDomain> {
        val vacations: List<Vacation> = vacationRepository.getVacationsBetweenDate(beginDate, finalDate, user.id)
        var holidays: List<Holiday> = emptyList()
        if (vacations.isNotEmpty()) {
            holidays = holidayService.findAllBetweenDate(beginDate, finalDate)
        }
        return filterVacationsWithHolidays(vacations, holidays)
    }

    @Transactional
    @ReadOnly
    fun getVacationsByChargeYear(chargeYear: Int, user: User): List<VacationDomain> {
        val vacations: List<Vacation> = vacationRepository.filterBetweenChargeYears(
            LocalDate.of(chargeYear, 1, 1),
            LocalDate.of(chargeYear, 1, 1),
            user.id
        )

        var holidays: List<Holiday> = emptyList()
        if (vacations.isNotEmpty()) {
            val minStartDate = vacations.minOfOrNull(Vacation::startDate)!!
            val maxEndDate = vacations.maxOfOrNull(Vacation::endDate)!!

            holidays = holidayService.findAllBetweenDate(minStartDate, maxEndDate)
        }

        return filterVacationsWithHolidays(vacations, holidays)
    }

    @Transactional
    fun createVacationPeriod(requestVacation: RequestVacation, user: User): MutableList<CreateVacationResponse> {
        val currentYear = LocalDate.now().year
        val lastYear = currentYear - 1
        val nextYear = currentYear + 1

        val holidays = getHolidaysBetweenLastAndNextYear()
        val vacations = vacationRepository.filterBetweenChargeYears(
            LocalDate.of(lastYear, Month.JANUARY, 1),
            LocalDate.of(nextYear, Month.DECEMBER, 31),
            user.id
        )

        val vacationsByYear: Map<Int, List<VacationDomain>> = filterVacationsWithHolidays(vacations, holidays)
            .groupBy { it.chargeYear.year }

        val lastYearRemainingVacations = myVacationsDetailService
            .getRemainingVacations(lastYear, vacationsByYear.getOrElse(lastYear) { listOf() }, user)
        val currentYearRemainingVacations = myVacationsDetailService
            .getRemainingVacations(currentYear, vacationsByYear.getOrElse(currentYear) { listOf() }, user)
        val nextYearRemainingVacations = myVacationsDetailService
            .getRemainingVacations(nextYear, vacationsByYear.getOrElse(nextYear) { listOf() }, user)


        var selectedDays = getVacationPeriodDays(requestVacation.startDate, requestVacation.endDate, holidays)

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

    private fun filterVacationsWithHolidays(vacations: List<Vacation>, holidays: List<Holiday>) =
        vacations.map { privateHoliday ->
            val days = getVacationPeriodDays(privateHoliday.startDate, privateHoliday.endDate, holidays)
            vacationConverter.toVacationDomain(privateHoliday, days)
        }

    @Transactional
    fun updateVacationPeriod(requestVacation: RequestVacation, user: User, vacation: Vacation): MutableList<CreateVacationResponse> {
        val holidays = this.getHolidaysBetweenLastAndNextYear()

        val oldCorrespondingDays = this.getVacationPeriodDays(vacation.startDate, vacation.endDate, holidays).size
        val newCorrespondingDays = this.getVacationPeriodDays(requestVacation.startDate, requestVacation.endDate, holidays).size

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

    fun getHolidaysBetweenLastAndNextYear(): List<Holiday> {
        val currentYear = LocalDate.now().year
        val lastYearFirstDay = LocalDate.of(currentYear - 1, Month.JANUARY, 1)
        val nextYearLastDay = LocalDate.of(currentYear + 1, Month.DECEMBER, 31)

        return holidayService.findAllBetweenDate(lastYearFirstDay, nextYearLastDay)
    }

    @Transactional
    fun deleteVacationPeriod(id: Long, userId: Long) {
        vacationRepository.deleteById(id)
    }

    fun getVacationPeriodDays(beginDate: LocalDate, finalDate: LocalDate, holidays: List<Holiday>): List<LocalDate> {
        val holidaysDates = holidays.map { it.date.toLocalDate() }
        return beginDate
            .myDatesUntil(finalDate)
            .filterNot { it.isWeekend() || it.isHoliday(holidaysDates) }
    }
}
