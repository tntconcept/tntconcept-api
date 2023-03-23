package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.core.domain.ActivityInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.LocalTime

@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActivityRepositoryIT {

    @Inject
    private lateinit var activityRepository: ActivityRepository

    @Test
    fun `should get activity by id`() {
        val activity = Activity(
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = createProjectRole(),
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.ACCEPTED
        )
        val savedActivity = activityRepository.save(activity)

        val result = activityRepository.findByIdEager(savedActivity.id!!)

        assertEquals(savedActivity, result)

    }

    @Test
    fun `should get activities filtered by period of time`() {
        val todayActivity = Activity(
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = createProjectRole(),
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.ACCEPTED
        )
        val yesterdayActivity = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 2",
            projectRole = createProjectRole(),
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityForTwoDays = Activity(
            start = yesterday.minusDays(2).atTime(0, 0, 0),
            end = yesterday.minusDays(1).atTime(23, 59, 59),
            duration = 960,
            description = "Test activity 3",
            projectRole = createProjectRole(),
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.ACCEPTED
        )
        val savedActivities = activityRepository.saveAll(
            listOf(
                todayActivity, yesterdayActivity, activityForTwoDays
            )
        )

        val start = yesterday.minusDays(1L).atTime(LocalTime.MIN)
        val end = today.atTime(LocalTime.MAX)
        val activitiesBetweenDate = activityRepository.getActivitiesBetweenDate(start, end, userId)

        assertEquals(3, activitiesBetweenDate.size)
        assertTrue(activitiesBetweenDate.contains(savedActivities.elementAt(0)))
        assertTrue(activitiesBetweenDate.contains(savedActivities.elementAt(1)))
        assertTrue(activitiesBetweenDate.contains(savedActivities.elementAt(2)))
    }

    @Test
    fun `should get pending activities`() {
        val todayActivity = Activity(
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = createProjectRole(),
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.ACCEPTED
        )
        val yesterdayActivity = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 2",
            projectRole = createProjectRole(),
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityForTwoDays = Activity(
            start = yesterday.minusDays(2).atTime(0, 0, 0),
            end = yesterday.minusDays(1).atTime(23, 59, 59),
            duration = 960,
            description = "Test activity 3",
            projectRole = createProjectRole(),
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val savedActivities = activityRepository.saveAll(
            listOf(
                todayActivity, yesterdayActivity, activityForTwoDays
            )
        )
        var retrievedActivities = activityRepository.getActivitiesApprovalState(ApprovalState.PENDING, userId)

        assertEquals(2, retrievedActivities.size)
        assertTrue(retrievedActivities.contains(savedActivities.elementAt(1)))
        assertTrue(retrievedActivities.contains(savedActivities.elementAt(2)))

        retrievedActivities = activityRepository.getActivitiesApprovalState(ApprovalState.ACCEPTED, userId)
        assertEquals(1, retrievedActivities.size)
        assertTrue(retrievedActivities.contains(savedActivities.elementAt(0)))
    }

    @Test
    fun `should get worked minutes between date`() {
        val todayActivity = Activity(
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.ACCEPTED
        )
        val yesterdayActivity = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 2",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityForTwoDays = Activity(
            start = today.plusDays(2).atTime(0, 0, 0),
            end = today.plusDays(3).atTime(23, 59, 59),
            duration = 960,
            description = "Test activity 3",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        activityRepository.saveAll(
            listOf(
                todayActivity, yesterdayActivity, activityForTwoDays
            )
        )
        val start = yesterday.minusDays(1L).atTime(LocalTime.MIN)
        val end = today.atTime(LocalTime.MAX)

        val workedTimeActivities = activityRepository.getActivitiesIntervals(start, end, createProjectRole().id, userId)

        val expectedWorkedMinutesActivities = listOf(
            ActivityInterval(yesterdayActivity.start, yesterdayActivity.end, projectRole.timeUnit),
            ActivityInterval(todayActivity.start, todayActivity.end, projectRole.timeUnit)
        )

        assertEquals(2, workedTimeActivities.size)
        assertEquals(expectedWorkedMinutesActivities, workedTimeActivities)
    }

    private companion object {
        private val today = LocalDate.now()
        private val yesterday = LocalDate.now().minusDays(1)
        private val userId = 1L
        private val projectRole = createProjectRole()
    }
}