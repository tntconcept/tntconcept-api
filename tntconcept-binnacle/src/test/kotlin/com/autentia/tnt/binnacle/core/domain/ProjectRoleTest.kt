package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
import com.autentia.tnt.binnacle.config.createProjectRoleTimeInfo
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.LocalDateTime

class ProjectRoleTest {

    private val holidayRepository = mock<HolidayRepository>()
    private val calendarFactory = CalendarFactory(holidayRepository)
    private val calendar = calendarFactory.create(DateInterval.ofYear(2023))

    @Test
    fun `test GetRemainingInUnits in minutes with maxAllowed`() {
        assertEquals(
            60,
            createDomainProjectRole().copy(timeInfo = createProjectRoleTimeInfo(maxTimeAllowedByYear = 120))
                .getRemainingInUnits(calendar, listOf(createDomainActivity()))
        )
    }

    @Test
    fun `test GetRemainingInUnits in days with maxAllowed`() {
        val dateTime = LocalDateTime.of(2023, 4, 13, 0, 0, 0)
        val dateTimePlusOneDay = LocalDateTime.of(2023, 4, 13, 23, 59, 59)
        val projectRole = createDomainProjectRole().copy(timeInfo = TimeInfo(MaxTimeAllowed(960, 0) , TimeUnit.DAYS))
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