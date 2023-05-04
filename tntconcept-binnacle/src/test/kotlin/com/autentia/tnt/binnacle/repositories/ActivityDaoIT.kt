package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.core.domain.ActivityInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.RequireEvidence
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.LocalTime

@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActivityDaoIT {

    @Inject
    private lateinit var activityDao: ActivityDao

    @Test
    fun `should find activity by id`() {
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
        val savedActivity = activityDao.save(activity)

        val result = activityDao.findById(savedActivity.id!!)

        assertEquals(savedActivity, result.get())
    }

    @Test
    fun `should find activity by id and user id`() {
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
        val savedActivity = activityDao.save(activity)

        val result = activityDao.findByIdAndUserId(savedActivity.id!!, activity.userId)

        assertEquals(savedActivity, result)
    }

    @Test
    fun `should find activities filtered by period of time`() {
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
        val savedActivities = activityDao.saveAll(
            listOf(
                todayActivity, yesterdayActivity, activityForTwoDays
            )
        )

        val start = yesterday.minusDays(1L).atTime(LocalTime.MIN)
        val end = today.atTime(LocalTime.MAX)
        val activitiesBetweenDate = activityDao.find(start, end, userId)

        assertEquals(3, activitiesBetweenDate.size)
        assertTrue(activitiesBetweenDate.contains(savedActivities.elementAt(0)))
        assertTrue(activitiesBetweenDate.contains(savedActivities.elementAt(1)))
        assertTrue(activitiesBetweenDate.contains(savedActivities.elementAt(2)))
    }

    @Test
    fun `should find pending activities`() {
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
        val savedActivities = activityDao.saveAll(
            listOf(
                todayActivity, yesterdayActivity, activityForTwoDays
            )
        )
        var retrievedActivities = activityDao.findByApprovalStateAndUserId(ApprovalState.PENDING, userId)

        assertEquals(2, retrievedActivities.size)
        assertTrue(retrievedActivities.contains(savedActivities.elementAt(1)))
        assertTrue(retrievedActivities.contains(savedActivities.elementAt(2)))

        retrievedActivities = activityDao.findByApprovalStateAndUserId(ApprovalState.ACCEPTED, userId)
        assertEquals(1, retrievedActivities.size)
        assertTrue(retrievedActivities.contains(savedActivities.elementAt(0)))
    }

    @Test
    fun `should find pending activities of all users`() {
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
        val savedActivities = activityDao.saveAll(
            listOf(
                todayActivity, yesterdayActivity, activityForTwoDays
            )
        )
        var retrievedActivities = activityDao.findByApprovalState(ApprovalState.PENDING)

        assertEquals(2, retrievedActivities.size)
        assertTrue(retrievedActivities.contains(savedActivities.elementAt(1)))
        assertTrue(retrievedActivities.contains(savedActivities.elementAt(2)))
    }

    @Test
    fun `should find worked minutes between date`() {
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
        activityDao.saveAll(
            listOf(
                todayActivity, yesterdayActivity, activityForTwoDays
            )
        )
        val start = yesterday.minusDays(1L).atTime(LocalTime.MIN)
        val end = today.atTime(LocalTime.MAX)

        val workedTimeActivities = activityDao.findIntervals(start, end, createProjectRole().id, userId)

        val expectedWorkedMinutesActivities = listOf(
            ActivityInterval(yesterdayActivity.start, yesterdayActivity.end, projectRole.timeUnit),
            ActivityInterval(todayActivity.start, todayActivity.end, projectRole.timeUnit)
        )

        assertEquals(2, workedTimeActivities.size)
        assertEquals(expectedWorkedMinutesActivities, workedTimeActivities)
    }

    @Test
    fun `should find overlapped activities`() {
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
        activityDao.saveAll(
            listOf(
                todayActivity, yesterdayActivity, activityForTwoDays
            )
        )
        val start = yesterday.minusDays(1L).atTime(LocalTime.MIN)
        val end = today.atTime(LocalTime.MAX)

        val workedTimeActivities = activityDao.findOverlapped(start, end, userId)

        val expectedWorkedMinutesActivities = listOf(
            todayActivity, yesterdayActivity
        )

        assertEquals(2, workedTimeActivities.size)
        assertEquals(expectedWorkedMinutesActivities, workedTimeActivities)
    }

    @Test
    fun `should find intervals`() {
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
        val theDayBeforeYesterdayActivity = Activity(
            start = yesterday.minusDays(1L).atTime(8, 0, 0),
            end = yesterday.minusDays(1L).atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 2",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        activityDao.saveAll(
            listOf(
                todayActivity, yesterdayActivity, theDayBeforeYesterdayActivity
            )
        )

        val expectedActivityIntervals = listOf(
            ActivityInterval(yesterdayActivity.start, yesterdayActivity.end, projectRole.timeUnit),
            ActivityInterval(todayActivity.start, todayActivity.end, projectRole.timeUnit),
        )

        val start = yesterday.atTime(LocalTime.MIN)
        val end = today.atTime(LocalTime.MAX)

        val activityIntervals = activityDao.findIntervals(start, end, projectRole.id, userId)

        assertEquals(expectedActivityIntervals, activityIntervals)
    }

    @Test
    fun `should find by project role and user id`() {
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
            projectRole = createProjectRole(2L),
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val theDayBeforeYesterdayActivity = Activity(
            start = yesterday.minusDays(1L).atTime(8, 0, 0),
            end = yesterday.minusDays(1L).atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 2",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        activityDao.saveAll(
            listOf(
                todayActivity, yesterdayActivity, theDayBeforeYesterdayActivity
            )
        )

        val expectedActivities = listOf(todayActivity, theDayBeforeYesterdayActivity)
        val result = activityDao.findByProjectRoleIdAndUserId(projectRole.id, userId)

        assertEquals(expectedActivities, result)
    }

    @Test
    fun `should find activities without evidence and required evidence once`() {
        val projectRole = createProjectRole().copy(id = 4L, requireEvidence = RequireEvidence.ONCE);
        val projectRoleWithoutEvidencesNeeded = createProjectRole().copy(id = 5L, requireEvidence = RequireEvidence.NO)
        val activityWithoutEvidence = Activity(
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
        val activityWithEvidence = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 2",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.PENDING
        )
        val activityWithoutEvidenceNeeded = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 2",
            projectRole = projectRoleWithoutEvidencesNeeded,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        activityDao.saveAll(listOf(activityWithoutEvidence, activityWithEvidence, activityWithoutEvidenceNeeded))

        val result = activityDao.findWithMissingEvidenceOnce()

        val expectedResults = listOf(activityWithoutEvidence)
        assertEquals(expectedResults, result)
    }


    private companion object {
        private val today = LocalDate.now()
        private val yesterday = LocalDate.now().minusDays(1)
        private const val userId = 1L
        private val projectRole = createProjectRole()
    }
}