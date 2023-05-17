package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createOrganization
import com.autentia.tnt.binnacle.config.createProject
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.repositories.predicates.ActivityMissingEvidenceSpecification
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@MicronautTest(transactionMode = TransactionMode.SEPARATE_TRANSACTIONS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityDaoSpecificationIT {

    @Inject
    private lateinit var activityDao: ActivityDao

    @Inject
    private lateinit var projectRepository: ProjectRepository

    @Inject
    private lateinit var organizationRepository: OrganizationRepository

    @Inject
    private lateinit var projectRoleDao: ProjectRoleDao

    @Test
    fun `test findAll without condition`() {

        val activitiesToSave = listOf(
            createActivity().copy(id = null),
            createActivity().copy(id = null, userId = 1L)
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(ActivityPredicates.ALL)
        assertEquals(2, actualActivities.size)
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
            createActivity().copy(id = null),
            createActivity().copy(id = null, approvalState = ApprovalState.ACCEPTED)
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
            createActivity(null),
            createActivity(null).copy(projectRole = createProjectRole(2L))
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
            (
                    Project(
                        project.id,
                        project.name,
                        false,
                        project.billable,
                        organization.get(),
                        project.projectRoles
                    )
                    )
        )

        val activitiesToSave = listOf(
            createActivity(null),
            createActivity(null).copy(
                projectRole = createProjectRole(2L).copy(
                    project = createProject(id = 2L).copy(
                        organization = createOrganization(2L)
                    )
                )
            )
        )
        activityDao.saveAll(activitiesToSave)

        val actualActivities = activityDao.findAll(ActivityPredicates.ALL.and(ActivityPredicates.organizationId(1L)))
        assertEquals(1, actualActivities.size)
        assertEquals(1L, actualActivities[0].projectRole.project.organization.id)
    }


    @Test
    fun `test findAll with Specification date between condition`() {

        val activity = createActivity().copy(id = null)
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(1)

        val expectedActivityAtStart = activity.copy(
            id = null,
            start = startDate.atStartOfDay().minusDays(1),
            end = startDate.atStartOfDay()
        )

        val expectedActivityAtMiddle = activity.copy(
            id = null,
            start = startDate.atStartOfDay(),
            end = endDate.atTime(23, 59, 59)
        )

        val expectedActivityAtEnd = activity.copy(
            id = null,
            start = endDate.atTime(23, 59, 59).minusSeconds(1),
            end = endDate.atTime(23, 59, 59).plusDays(1)
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
            expectedActivityAtStart,
            expectedActivityAtMiddle,
            expectedActivityAtEnd
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

    @Test
    fun `should find activities without evidence and required evidence once`() {
        val projectRole = projectRoleDao.findById(1L).get()
        val projectRoleEdited = projectRole.copy(requireEvidence = RequireEvidence.ONCE)
        projectRoleDao.update(projectRoleEdited)
        val projectRoleWithoutEvidenceOnce = projectRoleDao.findById(3).get()
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
            description = "Test activity 3",
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
            description = "Test activity 4",
            projectRole = projectRoleWithoutEvidenceOnce,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityWithAutogeneratedEvidence = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "###Autocreated evidence###\n(DO NOT DELETE)\n ETC. ETC.",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        activityDao.saveAll(
            listOf(
                activityWithoutEvidence,
                activityWithEvidence,
                activityWithoutEvidenceNeeded,
                activityWithAutogeneratedEvidence
            )
        )

        val predicate = ActivityPredicates.withoutEvidence()
        val result = activityDao.findAll(predicate)

        val expectedResults = listOf(activityWithoutEvidence)
        assertEquals(expectedResults, result)
    }

    @Test
    fun `should find activities missing evidence with required evidence weekly`() {
        var projectRole: ProjectRole = projectRoleDao.findById(1).orElseThrow { fail() }
        projectRole = projectRole.copy(requireEvidence = RequireEvidence.WEEKLY)
        projectRoleDao.update(projectRole)
        val otherProjectRole: ProjectRole = projectRoleDao.findById(2).orElseThrow { fail() }
        val activityWithEvidence = Activity(
            start = today.atTime(8, 0, 0).minusDays(3),
            end = today.atTime(17, 0, 0).minusDays(3),
            duration = 540,
            description = "###Autocreated evidence###\n(DO NOT DELETE)\n ETC. ETC.",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityHasPreviousEvidence = Activity(
            start = today.atTime(8, 0, 0),
            end = today.atTime(17, 0, 0),
            duration = 540,
            description = "This should NOT be returned",
            projectRole = projectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityEvidenceTooOld = Activity(
            start = today.atTime(8, 0, 0).minusDays(8),
            end = today.atTime(17, 0, 0).minusDays(8),
            duration = 540,
            description = "###Autocreated evidence###\n(DO NOT DELETE)\n ETC. ETC.",
            projectRole = otherProjectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        val activityWithoutEvidence = Activity(
            start = today.atTime(8, 0, 0),
            end = today.atTime(17, 0, 0),
            duration = 540,
            description = "This should be returned",
            projectRole = otherProjectRole,
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        activityDao.saveAll(
            listOf(
                activityWithEvidence,
                activityEvidenceTooOld,
                activityHasPreviousEvidence,
                activityWithoutEvidence
            )
        )

        val predicate = ActivityMissingEvidenceSpecification()

        val results = activityDao.findAll(predicate)

        val expectedResults: List<Activity> = listOf(activityWithoutEvidence)
        assertEquals(expectedResults.size, results.size)
    }

    private companion object {
        private val yesterday = LocalDate.now().minusDays(1)
        private val today = LocalDate.now()
        private const val userId = 1L
    }
}