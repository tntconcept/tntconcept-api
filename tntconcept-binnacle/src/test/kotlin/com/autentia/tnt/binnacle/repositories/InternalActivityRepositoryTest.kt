package com.autentia.tnt.binnacle.repositories


import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class InternalActivityRepositoryTest {
    private val activityDao = mock<ActivityDao>()

    private val internalActivityRepository = InternalActivityRepository(activityDao)

    @Test
    fun `find all activities`() {
        val activities = listOf(
            createActivity()
        )
        val activitySpecification = ActivityPredicates.ALL

        whenever(activityDao.findAll(activitySpecification)).thenReturn(activities)

        val result = internalActivityRepository.findAll(ActivityPredicates.ALL)

        assertEquals(activities, result)
    }

    @Test
    fun `find activity by id`() {
        val activityId = 1L
        val activity = createActivity()
        whenever(activityDao.findById(activityId)).thenReturn(Optional.of(activity))

        val result = internalActivityRepository.findById(activityId)

        assertEquals(activity, result)
    }

    @Test
    fun `find activity by id not found`() {
        val activityId = 1L

        whenever(activityDao.findById(activityId)).thenReturn(Optional.empty())

        val result = internalActivityRepository.findById(activityId)

        assertEquals(null, result)
    }

    @Test
    fun `find activity between dates`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val activities = listOf(
            createActivity()
        )
        whenever(activityDao.find(startDate, endDate, userId)).thenReturn(activities)

        val result: List<Activity> = internalActivityRepository.findByUserId(startDate, endDate, userId)

        assertEquals(activities, result)
    }

    @Test
    fun `find by project role ids should retrieve activities with user id`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val projectRoleIds = listOf(1L)
        val activities = listOf(
            createActivity()
        )
        whenever(activityDao.findByProjectRoleIds(startDate, endDate, projectRoleIds, userId)).thenReturn(activities)

        val result = internalActivityRepository.findByProjectRoleIds(startDate, endDate, projectRoleIds, userId)

        assertEquals(activities, result)
    }

    @Test
    fun `find between dates and by userIds should retrieve activities`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val userIds = listOf(userId)
        val activities = listOf(createActivity())

        whenever(activityDao.find(startDate, endDate, userIds)).thenReturn(activities)

        val result = internalActivityRepository.find(startDate, endDate, userIds)

        assertEquals(activities, result)
    }

    @Test
    fun `find by approval state should retrieve activities`() {
        val activities = listOf(createActivity())
        val approvalState = ApprovalState.ACCEPTED

        whenever(activityDao.findByApprovalState(approvalState)).thenReturn(activities)

        val result = internalActivityRepository.find(approvalState)

        assertEquals(activities, result)
    }

    @Test
    fun `find by approval state and user id should retrieve activities`() {
        val activities = listOf(createActivity(approvalState = ApprovalState.ACCEPTED))
        val approvalState = ApprovalState.ACCEPTED

        whenever(activityDao.findByApprovalStateAndUserId(approvalState, userId)).thenReturn(activities)

        val result = internalActivityRepository.findByApprovalStateAndUserId(approvalState, userId)

        assertEquals(activities, result)
    }

    @Test
    fun `find by project role and user id should retrieve activities`() {
        val activities = listOf(createActivity())
        val projectRoleId = 1L

        whenever(activityDao.findByProjectRoleIdAndUserId(projectRoleId, userId)).thenReturn(activities)

        val result = internalActivityRepository.findByProjectRoleIdAndUserId(projectRoleId, userId)

        assertEquals(activities, result)
    }

    @Test
    fun `find of latest projects by user id should retrieve activities`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val activities = listOf(
            createActivity().copy(
                start = startDate,
                end = endDate
            )
        )

        whenever(activityDao.findOfLatestProjects(startDate, endDate, userId)).thenReturn(activities)

        val result = internalActivityRepository.findOfLatestProjects(startDate, endDate, userId)

        assertEquals(activities, result)
    }

    @Test
    fun `find by project id should retrieve activities`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val projectId = 1L
        val activities = listOf(createActivity())

        whenever(activityDao.findByProjectId(startDate, endDate, projectId, userId)).thenReturn(activities)

        val result = internalActivityRepository.findByProjectId(startDate, endDate, projectId, userId)

        assertEquals(activities, result)
    }

    @Test
    fun `find activities by worked minutes`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val activities = listOf(
            ActivityTimeOnly(
                startDate = startDate,
                duration = 10,
                1L
            )
        )

        whenever(activityDao.findWorkedMinutes(startDate, endDate, userId)).thenReturn(activities)

        val result = internalActivityRepository.findWorkedMinutes(startDate, endDate, userId)

        assertEquals(activities, result)
    }

    @Test
    fun `find overlapped should retrieve activities`() {
        val startDate = today.atTime(LocalTime.MIN)
        val endDate = today.plusDays(30L).atTime(
            LocalTime.MAX
        )
        val activities = listOf(createActivity())

        whenever(activityDao.findOverlapped(startDate, endDate, userId)).thenReturn(activities)

        val result = internalActivityRepository.findOverlapped(startDate, endDate, userId)

        assertEquals(activities, result)
    }

    @Test
    fun `find by id and user id should retrieve activity`() {
        val activityId = 1L
        val activity = createActivity(activityId)

        whenever(activityDao.findByIdAndUserId(activityId, userId)).thenReturn(activity)

        val result = internalActivityRepository.findByIdAndUserId(activityId, userId)

        assertEquals(activity, result)
    }

    @Test
    fun `save should use activity dao`() {
        val activity = createActivity()

        whenever(activityDao.save(activity)).thenReturn(activity)

        val result = internalActivityRepository.save(activity)

        assertEquals(activity, result)
    }

    @Test
    fun `update should use activity dao`() {
        val activity = createActivity()

        whenever(activityDao.update(activity)).thenReturn(activity)

        val result = internalActivityRepository.update(activity)

        assertEquals(activity, result)
    }

    @Test
    fun `delete should use activity dao`() {
        val activityId = 1L

        internalActivityRepository.deleteById(activityId)

        verify(activityDao).deleteById(activityId)
    }

    private companion object {
        private const val userId = 1L
        private val today = LocalDate.now()
        private val projectRole = createProjectRole()
    }
}