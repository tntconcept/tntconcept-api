package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

internal class ActivityRepositorySecuredTest {

    private val securityService = mock<SecurityService>()
    private val internalActivityRepository = mock<InternalActivityRepository>()

    private var activityRepositorySecured =
        ActivityRepositorySecured(internalActivityRepository, securityService)

    @Test
    fun `find all with user id filter`() {
        val activities = listOf(
            Activity(
                id = 1L,
                start = today.atTime(10, 0, 0),
                end = today.atTime(12, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.NA
            )
        )
        val activitySpecification = PredicateBuilder.and(ActivityPredicates.ALL, ActivityPredicates.userId(userId))

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(internalActivityRepository.findAll(activitySpecification)).thenReturn(activities)

        val result = activityRepositorySecured.findAll(ActivityPredicates.ALL)

        assertEquals(activities, result)
    }

    @Test
    fun `find all without user id filter`() {
        val activities = listOf(
            Activity(
                id = 2L,
                start = today.atTime(10, 0, 0),
                end = today.atTime(12, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.NA
            )
        )
        val activitySpecification = ActivityPredicates.ALL

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))
        whenever(internalActivityRepository.findAll(activitySpecification)).thenReturn(activities)

        val result = activityRepositorySecured.findAll(ActivityPredicates.ALL)

        assertEquals(activities, result)
    }

    @Test
    fun `find activity should throw illegal state exception`() {
        val activityId = 1L

        val activity = Activity(
            id = activityId,
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.NA
        )
        whenever(internalActivityRepository.findByIdAndUserId(activityId, userId)).thenReturn(activity)
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> { activityRepositorySecured.findById(activityId) }
    }

    @Test
    fun `find activity by id for user with admin role permission`() {
        val activityId = 1L
        val activity = Activity(
            id = activityId,
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = 2L,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.NA,
        )
        whenever(internalActivityRepository.findById(activityId)).thenReturn(activity)
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
        verify(internalActivityRepository).findByIdAndUserId(activityId, userId)
    }

    @Test
    fun `test findOfLatestProjects should throw IllegalStateException if there is not logged user`() {

        whenever(securityService.authentication).thenReturn(Optional.empty())
        assertThrows<IllegalStateException> {
            activityRepositorySecured.findOfLatestProjects(LocalDateTime.now(), LocalDateTime.now())
        }
    }

    @Test
    fun `test findOfLatestProjects should return only user activities`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val userActivity = createActivity()

        whenever(securityService.authentication).doReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(internalActivityRepository.findOfLatestProjects(startDate, endDate, userActivity.userId)).doReturn(
            listOf(userActivity)
        )

