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
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.Month
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
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

    @ParameterizedTest(name = "[${ParameterizedTest.INDEX_PLACEHOLDER}] [{0}]")
    @MethodSource("getAnnualWorkSummaryParametersProvider")
    fun `given user and year should return annual work summary`(
        testDescription: String,
        summaryEntity: Optional<AnnualWorkSummaryEntity>,
        consumedVacations: List<VacationDomain>,
    ) {
        //Given
        val user = createUser()
        val year = 2021

        val vacationsTaken = consumedVacations.filter { VacationState.ACCEPT === it.state }.flatMap { it.days }.size

        doReturn(summaryEntity).whenever(annualWorkSummaryRepository).findById(AnnualWorkSummaryId(user.id, year))

        doReturn(EMPTY_HOLIDAYS).whenever(holidayService)
            .findAllBetweenDate(LocalDate.ofYearDay(year, 1), LocalDate.of(year, Month.DECEMBER, 31))

        doReturn(consumedVacations).whenever(vacationService).getVacationsByChargeYear(year, user)

        doReturn(EARNED_VACATIONS).whenever(timeWorkableService).getEarnedVacationsSinceHiringDate(user, year)

        doReturn(expectedSummary).whenever(annualWorkSummaryConverter)
            .toAnnualWorkSummary(year, EARNED_VACATIONS, vacationsTaken, summaryEntity.orElse(null))

        //When
        val annualWorkSummary = annualWorkSummaryService.getAnnualWorkSummary(user, year)

        //Then
        assertEquals(expectedSummary, annualWorkSummary)
    }

    @ParameterizedTest(name = "[${ParameterizedTest.INDEX_PLACEHOLDER}] [{0}]")
    @MethodSource("createAnnualWorkSummaryParametersProvider")
    fun `should create annual work summary`(
        testDescription: String,
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
                    worked = 1000.toDuration(DurationUnit.HOURS),
                    target = 1200.toDuration(DurationUnit.HOURS),
                    balance = Duration.parse("0h")
                ),
                AnnualBalance(
                    worked = 1000.toDuration(DurationUnit.HOURS),
                    target = 1200.toDuration(DurationUnit.HOURS),
                    notRequestedVacations = Duration.parse("0h"),
                    balance = Duration.parse("0h")
                )
            ),
            emptyMap()
        )

        val workSummaryEntity = AnnualWorkSummaryEntity(
            AnnualWorkSummaryId(user.id, year),
            workedHours = timeSummaryBalance.year.current.worked.toBigDecimalHours(),
            targetHours = timeSummaryBalance.year.current.target.toBigDecimalHours(),
        )

        val expectedSummary = AnnualWorkSummary(
            year,
            workedTime = timeSummaryBalance.year.current.worked,
            targetWorkingTime = timeSummaryBalance.year.current.target,
            earnedVacations = 22,
            consumedVacations = 20
        )

        appProperties.binnacle.workSummary.persistenceEnabled = saveSummary

        doReturn(EMPTY_HOLIDAYS).whenever(holidayService)
            .findAllBetweenDate(LocalDate.ofYearDay(year, 1), LocalDate.of(year, Month.DECEMBER, 31))
        doReturn(consumedVacations).whenever(vacationService).getVacationsByChargeYear(year, user)
        doReturn(EARNED_VACATIONS).whenever(timeWorkableService).getEarnedVacationsSinceHiringDate(user, year)
        doReturn(expectedSummary).whenever(annualWorkSummaryConverter)
            .toAnnualWorkSummary(year, EARNED_VACATIONS, vacationsTaken, workSummaryEntity)

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
        private fun getAnnualWorkSummaryParametersProvider() = listOf(
            Arguments.of(
                "given summary from repository and vacations in different states",
                Optional.of<AnnualWorkSummaryEntity>(mock()),
                getVacationsWithAllStates()
            ),
            Arguments.of(
                "given null summary from repository",
                Optional.ofNullable<AnnualWorkSummaryEntity>(null),
                listOf<VacationDomain>(mock())
            ),
        )

        @JvmStatic
        private fun createAnnualWorkSummaryParametersProvider() = listOf(
            Arguments.of(
                "given save summary flag active and vacations in different states",
                true,
                getVacationsWithAllStates()
            ),
            Arguments.of(
                "given save summary flag inactive and vacations in different states",
                false,
                listOf<VacationDomain>(mock())
            ),
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
