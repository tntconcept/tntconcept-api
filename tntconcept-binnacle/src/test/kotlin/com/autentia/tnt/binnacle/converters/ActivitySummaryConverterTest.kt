package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.core.domain.ActivitySummary
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

internal class ActivitySummaryConverterTest {

    private val activitySummaryConverter = ActivitySummaryConverter(
        WorkableProjectRoleIdChecker(listOf(ProjectRoleId(PROJECT_ROLE_NOT_WORKABLE.id)))
    )

    @Test
    fun `given ActivitySummary should return ActivitySummaryDTO with converted values` (){
        val date = START_DATE.plusDays(2).toLocalDate()
        val workedMinutes = 10.0

        val activitySummary = ActivitySummary(date, workedMinutes)

        val result = activitySummaryConverter.toActivitySummaryDTO(activitySummary)

        val activitySummaryDTO = ActivitySummaryDTO(date, workedMinutes)

        assertEquals(activitySummaryDTO, result)
    }

    @Test
    fun `given ActivityResponse list with timeUnit DAYS should return ActivitySummary with converted values`() {

        val firstDay = START_DATE.plusMonths(2)
        val secondDay = firstDay.plusDays(1)
        val thirdDay = firstDay.plusDays(2)
        val lastDay = firstDay.plusDays(3)

        val activitiesFirstDay = listOf(
            ACTIVITY_RESPONSE.copy(id = 1L, duration = 60, start = firstDay.plusHours(1), end = thirdDay, projectRole = PROJECT_ROLE_DAYS),
            ACTIVITY_RESPONSE.copy(id = 2L, duration = 240, start = firstDay.plusHours(5), end = firstDay.plusHours(9))
        )

        val activitiesSummary = activitySummaryConverter.toListActivitySummaryDate(
            activitiesFirstDay,
            firstDay.toLocalDate(),
            lastDay.toLocalDate()
        )

        val expectedActivities = listOf(
            ActivitySummary(firstDay.toLocalDate(), 12.0),
            ActivitySummary(secondDay.toLocalDate(), 8.0),
            ActivitySummary(thirdDay.toLocalDate(), 8.0),
            ActivitySummary(lastDay.toLocalDate(), 0.0)
        )

        assertEquals(expectedActivities, activitiesSummary)
    }

    private companion object {
        private const val USER_ID = 1L
        private val START_DATE = LocalDate.now().atStartOfDay().minusYears(1)
        private val END_DATE = LocalDate.now().atStartOfDay().minusYears(1)
        private val ORGANIZATION = Organization(1L, "Dummy Organization", listOf())
        private val PROJECT = Project(1L, "Dummy Project", true, false, ORGANIZATION, listOf())
        private val PROJECT_ROLE_MINUTES =
            ProjectRole(10L, "Workable Project role", RequireEvidence.NO, PROJECT, 0, true, false, TimeUnit.MINUTES)
        private val PROJECT_ROLE_DAYS =
            ProjectRole(10L, "Workable Project role", RequireEvidence.NO, PROJECT, 0, true, false, TimeUnit.DAYS)
        private val PROJECT_ROLE_NOT_WORKABLE =
            ProjectRole(6L, "Project role not workable", RequireEvidence.WEEKLY,
                PROJECT, 0, false, false, TimeUnit.MINUTES)

        private val ACTIVITY_RESPONSE = ActivityResponse(
            1L,
            START_DATE,
            END_DATE,
            60,
            "Activity",
            PROJECT_ROLE_MINUTES,
            USER_ID,
            true,
            ORGANIZATION,
            PROJECT,
            false,
            ApprovalState.PENDING
        )
    }

}