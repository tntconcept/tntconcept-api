package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.Month
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class WorkedTimeServiceTest {

    private lateinit var workableProjectRoleIdChecker: WorkableProjectRoleIdChecker

    private lateinit var sut: WorkedTimeService

    @BeforeEach
    fun setUp() {
        workableProjectRoleIdChecker = mock()
        sut = WorkedTimeService(workableProjectRoleIdChecker)
    }

    @Test
    fun `should return worked time excluding not workable project roles time`() {
        val duration = 8.toDuration(DurationUnit.HOURS)
        val workableProjectRoleId = ProjectRoleId(1)
        val notWorkableProjectRoleId = ProjectRoleId(2)
        val activities = listOf(
            Activity(duration, LocalDateTime.parse("2021-01-01T10:00:00"), workableProjectRoleId),
            Activity(duration, LocalDateTime.parse("2021-01-02T10:00:00"), workableProjectRoleId),
            Activity(duration, LocalDateTime.parse("2021-02-01T10:00:00"), workableProjectRoleId),
            Activity(duration, LocalDateTime.parse("2021-02-03T10:00:00"), notWorkableProjectRoleId)
        )
        whenever(workableProjectRoleIdChecker.isWorkable(workableProjectRoleId)).thenReturn(true)
        whenever(workableProjectRoleIdChecker.isWorkable(notWorkableProjectRoleId)).thenReturn(false)

        val expectedResult: Map<Month, Duration> =
            mapOf(
                Month.JANUARY to 16.toDuration(DurationUnit.HOURS),
                Month.FEBRUARY to 8.toDuration(DurationUnit.HOURS),
            )

        val workedTime = sut.workedTime(activities)

        assertEquals(expectedResult, workedTime)
    }


}
