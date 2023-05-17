package com.autentia.tnt.binnacle.repositories


import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
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

        whenever(activityDao.findAll(activitySpecification)).thenReturn(activities)

        val result = internalActivityRepository.findAll(ActivityPredicates.ALL)

        Assertions.assertEquals(activities, result)
    }

    @Test
    fun `find activity by id`() {
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
        whenever(activityDao.findByProjectRoleIds(startDate, endDate, projectRoleIds, userId)).thenReturn(activities)

        val result = internalActivityRepository.findByProjectRoleIds(startDate, endDate, projectRoleIds, userId)

        assertEquals(activities, result)
    }

    private companion object {
        private const val userId = 1L
        private val today = LocalDate.now()
        private val projectRole = createProjectRole()
    }
}