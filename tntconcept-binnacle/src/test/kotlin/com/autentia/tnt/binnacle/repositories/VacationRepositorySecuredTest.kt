package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.*

class VacationRepositorySecuredTest {
    private val vacationDao = mock<VacationDao>()
    private val securityService = mock<SecurityService>()
    private val vacationRepositorySecured = VacationRepositorySecured(vacationDao, securityService)

    @Test
    fun `find vacations between dates when not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { vacationRepositorySecured.find(startDate, endDate) }
    }

    @Test
    fun `find vacations between dates when user`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))

        vacationRepositorySecured.find(startDate, endDate)

        verify(vacationDao).find(startDate, endDate, userId)
    }

    @Test
    fun `find vacation by id when not authenticated`() {
        val vacationId = 1L
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { vacationRepositorySecured.findById(vacationId) }
    }

    @Test
    fun `find vacation by id when user`() {
        val vacationId = 1L
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))

        vacationRepositorySecured.findById(vacationId)

        verify(vacationDao).findByIdAndUserId(vacationId, userId)
        verify(vacationDao, never()).findById(vacationId)
    }

    @Test
    fun `find vacation by id when admin`() {
        val vacationId = 1L
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationAdmin))

        vacationRepositorySecured.findById(vacationId)

        verify(vacationDao).findById(vacationId)
        verify(vacationDao, never()).findByIdAndUserId(vacationId, adminUserId)
    }


    @Test
    fun `filter vacations charge year when user not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            vacationRepositorySecured.findBetweenChargeYears(
                startDate,
                endDate
            )
        }
    }

    @Test
    fun `filter vacations charge year when user`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))

        vacationRepositorySecured.findBetweenChargeYears(startDate, endDate)

        verify(vacationDao).findBetweenChargeYears(startDate, endDate, userId)
    }

    @Test
    fun `save all when user not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { vacationRepositorySecured.saveAll(emptyList()) }
    }

    @Test
    fun `save all when user not matching`() {
        val vacations = listOf(
            Vacation(
                id = null,
                startDate = startDate,
                endDate = endDate,
                state = VacationState.ACCEPT,
                userId = userId,
                description = "Test vacation",
                chargeYear = LocalDate.ofYearDay(2023, 1)
            ),
            Vacation(
                id = null,
                startDate = startDate.plusDays(7),
                endDate = endDate.plusDays(7),
                state = VacationState.ACCEPT,
                userId = adminUserId,
                description = "Test vacation 2",
                chargeYear = LocalDate.ofYearDay(2023, 1)

            )
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))

        assertThrows<IllegalArgumentException> { vacationRepositorySecured.saveAll(vacations) }
    }

    @Test
    fun `save all when user matching`() {
        val vacations = listOf(
            Vacation(
                id = null,
                startDate = startDate,
                endDate = endDate,
                state = VacationState.ACCEPT,
                userId = userId,
                description = "Test vacation",
                chargeYear = LocalDate.ofYearDay(2023, 1)
            ),
            Vacation(
                id = null,
                startDate = startDate.plusDays(7),
                endDate = endDate.plusDays(7),
                state = VacationState.ACCEPT,
                userId = userId,
                description = "Test vacation 2",
                chargeYear = LocalDate.ofYearDay(2023, 1)
            )
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))

        vacationRepositorySecured.saveAll(vacations)

        verify(vacationDao).saveAll(vacations)
    }


    @Test
    fun `update vacation when not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { vacationRepositorySecured.update(mock()) }
    }

    @Test
    fun `update vacation when activity does not exists`() {
        val vacationId = 1L
        val vacation = Vacation(
            id = vacationId,
            startDate = startDate,
            endDate = endDate,
            state = VacationState.ACCEPT,
            userId = userId,
            description = "Test vacation",
            chargeYear = LocalDate.ofYearDay(2023, 1)
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        whenever(vacationDao.findById(vacationId)).thenReturn(Optional.empty())

        assertThrows<IllegalArgumentException> { vacationRepositorySecured.update(vacation) }
    }

    @Test
    fun `update vacation when user`() {
        val vacationId = 1L
        val vacation = Vacation(
            id = vacationId,
            startDate = startDate,
            endDate = endDate,
            state = VacationState.ACCEPT,
            userId = userId,
            description = "Test vacation",
            chargeYear = LocalDate.ofYearDay(2024, 1)
        )
        whenever(vacationDao.findByIdAndUserId(vacationId, userId)).thenReturn(vacation)
        whenever(vacationDao.update(vacation)).thenReturn(vacation)
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))

        vacationRepositorySecured.update(vacation)

        verify(vacationDao).update(vacation)
    }

    @Test
    fun `update vacation when other user`() {
        val vacation = Vacation(
            id = 1L,
            startDate = startDate,
            endDate = endDate,
            state = VacationState.ACCEPT,
            userId = userId,
            description = "Test vacation",
            chargeYear = LocalDate.ofYearDay(2024, 1)
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationAdmin))

        assertThrows<IllegalArgumentException> { vacationRepositorySecured.update(vacation) }
    }

    @Test
    fun `delete vacation when not authenticated`() {
        val vacationId = 1L
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { vacationRepositorySecured.deleteById(vacationId) }
    }

    @Test
    fun `delete vacation when user`() {
        val vacationId = 1L
        val vacation = Vacation(
            id = 1L,
            startDate = startDate,
            endDate = endDate,
            state = VacationState.ACCEPT,
            userId = userId,
            description = "Test vacation",
            chargeYear = LocalDate.ofYearDay(2024, 1)
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        whenever(vacationDao.findByIdAndUserId(vacationId, userId)).thenReturn(vacation)

        vacationRepositorySecured.deleteById(vacationId)

        verify(vacationDao).deleteById(vacationId)
    }

    @Test
    fun `delete vacation when other user`() {
        val vacationId = 1L
        val vacation = Vacation(
            id = 1L,
            startDate = startDate,
            endDate = endDate,
            state = VacationState.ACCEPT,
            userId = userId,
            description = "Test vacation",
            chargeYear = LocalDate.ofYearDay(2024, 1)
        )
        whenever(vacationDao.findByIdAndUserId(vacationId, userId)).thenReturn(vacation)
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationAdmin))

        assertThrows<IllegalArgumentException> { vacationRepositorySecured.deleteById(vacationId) }
    }

    private companion object {
        private val startDate = LocalDate.of(2023, 3, 28)
        private val endDate = LocalDate.of(2023, 3, 29)
        private const val userId = 1L
        private const val adminUserId = 3L
        private val authenticationAdmin =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("admin")))
        private val authenticationUser = ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))
    }
}