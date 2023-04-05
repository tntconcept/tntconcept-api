package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.converters.AnnualWorkSummaryConverter
import com.autentia.tnt.binnacle.core.domain.*
import com.autentia.tnt.binnacle.core.domain.alertvalidators.AnnualWorkSummaryAlertValidators
import com.autentia.tnt.binnacle.core.services.TimeWorkableService
import com.autentia.tnt.binnacle.core.utils.toBigDecimalHours
import com.autentia.tnt.binnacle.entities.AnnualWorkSummaryId
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.repositories.AnnualWorkSummaryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.Month.DECEMBER
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit.HOURS
import kotlin.time.toDuration
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain
import com.autentia.tnt.binnacle.entities.AnnualWorkSummary as AnnualWorkSummaryEntity

internal class AnnualWorkSummaryServiceTest {

    private val annualWorkSummaryRepository = mock<AnnualWorkSummaryRepository>()
    private val holidayService = mock<HolidayService>()
    private val vacationService = mock<VacationService>()
    private val timeWorkableService = mock<TimeWorkableService>()
    private val annualWorkSummaryConverter = mock<AnnualWorkSummaryConverter>()
    private val appProperties = AppProperties()
    private val expectedSummary = mock<AnnualWorkSummary>()


    private var annualWorkSummaryService = AnnualWorkSummaryService(
        annualWorkSummaryRepository, vacationService, timeWorkableService,
        annualWorkSummaryConverter, appProperties
    )

    @ParameterizedTest
    @MethodSource("getAnnualWorkSummaryParametersProvider")
    fun `get annual work summary`(
        summaryEntity: Optional<AnnualWorkSummaryEntity>,
        consumedVacations: List<VacationDomain>,
    ) {
        //Given
        val user = createUser()
        val year = 2021
        val vacationsTaken = consumedVacations.filter { VacationState.ACCEPT === it.state }.flatMap { it.days }.size
        whenever(annualWorkSummaryRepository.findById(AnnualWorkSummaryId(user.id, year))).thenReturn(summaryEntity)
        whenever(holidayService.findAllBetweenDate(LocalDate.ofYearDay(year, 1), LocalDate.of(year, DECEMBER, 31))).thenReturn(EMPTY_HOLIDAYS)
        whenever(vacationService.getVacationsByChargeYear(year)).thenReturn(consumedVacations)
        whenever(timeWorkableService.getEarnedVacationsSinceHiringDate(user, year)).thenReturn(EARNED_VACATIONS)
        whenever(annualWorkSummaryConverter
            .toAnnualWorkSummary(year, EARNED_VACATIONS, vacationsTaken, summaryEntity.orElse(null))).thenReturn(expectedSummary)

        //When
        val annualWorkSummary = annualWorkSummaryService.getAnnualWorkSummary(user, year)

        //Then
        assertEquals(expectedSummary, annualWorkSummary)
    }

    @ParameterizedTest
    @MethodSource("createAnnualWorkSummaryParametersProvider")
    fun `create annual work summary`(
        saveSummary: Boolean,
        consumedVacations: List<VacationDomain>,
    ) {
        //Given
        val user = createUser()
        val year = 2021
        val vacationsTaken = consumedVacations.filter { VacationState.ACCEPT === it.state }.flatMap { it.days }.size
        val timeSummaryBalance = TimeSummary(
            YearAnnualBalance(
                PreviousAnnualBalance(
                    1000.toDuration(HOURS),
                    1200.toDuration(HOURS),
                    Duration.parse("0h")
                ),
                AnnualBalance(
                    1000.toDuration(HOURS),
                    1200.toDuration(HOURS),
                    Duration.parse("0h"),
                    Duration.parse("0h")
                )
            ),
            emptyMap()
        )

        val workSummaryEntity = AnnualWorkSummaryEntity(
            AnnualWorkSummaryId(user.id, year),
            timeSummaryBalance.year.current.worked.toBigDecimalHours(),
            timeSummaryBalance.year.current.target.toBigDecimalHours(),
        )

        val expectedSummary = AnnualWorkSummary(
            year,
            timeSummaryBalance.year.current.worked,
            timeSummaryBalance.year.current.target,
            22,
            20
        )

        appProperties.binnacle.workSummary.persistenceEnabled = saveSummary

        whenever(holidayService.findAllBetweenDate(LocalDate.ofYearDay(year, 1), LocalDate.of(year, DECEMBER, 31))).thenReturn(EMPTY_HOLIDAYS)
        whenever(vacationService.getVacationsByChargeYear(year)).thenReturn(consumedVacations)
        whenever(timeWorkableService.getEarnedVacationsSinceHiringDate(user, year)).thenReturn(EARNED_VACATIONS)
        whenever(annualWorkSummaryConverter.toAnnualWorkSummary(year, EARNED_VACATIONS, vacationsTaken, workSummaryEntity)).thenReturn(expectedSummary)

        //When
        val annualWorkSummary = annualWorkSummaryService.createAnnualWorkSummary(user, year, timeSummaryBalance)

        //Then
        if (saveSummary)
            verify(annualWorkSummaryRepository).saveOrUpdate(workSummaryEntity)
        else
            verify(annualWorkSummaryRepository, never()).saveOrUpdate(any())

        assertEquals(
            expectedSummary.copy(alerts = AnnualWorkSummaryAlertValidators.values().map { it.alert }),
            annualWorkSummary
        )
    }

    private companion object {
        @JvmStatic
        private fun getAnnualWorkSummaryParametersProvider() = arrayOf(
            arrayOf(Optional.of<AnnualWorkSummaryEntity>(mock()), getVacationsWithAllStates()),
            arrayOf(Optional.ofNullable<AnnualWorkSummaryEntity>(null), listOf<VacationDomain>(mock())),
        )

        @JvmStatic
        private fun createAnnualWorkSummaryParametersProvider() = arrayOf(
            arrayOf(true, getVacationsWithAllStates()),
            arrayOf(false, listOf<VacationDomain>(mock())),
        )

        private const val EARNED_VACATIONS = 22

        private val EMPTY_HOLIDAYS = listOf(mock<Holiday>())

        private fun getVacationsWithAllStates() = VacationState.values().map {
            VacationDomain(
                state = it,
                startDate = LocalDate.now(),
                endDate = LocalDate.now(),
                days = listOf(LocalDate.now()),
                chargeYear = LocalDate.now()
            )
        }.toList()
    }
}
