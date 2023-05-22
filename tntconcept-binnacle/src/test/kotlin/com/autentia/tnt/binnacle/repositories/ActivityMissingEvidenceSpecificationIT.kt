package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.time.LocalDate
import java.util.*


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityMissingEvidenceSpecificationIT {

    private companion object {
        // Everything for organization with ID = 3

        // Roles for project with id = 6
        private const val project_1_role_weekly = 6L
        private const val project_1_role_once = 7L

        // Roles for project with id = 7
        private const val project_2_role_weekly = 8L
        private const val project_2_role_once = 9L
        private const val project_2_role_none = 10L

        // Users
        private const val test_user_1 = 11L
        private const val test_user_2 = 12L
        private val activeUsers = listOf(test_user_1, test_user_2)

        // Fixed data
        private val today = LocalDate.now()
        private val yesterday = today.minusDays(1)

        private val activitiesMissingEvidencePredicate: Specification<Activity> = PredicateBuilder.and(
            PredicateBuilder.or(
                ActivityPredicates.missingEvidenceOnce(), PredicateBuilder.and(
                    ActivityPredicates.missingEvidenceWeekly(), ActivityPredicates.startDateBetweenDates(
                        DateInterval.of(
                            LocalDate.now().minusDays(7), LocalDate.now()
                        )
                    )
                )
            ), ActivityPredicates.belongsToUsers(activeUsers)
        )
    }


    // Data test
    private lateinit var project1RoleWeekly: ProjectRole
    private lateinit var project1RoleOnce: ProjectRole

    private lateinit var project2RoleWeekly: ProjectRole
    private lateinit var project2RoleOnce: ProjectRole
    private lateinit var project2RoleNone: ProjectRole

    @Inject
    private lateinit var projectRoleDao: ProjectRoleDao

    @Inject
    private lateinit var activityDao: ActivityDao

    @BeforeAll
    fun `obtain data test references`() {
        project1RoleWeekly = projectRoleDao.findById(project_1_role_weekly).orElseThrow { IllegalStateException() }
        project1RoleOnce = projectRoleDao.findById(project_1_role_once).orElseThrow { IllegalStateException() }

        project2RoleWeekly = projectRoleDao.findById(project_2_role_weekly).orElseThrow { IllegalStateException() }
        project2RoleOnce = projectRoleDao.findById(project_2_role_once).orElseThrow { IllegalStateException() }
        project2RoleNone = projectRoleDao.findById(project_2_role_none).orElseThrow { IllegalStateException() }
    }


    @Test
    fun `should find activities without evidence and required evidence once`() {
        // Setup
        val activityWithoutEvidence = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 2",
            projectRole = project1RoleOnce,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        activityDao.save(activityWithoutEvidence)

        // Search
        val predicate = ActivityPredicates.missingEvidenceOnce()
        val result = activityDao.findAll(predicate)

        assertThat(result).containsExactlyInAnyOrder(activityWithoutEvidence)
    }

    @Test
    fun `should not find activities that not require evidence once`() {
        val activityRequiresWeekly = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 4",
            projectRole = project2RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityNotRequireEvidence = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 5",
            projectRole = project2RoleNone,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        activityDao.saveAll(listOf(activityRequiresWeekly, activityNotRequireEvidence))

        val predicate = ActivityPredicates.missingEvidenceOnce()
        val activities = activityDao.findAll(predicate)

        assertThat(activities).isEmpty()
    }

    @Test
    fun `should not find activities that have evidence with required once`() {
        val activityWithEvidence = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "Test activity 3",
            projectRole = project1RoleOnce,
            userId = test_user_2,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.PENDING
        )
        val activityWithAutogeneratedEvidence = Activity(
            start = yesterday.atTime(8, 0, 0),
            end = yesterday.atTime(17, 0, 0),
            duration = 540,
            description = "###Autocreated evidence###\n(DO NOT DELETE)\n ETC. ETC.",
            projectRole = project1RoleOnce,
            userId = test_user_2,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        activityDao.saveAll(listOf(activityWithEvidence, activityWithAutogeneratedEvidence))

        val predicate = ActivityPredicates.missingEvidenceOnce()
        val activities = activityDao.findAll(predicate)

        assertThat(activities).isEmpty()
    }

    @Test
    fun `should find activities missing evidence with required evidence weekly`() {
        val activityWithEvidence = Activity(
            start = today.atTime(8, 0, 0).minusDays(3),
            end = today.atTime(17, 0, 0).minusDays(3),
            duration = 540,
            description = "###Autocreated evidence###\n(DO NOT DELETE)\n ETC. ETC.",
            projectRole = project2RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityHasPreviousEvidence = Activity(
            start = today.atTime(8, 0, 0),
            end = today.atTime(17, 0, 0),
            duration = 540,
            description = "This should NOT be returned",
            projectRole = project2RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityEvidenceTooOld = Activity(
            start = today.atTime(8, 0, 0).minusDays(8),
            end = today.atTime(17, 0, 0).minusDays(8),
            duration = 540,
            description = "###Autocreated evidence###\n(DO NOT DELETE)\n ETC. ETC.",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityWithoutEvidence = Activity(
            start = today.atTime(8, 0, 0),
            end = today.atTime(17, 0, 0),
            duration = 540,
            description = "This should be returned",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        activityDao.saveAll(
            listOf(
                activityWithEvidence,
                activityEvidenceTooOld,
                activityHasPreviousEvidence,
                activityWithoutEvidence,
            )
        )

        val predicate = ActivityPredicates.missingEvidenceWeekly()
        val results = activityDao.findAll(predicate)
        val expectedResults: List<Activity> = listOf(activityWithoutEvidence)

        assertThat(results).containsExactlyInAnyOrderElementsOf(expectedResults)
    }

    @Test
    fun `should find only activity with role once missing evidence with activities missing evidence predicate`() {
        val activityWithoutEvidence1 = Activity(
            start = today.atTime(8, 0, 0).minusDays(4),
            end = today.atTime(17, 0, 0).minusDays(4),
            duration = 540,
            description = "Activity 1",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityWithoutEvidence2 = Activity(
            start = today.atTime(8, 0, 0).minusDays(3),
            end = today.atTime(17, 0, 0).minusDays(3),
            duration = 540,
            description = "Activity 1",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val activityWithEvidence1 = Activity(
            start = today.atTime(8, 0, 0).minusDays(2),
            end = today.atTime(17, 0, 0).minusDays(2),
            duration = 540,
            description = "Activity 2",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.PENDING
        )
        val activityWithoutEvidence3 = Activity(
            start = today.atTime(8, 0, 0).minusDays(2),
            end = today.atTime(17, 0, 0).plusDays(1),
            duration = 4,
            description = "Activity 3",
            projectRole = project1RoleOnce,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        val activityWithEvidence4 = Activity(
            start = today.atTime(8, 0, 0).minusDays(5),
            end = today.atTime(17, 0, 0).minusDays(3),
            duration = 2,
            description = "Activity 3",
            projectRole = project1RoleOnce,
            userId = test_user_1,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.PENDING
        )

        activityDao.saveAll(
            listOf(
                activityWithoutEvidence1,
                activityWithoutEvidence2,
                activityWithoutEvidence3,
                activityWithEvidence4,
                activityWithEvidence1
            )
        )

        val results = activityDao.findAll(activitiesMissingEvidencePredicate)

        assertThat(results).containsExactly(activityWithoutEvidence3)
    }

    @Test
    fun `should not find activities missing evidence because evidence is registered in the previous 7 days`() {
        val act1 = Activity(
            start = today.atTime(8, 0, 0).minusDays(10),
            end = today.atTime(17, 0, 0).minusDays(10),
            duration = 540,
            description = "Activity 1",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val act2 = Activity(
            start = today.atTime(8, 0, 0).minusDays(5),
            end = today.atTime(17, 0, 0).minusDays(5),
            duration = 540,
            description = "Activity 2",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.PENDING
        )
        val act3 = Activity(
            start = today.atTime(8, 0, 0).minusDays(4),
            end = today.atTime(17, 0, 0).minusDays(4),
            duration = 540,
            description = "Activity 1",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val act4 = Activity(
            start = today.atTime(8, 0, 0),
            end = today.atTime(17, 0, 0),
            duration = 540,
            description = "Activity 2",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        activityDao.saveAll(listOf(act1, act2, act3, act4))

        val results = activityDao.findAll(activitiesMissingEvidencePredicate)

        assertThat(results).isEmpty()
    }


    @Test
    fun `should only return activities missing evidence from multiple users`() {
        val act1 = Activity(
            start = today.atTime(8, 0, 0).minusDays(5),
            end = today.atTime(17, 0, 0).minusDays(5),
            duration = 540,
            description = "Activity 1",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val act2 = Activity(
            start = today.atTime(8, 0, 0).minusDays(4),
            end = today.atTime(17, 0, 0).minusDays(4),
            duration = 540,
            description = "Activity 1",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val act3 = Activity(
            start = today.atTime(8, 0, 0).minusDays(5),
            end = today.atTime(17, 0, 0).minusDays(5),
            duration = 540,
            description = "Activity 22",
            projectRole = project1RoleWeekly,
            userId = test_user_2,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.PENDING
        )
        val act4 = Activity(
            start = today.atTime(8, 0, 0).minusDays(4),
            end = today.atTime(17, 0, 0).minusDays(4),
            duration = 540,
            description = "###Autocreated evidence###\n (DO NOT DELETE)\n ETC. ETC.",
            projectRole = project1RoleWeekly,
            userId = test_user_2,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        val act5 = Activity(
            start = today.atTime(8, 0, 0).minusDays(3),
            end = today.atTime(17, 0, 0).minusDays(4),
            duration = 1,
            description = "Activity 2123242",
            projectRole = project2RoleOnce,
            userId = test_user_1,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.PENDING
        )

        activityDao.saveAll(listOf(act1, act2, act3, act4, act5))

        val results = activityDao.findAll(activitiesMissingEvidencePredicate)
        val expected = listOf(act1, act2)

        assertThat(results).containsExactlyInAnyOrderElementsOf(expected)
    }

    @Test
    fun `should not return weekly evidence activities more than a week ago`() {
        val act1 = Activity(
            start = today.atTime(8, 0, 0).minusDays(15),
            end = today.atTime(17, 0, 0).minusDays(15),
            duration = 540,
            description = "Activity 1",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val act2 = Activity(
            start = today.atTime(8, 0, 0).minusDays(14),
            end = today.atTime(17, 0, 0).minusDays(14),
            duration = 540,
            description = "Activity 1",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val act3 = Activity(
            start = today.atTime(8, 0, 0).minusDays(13),
            end = today.atTime(17, 0, 0).minusDays(13),
            duration = 540,
            description = "Activity 1",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        activityDao.saveAll(listOf(act1, act2, act3))

        val results = activityDao.findAll(activitiesMissingEvidencePredicate)

        assertThat(results).isEmpty()
    }

    @Test
    fun `should return missing evidence activity the day before when the last evidence was more than 7 days ago`() {
        val act1 = Activity(
            start = today.atTime(8, 0, 0).minusDays(9),
            end = today.atTime(17, 0, 0).minusDays(9),
            duration = 540,
            description = "###Autocreated evidence###\n (DO NOT DELETE)\n ETC. ETC.",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val act2 = Activity(
            start = today.atTime(8, 0, 0).minusDays(1),
            end = today.atTime(17, 0, 0).minusDays(1),
            duration = 540,
            description = "Activity 1",
            projectRole = project1RoleWeekly,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )

        activityDao.saveAll(listOf(act1, act2))

        val results = activityDao.findAll(activitiesMissingEvidencePredicate)

        assertThat(results).containsExactly(act2)
    }

    @Test
    fun `should return missing evidence activities once`() {
        val act1 = Activity(
            start = today.atTime(8, 0, 0).minusDays(102),
            end = today.atTime(17, 0, 0).minusDays(100),
            duration = 3,
            description = "Activity 1",
            projectRole = project1RoleOnce,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val act2 = Activity(
            start = today.atTime(8, 0, 0).minusDays(5),
            end = today.atTime(17, 0, 0).minusDays(4),
            duration = 2,
            description = "Activity 2",
            projectRole = project1RoleOnce,
            userId = test_user_1,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.PENDING
        )
        val act3 = Activity(
            start = today.atTime(8, 0, 0).minusDays(20),
            end = today.atTime(17, 0, 0).minusDays(19),
            duration = 2,
            description = "Activity 2123123",
            projectRole = project1RoleOnce,
            userId = test_user_1,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.PENDING
        )

        activityDao.saveAll(listOf(act1, act2, act3))
        val results = activityDao.findAll(activitiesMissingEvidencePredicate)
        assertThat(results).containsExactly(act1, act2)
    }

}