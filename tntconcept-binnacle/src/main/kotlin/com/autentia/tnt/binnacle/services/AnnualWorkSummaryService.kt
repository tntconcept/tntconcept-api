package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.converters.AnnualWorkSummaryConverter
import com.autentia.tnt.binnacle.core.domain.AnnualWorkSummary
import com.autentia.tnt.binnacle.core.domain.TimeSummary
import com.autentia.tnt.binnacle.core.domain.alertvalidators.AnnualWorkSummaryAlertValidators
import com.autentia.tnt.binnacle.core.services.TimeWorkableService
import com.autentia.tnt.binnacle.core.utils.toBigDecimalHours
import com.autentia.tnt.binnacle.entities.AnnualWorkSummaryId
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.repositories.AnnualWorkSummaryRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.util.Optional
import javax.transaction.Transactional

@Singleton
@Transactional
@ReadOnly
internal class AnnualWorkSummaryService(
    private val annualWorkSummaryRepository: AnnualWorkSummaryRepository,
    private val vacationService: VacationService,
    private val timeWorkableService: TimeWorkableService,
    private val annualWorkSummaryConverter: AnnualWorkSummaryConverter,
    private val appProperties: AppProperties,
) {

    fun getAnnualWorkSummary(user: User, year: Int): AnnualWorkSummary {
        val workSummaryEntity = getAnnualWorkSummaryFromRepository(user, year)
        val consumedVacations = getConsumedVacations(year, user)
        val earnedVacations = getEarnedVacations(user, year)
        return buildAnnualWorkSummaryResponse(year, earnedVacations, consumedVacations, workSummaryEntity)
    }

    @Transactional(rollbackOn = [Exception::class])
    fun createAnnualWorkSummary(user: User, year: Int, timeSummaryBalance: TimeSummary): AnnualWorkSummary {
        val workSummaryEntity = com.autentia.tnt.binnacle.entities.AnnualWorkSummary(
            AnnualWorkSummaryId(user.id, year),
            timeSummaryBalance.year.current.worked.toBigDecimalHours(),
            timeSummaryBalance.year.current.target.toBigDecimalHours(),
        )

        if (appProperties.binnacle.workSummary.persistenceEnabled) {
            annualWorkSummaryRepository.saveOrUpdate(workSummaryEntity)
        }

        val consumedVacations = getConsumedVacations(year, user)
        val earnedVacations = getEarnedVacations(user, year)
        val annualWorkSummary = annualWorkSummaryConverter.toAnnualWorkSummary(
            year,
            earnedVacations,
            consumedVacations,
            workSummaryEntity,
        )
        val alerts = getAlerts(annualWorkSummary)
        return annualWorkSummary.copy(alerts = alerts)
    }

    private fun getAlerts(annualWorkSummary: AnnualWorkSummary) =
        AnnualWorkSummaryAlertValidators.values()
            .filter { it.validator.isAlerted(annualWorkSummary) }
            .map { it.alert }

    private fun getAnnualWorkSummaryFromRepository(
        user: User,
        year: Int,
    ): Optional<com.autentia.tnt.binnacle.entities.AnnualWorkSummary> {
        val id = AnnualWorkSummaryId(user.id, year)
        return annualWorkSummaryRepository.findById(id)
    }

    private fun getConsumedVacations(
        year: Int,
        user: User,
    ): Int {
        return vacationService.getVacationsByChargeYear(year, user)
            .filter { VacationState.ACCEPT === it.state }
            .flatMap { it.days }
            .size
    }

    private fun getEarnedVacations(user: User, year: Int) =
        timeWorkableService.getEarnedVacationsSinceHiringDate(
            user,
            year
        )

    private fun buildAnnualWorkSummaryResponse(
        year: Int,
        earnedVacations: Int,
        consumedVacations: Int,
        workSummaryEntity: Optional<com.autentia.tnt.binnacle.entities.AnnualWorkSummary>,
    ) = annualWorkSummaryConverter.toAnnualWorkSummary(
        year,
        earnedVacations,
        consumedVacations,
        workSummaryEntity.orElse(null),
    )

}
