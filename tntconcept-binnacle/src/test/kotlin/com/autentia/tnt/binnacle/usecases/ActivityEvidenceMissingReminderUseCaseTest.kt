package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.*
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.services.ActivityEvidenceMissingMailService
import com.autentia.tnt.binnacle.services.UserService
import org.junit.jupiter.api.*
import org.mockito.kotlin.*
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
internal class ActivityEvidenceMissingReminderUseCaseTest {

    private val activityRepository: ActivityRepository = mock()
    private val activityEvidenceMissingMailService: ActivityEvidenceMissingMailService = mock()
    private val userService: UserService = mock()

    private val activityEvidenceMissingReminderUseCase = ActivityEvidenceMissingReminderUseCase(
        activityRepository, activityEvidenceMissingMailService, userService
    )

    @BeforeEach
    fun setMocks() {
        doReturn(getListOfActiveUsers()).`when`(this.userService).findActive()
    }

    @AfterEach
    fun resetMocks() = reset(activityRepository, activityEvidenceMissingMailService, userService)

    @Test
    fun `No activities for user-role without evidence should not call email service`() {
        // Given: No activities for user-role without evidence
        doNothing().whenever(activityEvidenceMissingMailService).sendEmail(any(), any(), any(), any(), any(), any())
        doReturn(emptyList<Activity>()).whenever(activityRepository).findAll(any())

        // When: Use case is called
        activityEvidenceMissingReminderUseCase.sendReminders()

        // Then: Verify no email is sent
        verifyNoInteractions(activityEvidenceMissingMailService)
    }

    @Test
    fun `Activity found for user-role without evidence should call email service`() {
        // Given: One activity for user-role without evidence
        doNothing().whenever(activityEvidenceMissingMailService).sendEmail(any(), any(), any(), any(), any(), any())
        val act1 =
            createActivitySameDay(
                "Activity 1",
                userFirst,
                projectARoleWeekly,
                LocalDateTime.now().minusDays(1).withHour(8),
                3
            )
        doReturn(listOf(act1)).whenever(activityRepository).findAll(any())

        // When: Use case is called
        activityEvidenceMissingReminderUseCase.sendReminders()

        // Then: Verify email is sent
        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectA.organization.name),
            eq(projectA.name),
            eq(projectARoleWeekly.name),
            eq(projectARoleWeekly.requireEvidence),
            eq(userFirst.email),
            any()
        )
    }


    private companion object {
        private var incrementalId: Long = 1

        private fun getIncrementalId(): Long {
            val provideId = incrementalId;
            incrementalId++;
            return provideId;
        }

        private val organizationA = createOrganization().copy(id = 1L, name = "Organization A")

        private val projectA = createProject().copy(1L, name = "Project A", organization = organizationA)
        private val projectB = createProject().copy(id = 2L, name = "Project B", organization = organizationA)

        private val projectARoleWeekly = createProjectRole().copy(
            id = 2L,
            name = "projectARoleWeekly",
            requireEvidence = RequireEvidence.WEEKLY,
            project = projectA,
            isApprovalRequired = true,
            maxAllowed = 2,
            timeUnit = TimeUnit.DAYS,
            isWorkingTime = true
        )

        private val projectARoleOnce = createProjectRole().copy(
            id = 2L,
            name = "projectARoleOnce",
            requireEvidence = RequireEvidence.ONCE,
            project = projectA,
            isApprovalRequired = true,
            maxAllowed = 10,
            timeUnit = TimeUnit.DAYS,
            isWorkingTime = true
        )

        private val projectBRoleWeekly = createProjectRole().copy(
            id = 2L,
            name = "projectBRoleWeekly",
            requireEvidence = RequireEvidence.WEEKLY,
            project = projectB,
            isApprovalRequired = true,
            maxAllowed = 2,
            timeUnit = TimeUnit.DAYS,
            isWorkingTime = true
        )

        private val userFirst = createUser().copy(id = 2, email = "userFirst@example.com")
        private val userSecond = createUser().copy(id = 3, email = "userSecond@example.com")
        private val userThird = createUser().copy(id = 4, email = "userThird@example.com")


        fun createActivitySameDay(
            name: String,
            user: User,
            role: ProjectRole,
            date: LocalDateTime,
            durationInHours: Long,
            hasEvidence: Boolean = false
        ) {
            createActivity(role).copy(
                getIncrementalId(),
                userId = user.id,
                hasEvidences = hasEvidence,
                start = date,
                end = date.plusHours(durationInHours),
                description = name
            )
        }


        private fun buildActivitiesPredicate(allUserIds: List<Long>) = PredicateBuilder.and(
            PredicateBuilder.and(
                ActivityPredicates.hasNotEvidence(),
                ActivityPredicates.projectRoleRequiresEvidence(RequireEvidence.ONCE)
            ), ActivityPredicates.belongsToUsers(allUserIds)
        )

        private fun getListOfActiveUsers(): List<User> {
            return listOf(userFirst, userSecond, userThird);
        }

    }


}