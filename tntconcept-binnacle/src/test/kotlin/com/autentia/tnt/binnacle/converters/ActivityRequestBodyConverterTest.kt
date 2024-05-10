package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.entities.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

internal class ActivityRequestBodyConverterTest {

    private val activityRequestBodyConverter = ActivityRequestBodyConverter()

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
            LocalDate.of(2019, Month.DECEMBER, 30).atStartOfDay().plusMinutes(75),
            75,
            "New activity",
            false,
            1,
            false,
            null
        )

        val DUMMY_ORGANIZATION = Organization(1L, "Dummy Organization", 1, listOf())

        val DUMMY_PROJECT = Project(1L, "Dummy Project", open = true, LocalDate.now(), null, null, projectRoles = listOf(), organization = DUMMY_ORGANIZATION, billingType = "NO_BILLABLE")

        val DUMMY_PROJECT_ROLE =
            ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, DUMMY_PROJECT, 0, 0, true, false, TimeUnit.MINUTES)

        val ACTIVITY = Activity(
            1L,
            LocalDate.of(2019, Month.DECEMBER, 30).atStartOfDay(),
            LocalDate.of(2019, Month.DECEMBER, 30).atStartOfDay().plusMinutes(75),
            75,
            "New activity",
            DUMMY_PROJECT_ROLE,
            user.id,
            false,
            1,
            null,
            false,
            ApprovalState.NA
        )

    }

}
