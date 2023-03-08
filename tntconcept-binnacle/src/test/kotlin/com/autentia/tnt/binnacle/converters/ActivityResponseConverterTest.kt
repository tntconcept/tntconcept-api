package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class ActivityResponseConverterTest {

    private val organizationResponseConverter = mock<OrganizationResponseConverter>()
    private val projectResponseConverter = mock<ProjectResponseConverter>()
    private val projectRoleResponseConverter = mock<ProjectRoleResponseConverter>()

    private var activityResponseConverter = ActivityResponseConverter(organizationResponseConverter, projectResponseConverter, projectRoleResponseConverter)


    @Test
    fun `given domain Activity should return ActivityResponseDTO with converted values`() {

        doReturn(DUMMY_ORGANIZATION_DTO).whenever(organizationResponseConverter).toOrganizationResponseDTO(
            DUMMY_ORGANIZATION
        )

        doReturn(DUMMY_PROJECT_ROLE_DTO).whenever(projectRoleResponseConverter).toProjectRoleResponseDTO(
            DUMMY_PROJECT_ROLE
        )

        doReturn(DUMMY_PROJECT_DTO).whenever(projectResponseConverter).toProjectResponseDTO(DUMMY_PROJECT)

        val result = activityResponseConverter.mapActivityToActivityResponseDTO(DUMMY_ACTIVITY)

        assertEquals(DUMMY_ACTIVITY_DTO, result)

    }

    @Test
    fun `given domain Activity should return ActivityResponse with converted values`() {

        val result = activityResponseConverter.mapActivityToActivityResponse(DUMMY_ACTIVITY)

        assertEquals(DUMMY_ACTIVITY_RESPONSE, result)
    }

    @Test
    fun `given list ActivityResponse should return list ActivityResponseDTO with converted values`() {

        val listActivityResponse = listOf(DUMMY_ACTIVITY_RESPONSE)

        val listActivityResponseDTO = listOf(DUMMY_ACTIVITY_DTO)

        doReturn(DUMMY_PROJECT_ROLE_DTO).whenever(projectRoleResponseConverter).toProjectRoleResponseDTO(
            DUMMY_PROJECT_ROLE
        )

        doReturn(DUMMY_ORGANIZATION_DTO).whenever(organizationResponseConverter).toOrganizationResponseDTO(
            DUMMY_ORGANIZATION
        )

        doReturn(DUMMY_PROJECT_DTO).whenever(projectResponseConverter).toProjectResponseDTO(DUMMY_PROJECT)

        val result = listActivityResponse.map { activityResponseConverter.toActivityResponseDTO(it) }

        assertEquals(listActivityResponseDTO, result)
    }

    @Test
    fun `given ActivityResponse should return domain Activity with converted values`() {

        //When
        val activity = activityResponseConverter.toActivity(DUMMY_ACTIVITY_RESPONSE)

        //Then
        assertEquals(DUMMY_ACTIVITY_RESPONSE.startDate, activity.date)
        assertEquals(DUMMY_ACTIVITY_RESPONSE.duration.toDuration(DurationUnit.MINUTES), activity.duration)
        assertEquals(DUMMY_ACTIVITY_RESPONSE.projectRole.id, activity.projectRole.id)
    }

    private companion object{

        private val DUMMY_ORGANIZATION = Organization(1L, "Dummy Organization", listOf())
        private val DUMMY_PROJECT = Project(1L, "Dummy Project", true, false, DUMMY_ORGANIZATION, listOf())
        private val DUMMY_PROJECT_ROLE = ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, DUMMY_PROJECT, 0)

        private val DUMMY_ORGANIZATION_DTO = OrganizationResponseDTO(1L, "Dummy Organization")
        private val DUMMY_PROJECT_ROLE_DTO = ProjectRoleResponseDTO(10L, "Dummy Project role", RequireEvidence.NO)
        private val DUMMY_PROJECT_DTO = ProjectResponseDTO(1L, "Dummy Project", true, false)

        val DUMMY_ACTIVITY = Activity(
            id = 1L,
            startDate = LocalDate.of(2019, Month.DECEMBER, 30).atStartOfDay(),
            duration = 75,
            description = "Dummy activity",
            userId = 1,
            billable = false,
            departmentId = 1,
            hasImage = false,
            projectRole = DUMMY_PROJECT_ROLE,
            approvalState = ApprovalState.NA
        )

        val DUMMY_ACTIVITY_RESPONSE = ActivityResponse(
            DUMMY_ACTIVITY.id!!,
            DUMMY_ACTIVITY.startDate,
            DUMMY_ACTIVITY.duration,
            DUMMY_ACTIVITY.description,
            DUMMY_PROJECT_ROLE,
            DUMMY_ACTIVITY.userId,
            DUMMY_ACTIVITY.billable,
            DUMMY_ORGANIZATION,
            DUMMY_PROJECT,
            DUMMY_ACTIVITY.hasImage,
            DUMMY_ACTIVITY.approvalState
        )

        val DUMMY_ACTIVITY_DTO = ActivityResponseDTO(
            DUMMY_ACTIVITY.id!!,
            DUMMY_ACTIVITY.startDate,
            DUMMY_ACTIVITY.duration,
            DUMMY_ACTIVITY.description,
            DUMMY_PROJECT_ROLE_DTO,
            DUMMY_ACTIVITY.userId,
            DUMMY_ACTIVITY.billable,
            DUMMY_ORGANIZATION_DTO,
            DUMMY_PROJECT_DTO,
            DUMMY_ACTIVITY.hasImage,
            DUMMY_ACTIVITY.approvalState
        )


    }

}
