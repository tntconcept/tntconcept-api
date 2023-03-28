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

internal class ActivityRepositoryTest {

    private val securityService = mock<SecurityService>()
    private val activityDao = mock<ActivityDao>()

    private var activityRepository = ActivityRepository(activityDao, securityService)

    @Test
    fun `get activity should throw illegal state exception`() {
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

        assertThrows<IllegalStateException> { activityRepository.findById(activityId) }
    }

    @Test
    fun `get activity by id for user with admin role permission`() {
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

        val result = activityRepository.findById(activityId)

        assertEquals(activity, result)
    }

    @Test
    fun `get activity by id for user without admin role permission should return null`() {
        val activityId = 1L

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))

        val result = activityRepository.findById(activityId)

        assertNull(result)
        verify(activityDao).findByIdAndUserId(activityId, userId)
    }

    @Test
    fun `get worked minutes between dates should retrieve authenticated user worked minutes`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val workedTime = listOf(ActivityTimeOnly(startDate, 60, projectRole.id))

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(activityDao.workedMinutesBetweenDate(startDate, endDate, userId)).thenReturn(workedTime)

        val result: List<ActivityTimeOnly> = activityRepository.workedMinutesBetweenDate(
            startDate, endDate, userId
        )

        assertEquals(workedTime, result)
    }

    @Test
    fun `get worked minutes between dates should retrieve other user activities if is admin`() {
        val otherUserId = 2L
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val workedTime = listOf(ActivityTimeOnly(startDate, 60, projectRole.id))

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))
        whenever(activityDao.workedMinutesBetweenDate(startDate, endDate, otherUserId)).thenReturn(workedTime)

        val result: List<ActivityTimeOnly> = activityRepository.workedMinutesBetweenDate(
            startDate, endDate, otherUserId
        )

        assertEquals(workedTime, result)
    }

    @Test
    fun `should return empty worked minutes between range if logged user is not admin and requested activities belong to other user`() {
        val otherUserId = 2L
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))

        val result: List<ActivityTimeOnly> = activityRepository.workedMinutesBetweenDate(
            startDate, endDate, otherUserId
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `get worked minutes between dates should retrieve logged user activities`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val workedTime = listOf(ActivityTimeOnly(startDate, 60, projectRole.id))

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(activityDao.workedMinutesBetweenDate(startDate, endDate, userId)).thenReturn(workedTime)

        val result: List<ActivityTimeOnly> = activityRepository.workedMinutesBetweenDate(
            startDate, endDate, userId
        )

        assertEquals(workedTime, result)
    }

    @Test
    fun `get worked minutes between dates should retrieve authenticated user activities`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val workedTime = listOf(ActivityTimeOnly(startDate, 60, projectRole.id))

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(activityDao.workedMinutesBetweenDate(startDate, endDate, userId)).thenReturn(workedTime)

        val result: List<ActivityTimeOnly> = activityRepository.workedMinutesBetweenDate(
            startDate, endDate, userId
        )

        assertEquals(workedTime, result)
    }

    @Test
    fun `get activities between dates should retrieve other user activities if is admin`() {
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
        whenever(activityDao.getActivitiesBetweenDate(startDate, endDate, otherUserId)).thenReturn(activities)

        val result: List<Activity> = activityRepository.getActivitiesBetweenDate(
            startDate, endDate, otherUserId
        )

        assertEquals(activities, result)
    }

    @Test
    fun `should return empty activities between range if requested activities belong to other user and logged user is not admin`() {
        val otherUserId = 2L
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))

        val result: List<Activity> = activityRepository.getActivitiesBetweenDate(
            startDate, endDate, otherUserId
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `get activities between dates should retrieve logged user activities`() {
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

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(activityDao.getActivitiesBetweenDate(startDate, endDate, userId)).thenReturn(activities)

        val result: List<Activity> = activityRepository.getActivitiesBetweenDate(
            startDate, endDate, userId
        )

        assertEquals(activities, result)
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