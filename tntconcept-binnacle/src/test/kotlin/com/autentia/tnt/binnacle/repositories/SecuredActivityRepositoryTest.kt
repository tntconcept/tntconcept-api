package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.daos.ActivityDao
import com.autentia.tnt.binnacle.entities.Activity
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

internal class SecuredActivityRepositoryTest {

    private val securityService = mock<SecurityService>()
    private val activityDao = mock<ActivityDao>()

    private var securedActivityRepository = SecuredActivityRepository(activityDao, securityService)

    @Test
    fun `get activity by id`() {
        val activityId = 1L
        val activity = Activity(
            id = activityId,
            startDate = today.atTime(10, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasImage = false,
        )
        whenever(activityDao.findByIdAndUserId(activityId, userId)).thenReturn(activity)

        val result = securedActivityRepository.findById(activityId, userId)

        assertEquals(activity, result)
    }

    private companion object {
        private val USER = createUser()
        private val today = LocalDate.now()
        private const val userId = 1L
        private val projectRole = createProjectRole()
        private val authentication = ClientAuthentication(userId.toString(), emptyMap())
    }
}