package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class CalendarDaysForProjectRoleUseCaseTest {

    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val projectRoleRepository = mock<ProjectRoleRepository>()

    private val useCase = CalendarDaysForProjectRoleUseCase(calendarFactory, projectRoleRepository)

    @Test
    fun `get project role info`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 13)
        val roleId = 1L
        val projectRole = createProjectRole(roleId)

        whenever(projectRoleRepository.findById(roleId)).thenReturn(projectRole)

        useCase.get(startDate, endDate, roleId)

        verify(projectRoleRepository).findById(roleId)
    }

    @Test
    fun `should throw ProjectRoleNotFoundException if projectRole is not found`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 13)
        val roleId = 1L

        assertThrows<ProjectRoleNotFoundException> { useCase.get(startDate, endDate, roleId) }
    }

    @Test
    fun `should get workable days if projectRole has workable time unit`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 13)
        val roleId = 1L
        val projectRole = createProjectRole(roleId).copy(timeUnit = TimeUnit.DAYS)

        whenever(holidayService.findAllBetweenDate(startDate, endDate)).thenReturn(emptyList())
        whenever(projectRoleRepository.findById(roleId)).thenReturn(projectRole)

        val result = useCase.get(startDate, endDate, roleId)

        assertEquals(4, result)
    }

    @Test
    fun `should get workable days without taking into account holidays and weekend if projectRole has workable time unit`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 16)
        val roleId = 1L
        val projectRole = createProjectRole(roleId).copy(timeUnit = TimeUnit.DAYS)

        whenever(holidayService.findAllBetweenDate(startDate, endDate)).thenReturn(
            listOf(Holiday(1L, "holiday", startDate.atStartOfDay()))
        )
        whenever(projectRoleRepository.findById(roleId)).thenReturn(projectRole)

        val result = useCase.get(startDate, endDate, roleId)

        assertEquals(4, result)
    }

    @Test
    fun `should get all days between dates if projectRole has natural days time unit`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 16)
        val roleId = 1L
        val projectRole = createProjectRole(roleId).copy(timeUnit = TimeUnit.NATURAL_DAYS)

        whenever(holidayService.findAllBetweenDate(startDate, endDate)).thenReturn(
            listOf(Holiday(1L, "holiday", startDate.atStartOfDay()))
        )
        whenever(projectRoleRepository.findById(roleId)).thenReturn(projectRole)

         val result = useCase.get(startDate, endDate, roleId)

        assertEquals(7, result)
    }

    @Test
    fun `should return zero days if projectRole has minutes time unit`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 16)
        val roleId = 1L
        val projectRole = createProjectRole(roleId).copy(timeUnit = TimeUnit.MINUTES)

        whenever(holidayService.findAllBetweenDate(startDate, endDate)).thenReturn(
            listOf(Holiday(1L, "holiday", startDate.atStartOfDay()))
        )
        whenever(projectRoleRepository.findById(roleId)).thenReturn(projectRole)

         val result = useCase.get(startDate, endDate, roleId)

        assertEquals(0, result)
    }
}