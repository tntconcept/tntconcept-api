package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import io.archimedesfw.commons.time.test.ClockTestUtils
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.LocalDateTime

class AutoBlockProjectUseCaseTest() {
    private val mockNow1 = LocalDateTime.of(2024,3,20,0,0)//normal day
    private val mockNow2 = LocalDateTime.of(2024,3,4,0,0)//weekend
    private val mockNow3 = LocalDateTime.of(2024,5,6,0,0)//holidays
    private val mockNow4 = LocalDateTime.of(2024,5,2,0,0)//holiday second month day
    private val mockNow5 = LocalDateTime.of(2024,4,1,0,0)//normal first day of month
    private val mockNow6 = LocalDateTime.of(2024,4,2,0,0)//normal second workable day of month


    internal val projectRepository = mock<ProjectRepository>()
    internal val holidayRepository = mock<HolidayRepository>()
    private val calendarFactory = CalendarFactory(holidayRepository)

    private val calendarWorkableDaysUseCase = CalendarWorkableDaysUseCase(calendarFactory)
    private val autoBlockProjectUseCase= AutoBlockProjectUseCase(calendarWorkableDaysUseCase,projectRepository)
    @Test
    fun `should return false because mockNow1 is not the second workable day of the month`() {

        var result:Boolean=ClockTestUtils.runWithFixed(mockNow1)
        {
            autoBlockProjectUseCase.isSecondWorkableDayOfMonth()
        }
        assert(result==false)
    }
    @Test
    fun `should return true because mockNow2 is the second workable day of the month but with a weekend between`() {
        var result:Boolean=ClockTestUtils.runWithFixed(mockNow2)
        {
            autoBlockProjectUseCase.isSecondWorkableDayOfMonth()
        }
        assert(result==true)
    }
    @Test
    fun `should return true because mockNow3 is the second workable day of the month but with holidays between`() {
        var result:Boolean=ClockTestUtils.runWithFixed(mockNow3)
        {
            autoBlockProjectUseCase.isSecondWorkableDayOfMonth()
        }
        assert(result==true)
    }
    @Test
    fun `should return false because mockNow4 is the second day of the month but not workable due to holidays`() {
        var result:Boolean=ClockTestUtils.runWithFixed(mockNow4)
        {
            autoBlockProjectUseCase.isSecondWorkableDayOfMonth()
        }
        assert(result==false)
    }
    @Test
    fun `should return false because mockNow5 is the first day of the month`() {
        var result:Boolean=ClockTestUtils.runWithFixed(mockNow5)
        {
            autoBlockProjectUseCase.isSecondWorkableDayOfMonth()
        }
        assert(result==false)
    }
    @Test
    fun `should return true because mockNow6 is the second day of the month and also the second workable day`() {
        var result:Boolean=ClockTestUtils.runWithFixed(mockNow6)
        {
            autoBlockProjectUseCase.isSecondWorkableDayOfMonth()
        }
        assert(result==true)
    }
}