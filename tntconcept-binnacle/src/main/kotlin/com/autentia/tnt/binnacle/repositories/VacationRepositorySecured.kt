package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import com.autentia.tnt.security.application.isAdmin
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
internal class VacationRepositorySecured(
    private val vacationDao: VacationDao,
    private val securityService: SecurityService
) : VacationRepository {
    override fun find(startDate: LocalDate, endDate: LocalDate): List<Vacation> {
        val authentication = securityService.checkAuthentication()
        return vacationDao.find(startDate, endDate, authentication.id())
    }

    override fun findWithoutSecurity(startDate: LocalDate, endDate: LocalDate, userId: Long): List<Vacation> {
        //TODO: secure this method!!!
        return vacationDao.find(startDate, endDate, userId)
    }

    override fun findBetweenChargeYears(startYear: LocalDate, endYear: LocalDate): List<Vacation> {
        val authentication = securityService.checkAuthentication()
        return vacationDao.findBetweenChargeYears(startYear, endYear, authentication.id())
    }

    override fun findByChargeYear(chargeYear: LocalDate): List<Vacation> {
        val authentication = securityService.checkAuthentication()
        return vacationDao.findByChargeYear(chargeYear, authentication.id())
    }

    override fun findBetweenChargeYearsWithoutSecurity(
        startYear: LocalDate,
        endYear: LocalDate,
        userId: Long
    ): List<Vacation> {
        //TODO: secure this method!!!
        return vacationDao.findBetweenChargeYears(startYear, endYear, userId)
    }

    override fun findById(vacationId: Long): Vacation? {
        val authentication = securityService.checkAuthentication()
        return if (authentication.isAdmin()) {
            vacationDao.findById(vacationId).orElse(null)
        } else {
            vacationDao.findByIdAndUserId(vacationId, authentication.id())
        }
    }

    override fun save(vacation: Vacation): Vacation {
        val authentication = securityService.checkAuthentication()
        require(vacation.userId == authentication.id()) { "User cannot save vacation" }
        return vacationDao.save(vacation)
    }

    override fun saveAll(vacations: Iterable<Vacation>): Iterable<Vacation> {
        val authentication = securityService.checkAuthentication()
        vacations.forEach { require(it.userId == authentication.id()) { "User cannot save vacation" } }
        return vacationDao.saveAll(vacations)
    }

    override fun update(vacation: Vacation): Vacation {
        val authentication = securityService.checkAuthentication()
        require(vacation.userId == authentication.id()) { "User cannot update vacation" }
        require(vacation.id?.let { findById(it) } != null) { "Vacation to update does not exist" }
        return vacationDao.update(vacation)
    }

    override fun deleteById(vacationId: Long) {
        securityService.checkAuthentication()
        require(findById(vacationId) != null) { "Vacation to delete does not exist" }
        return vacationDao.deleteById(vacationId)
    }
}