        assertEquals(listOf(userActivity), activityRepositorySecured.findOfLatestProjects(startDate, endDate))
    }

    @Test
    fun `test findByProjectRoleIds should throw IllegalStateException if there is not logged user`() {

        whenever(securityService.authentication).thenReturn(Optional.empty())
        assertThrows<IllegalStateException> {
            activityRepositorySecured.findByProjectRoleIds(LocalDateTime.now(), LocalDateTime.now(), listOf(1L), userId)
        }
    }

    @Test
    fun `test findByProjectRoleIds should throw IllegalArgumentException if user id is not user logged and is not admin`() {

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        assertThrows<IllegalArgumentException> {
            activityRepositorySecured.findByProjectRoleIds(
                LocalDateTime.now(),
                LocalDateTime.now(),
                listOf(1L),
                adminUserId
            )
        }
    }

    @Test
    fun `test findByProjectRoleIds should return only user activities if user logged is not admin`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val userActivity = createActivity()
        val projectRoleIds = listOf(1L)
        whenever(securityService.authentication).doReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(
            internalActivityRepository.findByProjectRoleIds(
                startDate,
                endDate,
                projectRoleIds,
                userActivity.userId
            )
        ).doReturn(
            listOf(userActivity)
        )

        assertEquals(
            listOf(userActivity),
            activityRepositorySecured.findByProjectRoleIds(startDate, endDate, projectRoleIds, userId)
        )
    }

    @Test
    fun `test findByProjectId should throw IllegalStateException if there is not logged user`() {

        whenever(securityService.authentication).thenReturn(Optional.empty())
        assertThrows<IllegalStateException> {
            activityRepositorySecured.findByProjectId(LocalDateTime.now(), LocalDateTime.now(), 1L)
        }
    }

    @Test
    fun `test findByProjectId should return only user activities`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val userActivity = createActivity()

        whenever(securityService.authentication).doReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(internalActivityRepository.findByProjectId(startDate, endDate, 1L, userActivity.userId)).doReturn(
            listOf(userActivity)
        )

        assertEquals(listOf(userActivity), activityRepositorySecured.findByProjectId(startDate, endDate, 1L))
    }

    @Test
    fun `find worked minutes should retrieve authenticated user worked minutes`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val workedTime = listOf(ActivityTimeOnly(startDate, 60, projectRole.id))

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(internalActivityRepository.findWorkedMinutes(startDate, endDate, userId)).thenReturn(workedTime)

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
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityRepositorySecured.findWorkedMinutes(
                startDate, endDate
            )
        }
    }

    @Test
    fun `find activities between dates should retrieve other user activities if logged user is admin`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val activities = listOf(
            Activity(
                id = 1L,
                start = today.atTime(10, 0, 0),
                end = today.atTime(12, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.NA,
            )
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))
        whenever(internalActivityRepository.findByUserId(startDate, endDate, userId)).thenReturn(activities)

        val result: List<Activity> = activityRepositorySecured.findByUserId(
            startDate, endDate, userId
        )

        assertEquals(activities, result)
    }

    @Test
    fun `find activities between dates should not retrieve other user activities if logged user is not admin`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))

        assertThrows<IllegalArgumentException> {
            activityRepositorySecured.findByUserId(
                startDate, endDate, adminUserId
            )
        }
    }

    @Test
    fun `find activities between dates should retrieve logged user activities if user is not admin`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val activities = listOf(
            Activity(
                id = 1L,
                start = today.atTime(10, 0, 0),
                end = today.atTime(12, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.NA,
            )
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(internalActivityRepository.findByUserId(startDate, endDate, userId)).thenReturn(activities)


        val result = activityRepositorySecured.findByUserId(
            startDate, endDate, userId
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
                start = today.atTime(10, 0, 0),
                end = today.atTime(12, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.NA,
            )
        )

        whenever(securityService.authentication).thenReturn(Optional.empty())
        whenever(internalActivityRepository.find(startDate, endDate, userId)).thenReturn(activities)

        assertThrows<IllegalStateException> {
            activityRepositorySecured.findWorkedMinutes(
                startDate, endDate
            )
        }
    }

    @Test
    fun `save activity should throw IllegalStateException if user is not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityRepositorySecured.save(mock())
        }
    }

    @Test
    fun `save activity should throw IllegalArgumentException if activity does not belong to the authenticated user`() {
        val activity = Activity(
            id = 1L,
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.NA,
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))

        assertThrows<IllegalArgumentException> {
            activityRepositorySecured.save(activity)
        }
    }

    @Test
    fun `save activity should call dao to save activity`() {
        val activity = Activity(
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.NA,
        )
        val expectedActivity = Activity(
            id = 1L,
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.NA,
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(internalActivityRepository.save(activity)).thenReturn(expectedActivity)

        val result = activityRepositorySecured.save(activity)

        assertEquals(expectedActivity, result)
    }

    @Test
    fun `update activity should throw IllegalStateException if user is not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityRepositorySecured.update(mock())
        }
    }

    @Test
    fun `update activity should throw IllegalArgumentException if activity does not belong to the authenticated user`() {
        val activity = Activity(
            id = 1L,
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.NA,
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))

        assertThrows<IllegalArgumentException> {
            activityRepositorySecured.update(activity)
        }
    }

    @Test
    fun `update activity should throw IllegalArgumentException if activity does not exist`() {
        val activity = Activity(
            id = 1L,
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.NA,
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(activity.id?.let { internalActivityRepository.findById(it) }).thenReturn(null)

        assertThrows<IllegalArgumentException> {
            activityRepositorySecured.update(activity)
        }
    }

    @Test
    fun `update activity should call dao`() {
        val activity = Activity(
            id = 1L,
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Updated test activity",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.NA,
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(activity.id?.let { internalActivityRepository.findById(it) }).thenReturn(activity)
        whenever(internalActivityRepository.update(activity)).thenReturn(activity)

        val result = activityRepositorySecured.update(activity)

        assertEquals(activity, result)
    }

    @Test
    fun `delete activity should throw IllegalStateException if user is not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityRepositorySecured.deleteById(1L)
        }
    }

    @Test
    fun `delete activity should throw IllegalArgumentException if activity does not belong to the authenticated user`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))

        assertThrows<IllegalArgumentException> {
            activityRepositorySecured.deleteById(1L)
        }
    }

    @Test
    fun `delete activity should throw IllegalArgumentException if activity does not exist`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))

        assertThrows<IllegalArgumentException> {
            activityRepositorySecured.deleteById(1L)
        }
    }

    @Test
    fun `delete activity should call dao`() {
        val activity = Activity(
            id = 1L,
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.NA,
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(activity.id?.let { internalActivityRepository.findById(it) }).thenReturn(activity)

        activityRepositorySecured.deleteById(1L)

        verify(internalActivityRepository).deleteById(1L)
    }

    @Test
    fun `find by approval state should return activities of all users`() {
        val activities = listOf(
            Activity(
                id = 1L,
                start = today.atTime(10, 0, 0),
                end = today.atTime(12, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.NA,
            )
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))
        whenever(internalActivityRepository.findByApprovalState(ApprovalState.NA)).thenReturn(activities)

        val result: List<Activity> = activityRepositorySecured.find(
            ApprovalState.NA
        )

        assertEquals(activities, result)
    }

    @Test
    fun `find by approval state should return activities of the authenticated user`() {
        val activities = listOf(
            Activity(
                id = 1L,
                start = today.atTime(10, 0, 0),
                end = today.atTime(12, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.NA,
            )
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))
        whenever(internalActivityRepository.findByApprovalStateAndUserId(ApprovalState.NA, userId)).thenReturn(
            activities
        )

        val result: List<Activity> = activityRepositorySecured.find(
            ApprovalState.NA
        )

        assertEquals(activities, result)
    }

    @Test
    fun `find activities by approval state should throw IllegalStateException`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityRepositorySecured.find(
                ApprovalState.NA
            )
        }
    }

    @Test
    fun `find by project role should return activities`() {
        val activities = listOf(
            Activity(
                id = 1L,
                start = today.atTime(10, 0, 0),
                end = today.atTime(12, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.NA,
            )
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))
        whenever(internalActivityRepository.findByProjectRoleIdAndUserId(projectRole.id, adminUserId)).thenReturn(
            activities
        )

        val result: List<Activity> = activityRepositorySecured.find(
            projectRole.id
        )

        assertEquals(activities, result)
    }

    @Test
    fun `find activities by project role should throw IllegalStateException`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityRepositorySecured.find(
                projectRole.id
            )
        }
    }

    @Test
    fun `find overlapped should return activities`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val activities = listOf(
            Activity(
                id = 1L,
                start = today.atTime(10, 0, 0),
                end = today.atTime(12, 0, 0),
                duration = 120,
                description = "Test activity",
                projectRole = projectRole,
                userId = userId,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.NA,
            )
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))
        whenever(internalActivityRepository.findOverlapped(startDate, endDate, adminUserId)).thenReturn(activities)

        val result: List<Activity> = activityRepositorySecured.findOverlapped(
            startDate, endDate
        )

        assertEquals(activities, result)
    }

    @Test
    fun `find overlapped should throw IllegalStateException`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityRepositorySecured.findOverlapped(
                startDate, endDate
            )
        }
    }

    @Test
    fun `find intervals should return activities`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val intervals = listOf(
            createActivity().copy(start = today.atTime(10, 0, 0), end = today.atTime(12, 0, 0))
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))
        whenever(internalActivityRepository.find(startDate, endDate, projectRole.id, adminUserId)).thenReturn(intervals)

        val result = activityRepositorySecured.find(
            startDate, endDate, projectRole.id
        )

        assertEquals(intervals, result)
    }

    @Test
    fun `find intervals should throw IllegalStateException`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityRepositorySecured.find(
                startDate, endDate, projectRole.id
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