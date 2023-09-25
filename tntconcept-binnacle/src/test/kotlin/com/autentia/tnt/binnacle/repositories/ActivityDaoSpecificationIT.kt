package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createOrganization
import com.autentia.tnt.binnacle.config.createProject
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import io.micronaut.data.model.Sort
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityDaoSpecificationIT {

    @Inject
    private lateinit var activityDao: ActivityDao

    @Inject
    private lateinit var projectRepository: ProjectRepository

    @Inject
    private lateinit var organizationRepository: OrganizationRepository

    @Inject

    @Test
    fun `test findAll without condition`() {

        val activitiesToSave = listOf(
            createActivity().copy(id = null), createActivity().copy(id = null, userId = 1L)
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(ActivityPredicates.ALL)
        assertEquals(2, actualActivities.size)
    }

    @Test
    fun `test findAll with order`() {
        val initDate = LocalDate.of(2023, 1, 1)
        val predicate = ActivityPredicates.startDateBetweenDates(DateInterval.of(initDate, initDate.plusDays(3L)))

        val activitiesToSave = listOf(
            createActivity().copy(id = null, start = initDate.plusDays(1L).atTime(10, 30, 0), end = initDate.atTime(14, 30, 0)),
            createActivity().copy(id = null, start = initDate.plusDays(2L).atTime(12, 30, 0), end = initDate.atTime(14, 30, 0)),
            createActivity().copy(id = null, start = initDate.atTime(12, 30, 0), end = initDate.atTime(14, 30, 0)),
            createActivity().copy(id = null, userId = 1L, start = initDate.atTime(9, 0, 0), end = initDate.atTime(12, 30, 0)),
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(predicate, Sort.of(Sort.Order("start")))

        assertEquals(4, actualActivities.size)
        assertEquals(activitiesToSave[3].start, actualActivities[0].start)
        assertEquals(activitiesToSave[2].start, actualActivities[1].start)
        assertEquals(activitiesToSave[0].start, actualActivities[2].start)
        assertEquals(activitiesToSave[1].start, actualActivities[3].start)
    }

    @Test
    fun `test findAll by userId`() {

        val activitiesToSave = listOf(
            createActivity().copy(id = null),
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(ActivityPredicates.ALL.and(ActivityPredicates.userId(1L)))
        assertEquals(1, actualActivities.size)
        assertEquals(1L, actualActivities[0].userId)
    }

    @Test
    fun `test findAll by approvalState`() {

        val activitiesToSave = listOf(
            createActivity().copy(id = null), createActivity().copy(id = null, approvalState = ApprovalState.ACCEPTED)
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities =
            activityDao.findAll(ActivityPredicates.ALL.and(ActivityPredicates.approvalState(ApprovalState.ACCEPTED)))
        assertEquals(1, actualActivities.size)
        assertEquals(ApprovalState.ACCEPTED, actualActivities[0].approvalState)
    }

    @Test
    fun `test findAll by projectRole`() {

        val activitiesToSave = listOf(
            createActivity(null), createActivity(null).copy(projectRole = createProjectRole(2L))
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(ActivityPredicates.ALL.and(ActivityPredicates.roleId(1L)))
        assertEquals(1, actualActivities.size)
        assertEquals(1L, actualActivities[0].projectRole.id)
    }

    @Test
    fun `test findAll by project`() {

        val activitiesToSave = listOf(
            createActivity(null),
            createActivity(null).copy(projectRole = createProjectRole(2L).copy(project = createProject(id = 2L)))
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(ActivityPredicates.ALL.and(ActivityPredicates.projectId(1L)))
        assertEquals(1, actualActivities.size)
        assertEquals(1L, actualActivities[0].projectRole.project.id)
    }

    @Test
    fun `test findAll by organization`() {

        val project = projectRepository.findById(2).get()
        val organization = organizationRepository.findById(2L)

        projectRepository.update(
            (Project(
                project.id, project.name, false, project.billable, LocalDate.now(), null, null, organization.get(), project.projectRoles
            ))
        )

        val activitiesToSave = listOf(
            createActivity(null), createActivity(null).copy(
                projectRole = createProjectRole(2L).copy(
                    project = createProject(id = 2L).copy(
                        organization = createOrganization(2L)
                    )
                )
            )
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(ActivityPredicates.ALL.and(ActivityPredicates.organizationId(1L)))
        assertTrue(actualActivities.isNotEmpty())
        assertTrue(actualActivities.all { it.projectRole.project.organization.id == 1L })
    }


    @Test
    fun `test findAll with Specification date between condition`() {

        val activity = createActivity().copy(id = null)
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(1)

        val expectedActivityAtStart = activity.copy(
            id = null, start = startDate.atStartOfDay().minusDays(1), end = startDate.atStartOfDay()
        )

        val expectedActivityAtMiddle = activity.copy(
            id = null, start = startDate.atStartOfDay(), end = endDate.atTime(23, 59, 59)
        )

        val expectedActivityAtEnd = activity.copy(
            id = null, start = endDate.atTime(23, 59, 59).minusSeconds(1),
            end = endDate.atTime(23, 59, 59).plusDays(1)
        )

        val activitiesToSave = listOf(
            activity.copy(
                id = null, start = startDate.atStartOfDay().minusDays(1), end = startDate.atStartOfDay().minusSeconds(1)
            ), activity.copy(
                id = null,
                start = endDate.atTime(23, 59, 59).plusSeconds(1),
                end = endDate.atTime(23, 59, 59).plusDays(1)
            ), expectedActivityAtStart, expectedActivityAtMiddle, expectedActivityAtEnd
        )

        activityDao.saveAll(activitiesToSave)

        val activitySpecification = ActivityPredicates.ALL.and(
            ActivityPredicates.endDateGreaterThanOrEqualTo(startDate)
                .and(ActivityPredicates.startDateLessThanOrEqualTo(endDate))
        )

        val actualActivities = activityDao.findAll(activitySpecification)

        assertEquals(3, actualActivities.size)

        assertEquals(expectedActivityAtStart.start, actualActivities[0].start)
        assertEquals(expectedActivityAtStart.end, actualActivities[0].end)

        assertEquals(expectedActivityAtMiddle.start, actualActivities[1].start)
        assertEquals(expectedActivityAtMiddle.end, actualActivities[1].end)

        assertEquals(expectedActivityAtEnd.start, actualActivities[2].start)
        assertEquals(expectedActivityAtEnd.end, actualActivities[2].end)
    }
}