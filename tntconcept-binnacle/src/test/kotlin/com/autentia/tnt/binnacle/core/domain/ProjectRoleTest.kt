package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime

class ProjectRoleTest {

    private val holidayService = Mockito.mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val calendar = calendarFactory.create(DateInterval.ofYear(2023))

    @Test
    fun `test GetRemainingInUnits in minutes with maxAllowed`() {
        assertEquals(
            60,
            createDomainProjectRole().copy(maxAllowed = 120)
                .getRemainingInUnits(calendar, listOf(createDomainActivity()))
        )
    }

    @Test
    fun `test GetRemainingInUnits in days with maxAllowed`() {
        val dateTime = LocalDateTime.of(2023, 4, 13, 0, 0, 0)
        val dateTimePlusOneDay = LocalDateTime.of(2023, 4, 13, 23, 59, 59)
        val projectRole = createDomainProjectRole().copy(maxAllowed = 960, timeUnit = TimeUnit.DAYS)
        assertEquals(
            1,
            projectRole
                .getRemainingInUnits(
                    calendar,
                    listOf(
                        createDomainActivity().copy(
                            timeInterval = TimeInterval.of(dateTime, dateTimePlusOneDay), projectRole = projectRole
                        )
                    )
                )
        )
    }

    @Test
    fun `test GetRemainingInUnits without maxAllowed`() {
        assertEquals(
            0,
            createDomainProjectRole()
                .getRemainingInUnits(calendar, listOf(createDomainActivity()))
        )
    }
}