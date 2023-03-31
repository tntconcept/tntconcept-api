package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.daos.ActivityDao
import com.autentia.tnt.binnacle.entities.Activity
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

internal class ActivityRepositorySecuredTest {

    private val securityService = mock<SecurityService>()
    private val activityDao = mock<ActivityDao>()

    private var activityRepositorySecured = ActivityRepositorySecured(activityDao, securityService)

    @Test
    fun `find activity should throw illegal state exception`() {
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
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { activityRepositorySecured.findById(activityId) }
    }

    @Test
    fun `find activity by id for user with admin role permission`() {
        val activityId = 1L
        val activity = Activity(
            id = activityId,
            startDate = today.atTime(10, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = 2L,
            billable = false,
            hasImage = false,
        )
        whenever(activityDao.findById(activityId)).thenReturn(Optional.of(activity))
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))

        val result = activityRepositorySecured.findById(activityId)

        assertEquals(activity, result)
    }

    @Test
    fun `find activity by id for user without admin role permission should return null`() {
        val activityId = 1L

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))

        val result = activityRepositorySecured.findById(activityId)

        assertNull(result)
        verify(activityDao).findByIdAndUserId(activityId, userId)
    }

    @Test
    fun `find worked minutes should retrieve authenticated user worked minutes`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val workedTime = listOf(ActivityTimeOnly(startDate, 60, projectRole.id))

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(activityDao.workedMinutesBetweenDate(startDate, endDate, userId)).thenReturn(workedTime)

        val result: List<ActivityTimeOnly> = activityRepositorySecured.findWorkedMinutes(
            startDate, endDate
        )

        assertEquals(workedTime, result)
    }

    @Test
    fun `find worked minutes should throw IllegalStateException if there is not logged user`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val workedTime = listOf(ActivityTimeOnly(startDate, 60, projectRole.id))

        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityRepositorySecured.findWorkedMinutes(
                startDate, endDate, userId
            )
        }
    }

    @Test
    fun `find activities between dates should retrieve user logged activities`() {
        val otherUserId = 2L
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val activities = listOf(
            Activity(
                id = 1L,
                startDate = today.atTime(10, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasImage = false,
            )
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))
        whenever(activityDao.getActivitiesBetweenDate(startDate, endDate, adminUserId)).thenReturn(activities)

        val result: List<Activity> = activityRepositorySecured.find(
            startDate, endDate
        )

        assertEquals(activities, result)
    }

    @Test
    fun `find activities should throw IllegalStateException`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val activities = listOf(
            Activity(
                id = 1L,
                startDate = today.atTime(10, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasImage = false,
            )
        )

        whenever(securityService.authentication).thenReturn(Optional.empty())
        whenever(activityDao.getActivitiesBetweenDate(startDate, endDate, userId)).thenReturn(activities)

        assertThrows<IllegalStateException> {
            activityRepositorySecured.findWorkedMinutes(
                startDate, endDate, userId
            )
        }
    }

    private companion object {
        private const val userId = 1L
        private const val adminUserId = 3L
        private val today = LocalDate.now()
        private val projectRole = createProjectRole()
        private val authenticationWithAdminRole =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("admin")))
        private val authenticationWithoutAdminRole =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))

    }
}