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
internal class ActivityEvidenceMissingReminderUseCaseTest {

    private val activityRepository: ActivityRepository = mock()
    private val activityEvidenceMissingMailService: ActivityEvidenceMissingMailService = mock()
    private val userService: UserService = mock()

    private val activityEvidenceMissingReminderUseCase = ActivityEvidenceMissingReminderUseCase(
        activityRepository, activityEvidenceMissingMailService, userService
    )

    @BeforeEach
    fun setMocks() {
        doReturn(getListOfActiveUsers()).`when`(this.userService).getActiveUsersWithoutSecurity()
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

    @Test
    fun `Activities same role found for user-role without evidence should call once email service`(){
        // Given: Activities for same role without evidence
        doNothing().whenever(activityEvidenceMissingMailService).sendEmail(any(), any(), any(), any(), any(), any())
        val act1 =
            createActivitySameDay(
                "Activity 1",
                userFirst,
                projectARoleWeekly,
                LocalDateTime.now().minusDays(1).withHour(8),
                3
            )
        val act2 =
            createActivitySameDay(
                "Activity 2",
                userFirst,
                projectARoleWeekly,
                LocalDateTime.now().minusDays(1).withHour(16),
                3
            )


        doReturn(listOf(act1, act2)).whenever(activityRepository).findAll(any())

        // When: Use case is called
        activityEvidenceMissingReminderUseCase.sendReminders()

        // Then: Verify only one email is sent
        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectA.organization.name),
            eq(projectA.name),
            eq(projectARoleWeekly.name),
            eq(projectARoleWeekly.requireEvidence),
            eq(userFirst.email),
            any()
        )
    }

    @Test
    fun `Activities for different roles found for role-user without evidence should call email service each evidence`(){
        // Given: Activities for different roles and different require evidence without registered evidence
        doNothing().whenever(activityEvidenceMissingMailService).sendEmail(any(), any(), any(), any(), any(), any())
        val act1 =
            createActivitySameDay(
                "Activity 1",
                userFirst,
                projectARoleWeekly,
                LocalDateTime.now().minusDays(1).withHour(8),
                3
            )
        val act2 =
            createActivitySameDay(
                "Activity 2",
                userFirst,
                projectARoleWeekly,
                LocalDateTime.now().minusDays(1).withHour(16),
                3
            )
        val act3 =
            createActivityInterval(
                "Activity 3",
                userFirst,
                projectARoleOnce,
                LocalDateTime.now().minusDays(1).withHour(16),
                LocalDateTime.now().plusDays(3).withHour(16)
            )

        doReturn(listOf(act1, act2, act3)).whenever(activityRepository).findAll(any())

        // When: Use case is called
        activityEvidenceMissingReminderUseCase.sendReminders()

        // Then: Verify emails are sent
        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectA.organization.name),
            eq(projectA.name),
            eq(projectARoleWeekly.name),
            eq(projectARoleWeekly.requireEvidence),
            eq(userFirst.email),
            any()
        )
        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectA.organization.name),
            eq(projectA.name),
            eq(projectARoleOnce.name),
            eq(projectARoleOnce.requireEvidence),
            eq(userFirst.email),
            any()
        )
    }

    @Test
    fun `Activities for same project and different role found for roles-user same required evidence without evidence should call twice email service`(){
        // Given: Activities for two roles same project without evidence
        doNothing().whenever(activityEvidenceMissingMailService).sendEmail(any(), any(), any(), any(), any(), any())
        val act1 =
            createActivitySameDay(
                "Activity 1",
                userFirst,
                projectARoleWeekly,
                LocalDateTime.now().minusDays(1).withHour(8),
                3
            )
        val act2 =
            createActivitySameDay(
                "Activity 2",
                userSecond,
                projectARoleWeekly2,
                LocalDateTime.now().minusDays(2).withHour(16),
                3
            )

        doReturn(listOf(act1, act2)).whenever(activityRepository).findAll(any())

        // When: Use case is called
        activityEvidenceMissingReminderUseCase.sendReminders()

        // Then: Verify emails are sent
        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectA.organization.name),
            eq(projectA.name),
            eq(projectARoleWeekly.name),
            eq(projectARoleWeekly.requireEvidence),
            eq(userFirst.email),
            any()
        )
        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectA.organization.name),
            eq(projectA.name),
            eq(projectARoleWeekly2.name),
            eq(projectARoleWeekly2.requireEvidence),
            eq(userSecond.email),
            any()
        )
    }

    @Test
    fun `Activities found for different roles-user in same organization without evidence should call twice email service`(){
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
        val act2 =
            createActivitySameDay(
                "Activity 2",
                userFirst,
                projectBRoleWeekly,
                LocalDateTime.now().minusDays(2).withHour(16),
                3
            )

        doReturn(listOf(act1, act2)).whenever(activityRepository).findAll(any())

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
        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectB.organization.name),
            eq(projectB.name),
            eq(projectBRoleWeekly.name),
            eq(projectBRoleWeekly.requireEvidence),
            eq(userFirst.email),
            any()
        )
    }

    @Test
    fun `Activities found for different roles-user in different organization without evidence should call twice email service`() {
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
        val act2 =
            createActivitySameDay(
                "Activity 2",
                userFirst,
                projectCRoleWeeklyOrganizationB,
                LocalDateTime.now().minusDays(2).withHour(16),
                3
            )

        doReturn(listOf(act1, act2)).whenever(activityRepository).findAll(any())

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
        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectC.organization.name),
            eq(projectC.name),
            eq(projectCRoleWeeklyOrganizationB.name),
            eq(projectCRoleWeeklyOrganizationB.requireEvidence),
            eq(userFirst.email),
            any()
        )
    }

    @Test
    fun `Activities without evidences for multiple users in same project role`() {
        // Given: Activities for same role without evidence
        doNothing().whenever(activityEvidenceMissingMailService).sendEmail(any(), any(), any(), any(), any(), any())
        val act11 =
            createActivitySameDay(
                "Activity 1 usf",
                userFirst,
                projectARoleWeekly,
                LocalDateTime.now().minusDays(1).withHour(8),
                3
            )
        val act12 =
            createActivitySameDay(
                "Activity 2 usf",
                userFirst,
                projectARoleWeekly,
                LocalDateTime.now().minusDays(1).withHour(16),
                3
            )

        val act13 =
            createActivityInterval(
                "Activity 2 usf",
                userFirst,
                projectARoleOnce,
                LocalDateTime.now().minusDays(4).withHour(16),
                LocalDateTime.now().minusDays(1).withHour(16),
            )

        val act21 =
            createActivitySameDay(
                "Activity 1 usd",
                userSecond,
                projectARoleWeekly,
                LocalDateTime.now().minusDays(1).withHour(16),
                3
            )
        val act22 =
            createActivitySameDay(
                "Activity 2 usd",
                userSecond,
                projectARoleWeekly,
                LocalDateTime.now().minusDays(1).withHour(10),
                3
            )

        doReturn(listOf(act11, act12, act13, act21, act22)).whenever(activityRepository).findAll(any())

        // When: Use case is called
        activityEvidenceMissingReminderUseCase.sendReminders()

        // Then: Verify email is sent for each user
        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectA.organization.name),
            eq(projectA.name),
            eq(projectARoleWeekly.name),
            eq(projectARoleWeekly.requireEvidence),
            eq(userFirst.email),
            any()
        )

        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectA.organization.name),
            eq(projectA.name),
            eq(projectARoleOnce.name),
            eq(projectARoleOnce.requireEvidence),
            eq(userFirst.email),
            any()
        )

        verify(activityEvidenceMissingMailService).sendEmail(
            eq(projectA.organization.name),
            eq(projectA.name),
            eq(projectARoleWeekly.name),
            eq(projectARoleWeekly.requireEvidence),
            eq(userSecond.email),
            any()
        )
    }

    private companion object {
        private var incrementalId: Long = 1

        private fun getIncrementalId(): Long {
            val provideId = incrementalId
            incrementalId++
            return provideId
        }

        private val organizationA = createOrganization().copy(id = 1L, name = "Organization A")
        private val organizationB = createOrganization().copy(id = 2L, name = "Organization B")

        private val projectA = createProject().copy(1L, name = "Project A", organization = organizationA)
        private val projectB = createProject().copy(id = 2L, name = "Project B", organization = organizationA)
        private val projectC = createProject().copy(id = 2L, name = "Project B", organization = organizationB)

        private val projectARoleWeekly = createProjectRole().copy(
            id = 2L,
            name = "projectARoleWeekly",
            requireEvidence = RequireEvidence.WEEKLY,
            project = projectA,
            isApprovalRequired = true,
            maxTimeAllowedByYear = 2,
            timeUnit = TimeUnit.DAYS,
            isWorkingTime = true
        )

        private val projectARoleWeekly2 = createProjectRole().copy(
            id = 5L,
            name = "projectARoleWeekly TWO",
            requireEvidence = RequireEvidence.WEEKLY,
            project = projectA,
            isApprovalRequired = true,
            maxTimeAllowedByYear = 2,
            timeUnit = TimeUnit.DAYS,
            isWorkingTime = true
        )

        private val projectARoleOnce = createProjectRole().copy(
            id = 3L,
            name = "projectARoleOnce",
            requireEvidence = RequireEvidence.ONCE,
            project = projectA,
            isApprovalRequired = true,
            maxTimeAllowedByYear = 10,
            timeUnit = TimeUnit.DAYS,
            isWorkingTime = true
        )

        private val projectBRoleWeekly = createProjectRole().copy(
            id = 4L,
            name = "projectBRoleWeekly",
            requireEvidence = RequireEvidence.WEEKLY,
            project = projectB,
            isApprovalRequired = true,
            maxTimeAllowedByYear = 2,
            timeUnit = TimeUnit.DAYS,
            isWorkingTime = true
        )
        private val projectCRoleWeeklyOrganizationB = createProjectRole().copy(
            id = 6L,
            name = "projectCRoleWeeklyOrganizationB",
            requireEvidence = RequireEvidence.WEEKLY,
            project = projectC,
            isApprovalRequired = true,
            maxTimeAllowedByYear = 2,
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
        ): Activity {
            return createActivity(role).copy(
                getIncrementalId(),
                userId = user.id,
                hasEvidences = hasEvidence,
                start = date,
                end = date.plusHours(durationInHours),
                description = name
            )
        }

        fun createActivityInterval(
            name: String,
            user: User,
            role: ProjectRole,
            startDate: LocalDateTime,
            endDate: LocalDateTime,
            hasEvidence: Boolean = false
        ): Activity {
            return createActivity(role).copy(
                getIncrementalId(),
                userId = user.id,
                hasEvidences = hasEvidence,
                start = startDate,
                end = endDate,
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
            return listOf(userFirst, userSecond, userThird)
        }

    }


}