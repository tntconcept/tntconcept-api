package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.repositories.ProjectRepository
import io.archimedesfw.commons.time.test.ClockTestUtils
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.time.LocalDate
import java.time.LocalDateTime

class AutoBlockProjectUseCaseTest() {
    private val mockNow1 = LocalDateTime.of(2024,3,20,0,0)//normal day
    private val mockNow2 = LocalDateTime.of(2024,3,4,0,0)//weekend
    private val mockNow3 = LocalDateTime.of(2024,5,6,0,0)//holidays
    private val mockNow4 = LocalDateTime.of(2024,5,2,0,0)//holiday second month day
    private val mockNow5 = LocalDateTime.of(2024,4,1,0,0)//normal first day of month
    private val mockNow6 = LocalDateTime.of(2024,4,2,0,0)//normal second workable day of month


    internal val projectRepository = mock<ProjectRepository>()
    private val calendarWorkableDaysUseCase = mock<CalendarWorkableDaysUseCase>()
    private val autoBlockProjectUseCase = AutoBlockProjectUseCase(calendarWorkableDaysUseCase,projectRepository)
    @Test
    fun `should not call the project repository because mockNow1 is not the second workable day of the month`() {

        `when`(calendarWorkableDaysUseCase.get(mockNow1.withDayOfMonth(1).toLocalDate(),mockNow1.toLocalDate())).thenReturn(14)
        ClockTestUtils.runWithFixed(mockNow1)
        {
            autoBlockProjectUseCase.blockOpenProjectsOnSecondDayOfMonth()
        }
        verifyNoInteractions(projectRepository)

    }

    @Test
    fun `should call projectRepository with date 2024-2-29 because mockNow2 is the second workable day of the month with a weekend between`() {
        `when`(calendarWorkableDaysUseCase.get(mockNow2.withDayOfMonth(1).toLocalDate(),mockNow2.toLocalDate())).thenReturn(2)
        ClockTestUtils.runWithFixed(mockNow2)
        {
            autoBlockProjectUseCase.blockOpenProjectsOnSecondDayOfMonth()
        }

        verify(projectRepository, times(1)).blockOpenProjects(anyOrNull())
        verify(projectRepository).blockOpenProjects(LocalDate.of(2024,2,29))
    }
    @Test
    fun `should call projectRepository with date 2024-4-30 because mockNow3 is the second workable day of the month with holidays between`() {
        `when`(calendarWorkableDaysUseCase.get(mockNow3.withDayOfMonth(1).toLocalDate(),mockNow3.toLocalDate())).thenReturn(2)
        ClockTestUtils.runWithFixed(mockNow3)
        {
            autoBlockProjectUseCase.blockOpenProjectsOnSecondDayOfMonth()
        }
        verify(projectRepository, times(1)).blockOpenProjects(anyOrNull())
        verify(projectRepository).blockOpenProjects(LocalDate.of(2024,4,30))

    }
    @Test
    fun `should not call the project repository because mockNow4 is the second day of the month but not workable due to holidays`() {
        `when`(calendarWorkableDaysUseCase.get(mockNow4.withDayOfMonth(1).toLocalDate(),mockNow4.toLocalDate())).thenReturn(0)
        ClockTestUtils.runWithFixed(mockNow4)
        {
            autoBlockProjectUseCase.blockOpenProjectsOnSecondDayOfMonth()
        }
        verifyNoInteractions(projectRepository)
    }
    @Test
    fun `should not call the project repository because mockNow5 is the first day of the month`() {
        `when`(calendarWorkableDaysUseCase.get(mockNow5.withDayOfMonth(1).toLocalDate(), mockNow5.toLocalDate())).thenReturn(1)
        ClockTestUtils.runWithFixed(mockNow5)
        {
            autoBlockProjectUseCase.blockOpenProjectsOnSecondDayOfMonth()
        }
        verifyNoInteractions(projectRepository)

    }
    @Test
    fun `should call projectRepository with date 2024-3-31 because mockNow6 is the second day of the month and also the second workable day`() {
        `when`(calendarWorkableDaysUseCase.get(mockNow6.withDayOfMonth(1).toLocalDate(),mockNow6.toLocalDate())).thenReturn(2)
        ClockTestUtils.runWithFixed(mockNow6)
        {
            autoBlockProjectUseCase.blockOpenProjectsOnSecondDayOfMonth()
        }
        verify(projectRepository, times(1)).blockOpenProjects(anyOrNull())
        verify(projectRepository).blockOpenProjects(LocalDate.of(2024,3,31))
    }
}