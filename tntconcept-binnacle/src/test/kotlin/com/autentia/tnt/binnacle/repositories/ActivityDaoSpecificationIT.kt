package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityDaoSpecificationIT {

    @Inject
    private lateinit var activityDao: ActivityDao

    @Test
    fun `test findAll without condition`() {

        val activitiesToSave = listOf(
            createActivity().copy(id = null),
            createActivity().copy(id = null, userId = 2L)
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(ActivityPredicates.ALL)
        assertEquals(2, actualActivities.size)
    }

    @Test
    fun `test findAll by approvalState`() {

        val activitiesToSave = listOf(
            createActivity().copy(id = null),
            createActivity().copy(id = null, approvalState = ApprovalState.ACCEPTED)
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(ActivityPredicates.approvalState(ApprovalState.ACCEPTED))
        assertEquals(1, actualActivities.size)
        assertEquals(ApprovalState.ACCEPTED, actualActivities[0].approvalState)
    }

    @Test
    fun `test findAll by projectRole`() {

        val activitiesToSave = listOf(
            createActivity(null),
            createActivity(null).copy(projectRole = createProjectRole(2L))
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(ActivityPredicates.roleId(1L))
        assertEquals(1, actualActivities.size)
        assertEquals(1L, actualActivities[0].projectRole.id)
    }

    @Test
    fun `test findAll with Specification date between condition`() {

        val activity = createActivity().copy(id = null)
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(1)
        val expectedActivity = activity.copy(
            id = null,
            start = startDate.atStartOfDay().minusDays(1),
            end = startDate.atStartOfDay()
        )
        val activitiesToSave = listOf(
            activity.copy(
                id = null,
                start = startDate.atStartOfDay().minusDays(1),
                end = startDate.atStartOfDay().minusSeconds(1)
            ),
            activity.copy(
                id = null,
                start = endDate.atTime(23, 59, 59).plusSeconds(1),
                end = endDate.atTime(23, 59, 59).plusDays(1)
            ),
            expectedActivity
        )

        activityDao.saveAll(activitiesToSave)

        val activitySpecification = ActivityPredicates.endDateGreaterThanOrEqualTo(startDate)
            .and(ActivityPredicates.startDateLessThanOrEqualTo(endDate))

        val actualActivities = activityDao.findAll(activitySpecification)

        assertEquals(1, actualActivities.size)
        assertEquals(expectedActivity.start, actualActivities[0].start)
        assertEquals(expectedActivity.end, actualActivities[0].end)
    }
}