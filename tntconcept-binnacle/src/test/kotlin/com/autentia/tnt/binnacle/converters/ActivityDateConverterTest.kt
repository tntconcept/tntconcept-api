package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityDate
import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class ActivityDateConverterTest {

    private val activityDateConverter = ActivityDateConverter(
        WorkableProjectRoleIdChecker(listOf(ProjectRoleId(PROJECT_ROLE_NOT_WORKABLE.id))),
        ActivityResponseConverter(OrganizationResponseConverter(), ProjectResponseConverter(), ProjectRoleResponseConverter())
    )

    @Test
    fun `given ActivityDate should return ActivityDateDTO with converted values`(){
        val date = START_DATE.plusDays(2).toLocalDate()
        val workedMinutes = 100

        val activityDate = ActivityDate(date, workedMinutes, listOf(ACTIVITY_RESPONSE))

        val result = activityDateConverter.toActivityDateDTO(activityDate)

        val activityDateDTO = ActivityDateDTO(date, workedMinutes, listOf(ACTIVITY_RESPONSE_DTO))

        assertEquals(activityDateDTO, result)
    }

    @Test
    fun `given ActivityResponseDTO list should return ActivityDateDTO with converted values`() {

        val firstDay = START_DATE.plusMonths(2)
        val secondDay = firstDay.plusDays(1)
        val lastDay = firstDay.plusDays(2)

        val activitiesFirstDay = listOf(
            ACTIVITY_RESPONSE.copy(id = 1L, duration = 60, startDate = firstDay.plusHours(1)),
            ACTIVITY_RESPONSE.copy(id = 2L, duration = 120, startDate = firstDay.plusHours(5))
        )
        val activitiesSecondDay = listOf(
            ACTIVITY_RESPONSE.copy(id = 3L, duration = 180, startDate = secondDay)
        )
        val activitiesLastDay = listOf(
            ACTIVITY_RESPONSE.copy(id = 4L, duration = 100, startDate = lastDay),
            ACTIVITY_RESPONSE.copy(id = 5L, duration = 100, startDate = lastDay, projectRole = PROJECT_ROLE_NOT_WORKABLE)
        )

        val activitiesDate = activityDateConverter.toListActivityDate(
            activitiesFirstDay + activitiesSecondDay + activitiesLastDay,
            firstDay.toLocalDate(),
            lastDay.toLocalDate()
        )

        val expectedActivitiesDate = listOf(
            ActivityDate(firstDay.toLocalDate(), 180, activitiesFirstDay),
            ActivityDate(secondDay.toLocalDate(), 180, activitiesSecondDay),
            ActivityDate(lastDay.toLocalDate(), 100, activitiesLastDay)
        )

        assertEquals(expectedActivitiesDate, activitiesDate)
    }

    private companion object {
        private const val USER_ID = 1L
        private val START_DATE = LocalDate.now().atStartOfDay().minusYears(1)
        private val ORGANIZATION = Organization(1L, "Dummy Organization", listOf())
        private val PROJECT = Project(1L, "Dummy Project", true, false, ORGANIZATION, listOf())
        private val PROJECT_ROLE = ProjectRole(10L, "Workable Project role", RequireEvidence.NO, PROJECT, 0)
        private val PROJECT_ROLE_NOT_WORKABLE =
            ProjectRole(6L, "Project role not workable", RequireEvidence.WEEKLY, PROJECT, 0)

        private val ACTIVITY_RESPONSE = ActivityResponse(
            1L,
            START_DATE,
            60,
            "Activity",
            PROJECT_ROLE,
            USER_ID,
            true,
            ORGANIZATION,
            PROJECT,
            false,
            ApprovalState.PENDING
        )

        private val ACTIVITY_RESPONSE_DTO = ActivityResponseDTO(
            ACTIVITY_RESPONSE.id,
            ACTIVITY_RESPONSE.startDate,
            ACTIVITY_RESPONSE.duration,
            ACTIVITY_RESPONSE.description,
            ProjectRoleResponseDTO(PROJECT_ROLE.id, PROJECT_ROLE.name, PROJECT_ROLE.requireEvidence),
            ACTIVITY_RESPONSE.userId,
            ACTIVITY_RESPONSE.billable,
            OrganizationResponseDTO(ORGANIZATION.id, ORGANIZATION.name),
            ProjectResponseDTO(PROJECT.id, PROJECT.name, PROJECT.open, PROJECT.billable),
            ACTIVITY_RESPONSE.hasEvidences,
            ACTIVITY_RESPONSE.approvalState
        )
    }

}
