package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

internal class ActivityRequestBodyConverterTest {

    private val activityRequestBodyConverter = ActivityRequestBodyConverter()

    @Test
    fun `given ActivityRequestBodyDTO should return domain ActivityRequestBody with converted values`() {

        val result = activityRequestBodyConverter.mapActivityRequestBodyDTOToActivityRequestBody(
            ACTIVITY_REQUEST_BODY_DTO
        )

        assertEquals(ACTIVITY_REQUEST_BODY, result)
    }


    @Test
    fun `given domain ActivityRequestBody should return domain Activity with converted values`() {

        val result = activityRequestBodyConverter.mapActivityRequestBodyToActivity(ACTIVITY_REQUEST_BODY, DUMMY_PROJECT_ROLE, user)

        assertEquals(ACTIVITY, result)
    }

    private companion object{

        private val user = createUser()

        private val ACTIVITY_REQUEST_BODY = ActivityRequestBody(
            1L,
            LocalDate.of(2019, Month.DECEMBER, 30).atStartOfDay(),
            75,
            "New activity",
            false,
            1,
            false
        )

        private val ACTIVITY_REQUEST_BODY_DTO = ActivityRequestBodyDTO(
            ACTIVITY_REQUEST_BODY.id,
            ACTIVITY_REQUEST_BODY.startDate,
            ACTIVITY_REQUEST_BODY.duration,
            ACTIVITY_REQUEST_BODY.description,
            ACTIVITY_REQUEST_BODY.billable,
            ACTIVITY_REQUEST_BODY.projectRoleId,
            ACTIVITY_REQUEST_BODY.hasImage
        )

        val DUMMY_ORGANIZATION = Organization(1L, "Dummy Organization", listOf())

        val DUMMY_PROJECT = Project(1L, "Dummy Project", open = true, billable = false, projectRoles = listOf(), organization = DUMMY_ORGANIZATION)

        val DUMMY_PROJECT_ROLE = ProjectRole(10L, "Dummy Project role", false, DUMMY_PROJECT, 0)

        val ACTIVITY = Activity(
                1L,
                LocalDate.of(2019, Month.DECEMBER, 30).atStartOfDay(),
                75,
                "New activity",
                DUMMY_PROJECT_ROLE,
                user.id,
                false,
                1,
                null,
                false
        )

    }

}
