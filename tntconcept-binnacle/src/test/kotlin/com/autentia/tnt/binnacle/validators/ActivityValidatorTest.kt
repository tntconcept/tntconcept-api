package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.exception.ActivityBeforeHiringDateException
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ActivityPeriodClosedException
import com.autentia.tnt.binnacle.exception.BinnacleException
import com.autentia.tnt.binnacle.exception.MaxHoursPerRoleException
import com.autentia.tnt.binnacle.exception.OverlapsAnotherTimeException
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.kotlin.mock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.util.Optional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@TestInstance(PER_CLASS)
internal class ActivityValidatorTest {

    private val activityRepository = mock<ActivityRepository>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()

    private val activityValidator = ActivityValidator(activityRepository, projectRoleRepository)

    @TestInstance(PER_CLASS)
    @Nested
    inner class CheckActivityIsValidForCreation {
        @Test
        fun `do nothing when activity is valid`() {

            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(projectRole.id)

            activityValidator.checkActivityIsValidForCreation(newActivityInMarch, user)
        }

        private fun exceptionProvider() = arrayOf(
            arrayOf(
                "ProjectRoleNotFoundException",
                newActivityInClosedProject,
                closedProjectRole,
                user,
                ProjectClosedException()
            ),
            arrayOf(
                "ActivityPeriodClosedException",
                newActivityTwoYearsAgo,
                projectRole,
                user,
                ActivityPeriodClosedException()
            ),
            arrayOf(
                "ActivityBeforeHiringDateException",
                newActivityBeforeHiringDate,
                projectRole,
                userHiredLastYear,
                ActivityBeforeHiringDateException()
            ),
        )

        @ParameterizedTest
        @MethodSource("exceptionProvider")
        fun `throw exceptions`(
            testDescription: String,
            activityRequestBody: ActivityRequestBody,
            projectRole: ProjectRole,
            user: User,
            expectedException: BinnacleException,
        ) {

            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(projectRole.id)

            val exception = assertThrows<BinnacleException> {
                activityValidator.checkActivityIsValidForCreation(activityRequestBody, user)
            }

            assertEquals(expectedException.message, exception.message)
        }


        @Test
        fun `throw ProjectRoleNotFoundException with role id when project role is not in the database`() {

            doReturn(Optional.empty<ProjectRole>()).whenever(projectRoleRepository).findById(projectRole.id)

            val exception = assertThrows<ProjectRoleNotFoundException> {
                activityValidator.checkActivityIsValidForCreation(newActivityInMarch, user)
            }
            assertEquals(projectRole.id, exception.id)
        }

        @Test
        fun `do nothing when activity started last year`() {

            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(projectRole.id)

            activityValidator.checkActivityIsValidForCreation(newActivityLastYear, user)
        }

        @Test
        fun `throw OverlapsAnotherTimeException when there is already an activity of that user at the same time`() {
            val newActivity = ActivityRequestBody(
                null,
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45),
                75,
                "New activity",
                false,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(
                listOf(
                    Activity(
                        1,
                        LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                        120,
                        "Other activity",
                        projectRole,
                        user.id,
                        false,
                        approvalState = ApprovalState.NA
                    )
                )
            ).whenever(activityRepository).getActivitiesBetweenDate(
                LocalDateTime.of(2022, Month.JULY, 7, 0, 0, 0),
                LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),
                user.id
            )

            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(projectRole.id)

            assertThrows<OverlapsAnotherTimeException> {
                activityValidator.checkActivityIsValidForCreation(newActivity, user)
            }
        }

        private fun maxHoursRoleLimitProviderCreate() = arrayOf(
            arrayOf(
                "reached limit no remaining hours",
                listOf(activityReachedLimitTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(startDate = todayDateTime, duration = (HOUR * 9)),
                0.0
            ),
            arrayOf(
                "reached limit no remaining hours current day",
                listOf(activityReachedLimitTodayTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(startDate = todayDateTime, duration = (HOUR * 9)),
                0.0
            ),
            arrayOf(
                "reached limit no remaining hours half hour",
                listOf(activityReachedHalfHourTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = 90),
                createActivityRequestBody(startDate = todayDateTime, duration = (HOUR * 9)),
                0.5
            ),
            arrayOf(
                "not reached limit remaining hours left",
                listOf(activityNotReachedLimitTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(startDate = todayDateTime, duration = (HOUR * 10)),
                3.0
            ),
        )

        @ParameterizedTest
        @MethodSource("maxHoursRoleLimitProviderCreate")
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`(
            testDescription: String,
            activitiesThisYear: List<ActivityTimeOnly>,
            projectRoleLimited: ProjectRole,
            activityRequestBody: ActivityRequestBody,
            expectedRemainingHours: Double
        ) {

            doReturn(Optional.of(projectRoleLimited)).whenever(projectRoleRepository).findById(projectRoleLimited.id)

            doReturn(activitiesThisYear)
                .whenever(activityRepository).workedMinutesBetweenDate(firstDayOfYear, lastDayOfYear, user.id)

            val exception = assertThrows<MaxHoursPerRoleException> {
                activityValidator.checkActivityIsValidForCreation(activityRequestBody, user)
            }

            assertEquals(projectRoleLimited.maxAllowed / DECIMAL_HOUR, exception.maxAllowedHours)
            assertEquals(expectedRemainingHours, exception.remainingHours)

        }
    }

    @TestInstance(PER_CLASS)
    @Nested
    inner class CheckActivityIsValidForUpdate {
        @Test
        fun `do nothing when activity is valid`() {
            doReturn(Optional.of(currentActivity)).whenever(activityRepository).findById(1L)

            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(any())

            activityValidator.checkActivityIsValidForUpdate(validActivityToUpdate, user)
        }

        @Test
        fun `throw ActivityNotFoundException with activity id when the activity to be replaced does not exist`() {

            doReturn(Optional.empty<Activity>()).whenever(activityRepository).findById(1L)
            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(any())

            val exception = assertThrows<ActivityNotFoundException> {
                activityValidator.checkActivityIsValidForUpdate(activityUpdateNonexistentID, user)
            }
            assertEquals(1L, exception.id)
        }

        @Test
        fun `throw ProjectRoleNotFoundException with role id when project role is not in the database`() {
            val newActivity = ActivityRequestBody(
                1L,
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                60,
                "description",
                false,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
                23,
                "Old description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.empty<ProjectRole>()).whenever(projectRoleRepository).findById(projectRole.id)
            doReturn(Optional.of(currentActivity)).whenever(activityRepository).findById(1L)

            val exception = assertThrows<ProjectRoleNotFoundException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, user)
            }
            assertEquals(projectRole.id, exception.id)
        }

        @Test
        fun `throw UserPermissionException when authenticated user is not the same who created the original activity`() {

            doReturn(Optional.of(currentActivityAnotherUser)).whenever(activityRepository).findById(1L)

            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(any())

            assertThrows<UserPermissionException> {
                activityValidator.checkActivityIsValidForUpdate(newActivityRequest, user)
            }
        }

        @Test
        fun `throw ProjectClosedException when chosen project is already closed`() {
            doReturn(Optional.of(closedProjectRole)).whenever(projectRoleRepository).findById(any())

            val newActivity = ActivityRequestBody(
                1L,
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                60,
                "description",
                false,
                2L,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.of(currentActivity)).whenever(activityRepository).findById(1L)
            doReturn(Optional.of(closedProjectRole)).whenever(projectRoleRepository).findById(any())

            assertThrows<ProjectClosedException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, user)
            }
        }

        @Test
        fun `do nothing when updated activity started last year`() {

            doReturn(Optional.of(currentActivity)).whenever(activityRepository).findById(1L)
            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(any())

            activityValidator.checkActivityIsValidForUpdate(activityLastYear, user)
        }

        @Test
        fun `throw ActivityPeriodClosedException when updated activity started more than one year ago`() {
            doReturn(Optional.of(currentActivity)).whenever(activityRepository).findById(1L)

            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(any())

            assertThrows<ActivityPeriodClosedException> {
                activityValidator.checkActivityIsValidForUpdate(activityUpdateTwoYearsAgo, user)
            }
        }

        @Test
        fun `throw OverlapsAnotherTimeException when there is already an activity of that user at the same time`() {
            val newActivity = ActivityRequestBody(
                1L,
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0),
                75,
                "description",
                false,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )
            given(activityRepository.findById(1L)).willReturn(Optional.of(currentActivity))

            given(
                activityRepository.getActivitiesBetweenDate(
                    LocalDateTime.of(2022, Month.JULY, 7, 0, 0, 0),
                    LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),
                    user.id
                )
            ).willReturn(
                listOf(
                    Activity(
                        33,
                        LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                        120,
                        "Other activity",
                        projectRole,
                        user.id,
                        billable = false,
                        hasImage = false,
                        approvalState = ApprovalState.NA
                    )
                )
            )
            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(projectRole.id)

            assertThrows<OverlapsAnotherTimeException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, user)
            }
        }

        private fun maxHoursRoleLimitProviderUpdate() = arrayOf(
                arrayOf(
                    "reached limit no remaining hours",
                    listOf(activityReachedLimitTimeOnly, activityNoLimitTimeOnly),
                    createProjectRoleWithLimit(maxAllowed = HOUR * 8),
                    activityReachedLimitUpdate,
                    createActivityRequestBodyToUpdate(
                        id = activityReachedLimitUpdate.id!!,
                        startDate = todayDateTime,
                        duration = HOUR * 9
                    ),
                    0.0
                ),
                arrayOf(
                    "not reached limit remaining hours left",
                    listOf(activityNotReachedLimitTimeOnly, activityNoLimitTimeOnly),
                    createProjectRoleWithLimit(maxAllowed = HOUR * 8),
                    activityNotReachedLimitUpdate,
                    createActivityRequestBodyToUpdate(
                        id = activityNotReachedLimitUpdate.id!!,
                        startDate = todayDateTime,
                        duration = HOUR * 10
                    ),
                    3.0
                ),
            )

        @ParameterizedTest
        @MethodSource("maxHoursRoleLimitProviderUpdate")
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`(
            testDescription: String,
            activitiesThisYear: List<ActivityTimeOnly>,
            projectRoleLimited: ProjectRole,
            currentActivity: Activity,
            activityRequestBodyToUpdate: ActivityRequestBody,
            expectedRemainingHours: Double
        ) {

            doReturn(activitiesThisYear)
                .whenever(activityRepository).workedMinutesBetweenDate(firstDayOfYear, lastDayOfYear, user.id)

            doReturn(Optional.of(currentActivity)).whenever(activityRepository)
                .findById(currentActivity.id!!)

            doReturn(Optional.of(projectRoleLimited)).whenever(projectRoleRepository).findById(projectRoleLimited.id)

            val exception = assertThrows<MaxHoursPerRoleException> {
                activityValidator.checkActivityIsValidForUpdate(activityRequestBodyToUpdate, user)
            }

            assertEquals(projectRoleLimited.maxAllowed / DECIMAL_HOUR, exception.maxAllowedHours)
            assertEquals(expectedRemainingHours, exception.remainingHours)

        }

        @Test
        fun `not fail when the activity whose time is overlapped is the activity to be replaced`() {
            val newActivity = ActivityRequestBody(
                1L,
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0),
                75,
                "New description",
                false,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                23,
                "Old description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )
            given(activityRepository.findById(1L)).willReturn(Optional.of(currentActivity))

            given(
                activityRepository.getActivitiesBetweenDate(
                    LocalDateTime.of(2022, Month.JULY, 7, 0, 0, 0),
                    LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),
                    user.id
                )
            ).willReturn(
                listOf(
                    Activity(
                        1L,
                        LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                        23,
                        "Other activity",
                        projectRole,
                        user.id,
                        billable = false,
                        hasImage = false,
                        approvalState = ApprovalState.NA
                    )
                )
            )
            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(projectRole.id)

            activityValidator.checkActivityIsValidForUpdate(newActivity, user)
        }

        @Test
        fun `throw ActivityBeforeHiringDateException when updated activity starting date is before that user hiring date`() {

            val newActivity = ActivityRequestBody(
                1,
                LocalDateTime.of(
                    userHiredLastYear.hiringDate.year,
                    userHiredLastYear.hiringDate.month.minus(1),
                    1,
                    2,
                    1
                ),
                HOUR,
                "Updated activity",
                false,
                userHiredLastYear.id,
                false,
                approvalState = ApprovalState.NA
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month, 3, 2, 1),
                23,
                "Old description",
                projectRole,
                userHiredLastYear.id,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.of(currentActivity)).whenever(activityRepository).findById(1L)
            doReturn(Optional.of(projectRole)).whenever(projectRoleRepository).findById(any())

            assertThrows<ActivityBeforeHiringDateException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, userHiredLastYear)
            }
        }

    }

    @Nested
    inner class CheckActivityIsValidForDeletion {

        @Test
        fun `do nothing when activity to delete is valid`() {
            val id = 1L
            val activity = Activity(
                id,
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.of(activity)).whenever(activityRepository).findById(id)

            activityValidator.checkActivityIsValidForDeletion(id, user)
        }

        @Test
        fun `throw ActivityNotFoundException with id when activity is not in the database`() {
            val id = 1L

            given(activityRepository.findById(id)).willReturn(Optional.empty())

            val exception = assertThrows<ActivityNotFoundException> {
                activityValidator.checkActivityIsValidForDeletion(id, user)
            }
            assertEquals(id, exception.id)
        }

        @Test
        fun `do nothing when activity started last year`() {
            val id = 1L
            val activity = Activity(
                id,
                someYearsAgoLocalDateTime(1),
                HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.of(activity)).whenever(activityRepository).findById(id)

            activityValidator.checkActivityIsValidForDeletion(id, user)
        }

        @Test
        fun `throw ActivityPeriodClosedException when activity started more than one year ago`() {
            val id = 1L
            val activity = Activity(
                id,
                someYearsAgoLocalDateTime(2),
                HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.of(activity)).whenever(activityRepository).findById(id)

            assertThrows<ActivityPeriodClosedException> {
                activityValidator.checkActivityIsValidForDeletion(id, user)
            }
        }

        @Test
        fun `throw UserPermissionException when user is not the creator of the activity`() {
            val id = 1L
            val activity = Activity(
                id,
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                HOUR,
                "description",
                projectRole,
                33L,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.of(activity)).whenever(activityRepository).findById(id)

            assertThrows<UserPermissionException> {
                activityValidator.checkActivityIsValidForDeletion(id, user)
            }
        }
    }


    private companion object {

        private val user = createUser()
        private val today = LocalDate.now()
        private val userHiredLastYear = createUser(LocalDate.of(today.year - 1, Month.FEBRUARY, 22))

        private const val HOUR = 60
        private const val DECIMAL_HOUR = 60.0

        private val yesterdayDateTime = LocalDateTime.of(LocalDate.now().minusDays(2), LocalTime.now())
        private val todayDateTime =
            LocalDateTime.of(LocalDate.now().year, LocalDate.now().month, LocalDate.now().dayOfMonth, 0, 0)

        private val firstDayOfYear = LocalDateTime.of(LocalDate.now().year, Month.JANUARY, 1, 0, 0)
        private val lastDayOfYear = LocalDateTime.of(LocalDate.now().year, Month.DECEMBER, 31, 23, 59)

        private val vacationProject =
            Project(1, "Vacaciones", true, true, Organization(1, "Organization", emptyList()), emptyList())
        private val permisoProject =
            Project(2, "Vacaciones", true, true, Organization(1, "Organization", emptyList()), emptyList())
        private val projectRole = ProjectRole(1, "vac", false, vacationProject, 0)

        private val projectRoleNoLimit = ProjectRole(2, "perm", false, permisoProject, 0)
        private val projectRoleLimited = ProjectRole(3, "vac", false, vacationProject, (HOUR * 8))

        private val activityNotReachedLimitUpdate = createActivity(
            id = 1L,
            startDate = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            duration = (HOUR * 5),
            projectRole = projectRoleLimited
        )

        private val activityReachedLimitUpdate = createActivity(
            id = 1L,
            startDate = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            duration = projectRoleLimited.maxAllowed,
            projectRole = projectRoleLimited
        )

        val activityReachedLimitTimeOnly = ActivityTimeOnly(
            yesterdayDateTime,
            projectRoleLimited.maxAllowed,
            projectRoleLimited.id
        )

        val activityNoLimitTimeOnly = ActivityTimeOnly(
            yesterdayDateTime,
            (HOUR * 8),
            projectRoleNoLimit.id
        )
        val activityReachedLimitTodayTimeOnly = ActivityTimeOnly(
            todayDateTime,
            projectRoleLimited.maxAllowed,
            projectRoleLimited.id
        )
        val activityReachedHalfHourTimeOnly = ActivityTimeOnly(
            todayDateTime,
            HOUR,
            projectRoleLimited.id
        )
        val activityNotReachedLimitTimeOnly = ActivityTimeOnly(
            todayDateTime,
            (HOUR * 5),
            projectRoleLimited.id
        )

        private val newActivityInMarch = ActivityRequestBody(
            null,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            HOUR,
            "description",
            false,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )
        private const val CLOSED_ID = 2L

        private val newActivityInClosedProject = ActivityRequestBody(
            null,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            HOUR,
            "description",
            false,
            CLOSED_ID,
            false,
            approvalState = ApprovalState.NA
        )
        private val newActivityLastYear = ActivityRequestBody(
            null,
            someYearsAgoLocalDateTime(1),
            HOUR,
            "description",
            false,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )
        private val newActivityTwoYearsAgo = ActivityRequestBody(
            null,
            someYearsAgoLocalDateTime(2),
            HOUR,
            "description",
            false,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )

        private val activityLastYear = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(1),
            HOUR,
            "Updated activity",
            false,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )
        private val activityUpdateNonexistentID = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(2),
            HOUR,
            "Updated activity",
            false,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )

        private val currentActivity = Activity(
            1L,
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
            23,
            "Old description",
            projectRole,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )
        val newActivityRequest = ActivityRequestBody(
            1L,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            HOUR,
            "description",
            false,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )
        val activityUpdateTwoYearsAgo = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(2),
            HOUR,
            "Updated activity",
            false,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )
        private const val anyOtherUserId = 33L

        private val currentActivityAnotherUser = Activity(
            1L,
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
            23,
            "Old description",
            projectRole,
            anyOtherUserId,
            false,
            approvalState = ApprovalState.NA
        )
        private val validActivityToUpdate = ActivityRequestBody(
            1L,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            HOUR,
            "description",
            false,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )

        private val newActivityBeforeHiringDate = ActivityRequestBody(
            null,
            LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month.minus(1), 3, 11, 45),
            HOUR,
            "description",
            false,
            userHiredLastYear.id,
            false,
            approvalState = ApprovalState.NA
        )

        private val closedProject = Project(
            CLOSED_ID, "TNT", false, false,
            Organization(1, "Autentia", emptyList()), emptyList()
        )
        private val closedProjectRole = ProjectRole(CLOSED_ID, "Architect", false, closedProject, 0)

        private fun createActivityRequestBody(
            startDate: LocalDateTime,
            duration: Int,
            description: String = "",
            projectRoleId: Long = projectRoleLimited.id,
            billable: Boolean = false,
            hasImage: Boolean = false,
            approvalState: ApprovalState = ApprovalState.NA
        ): ActivityRequestBody =
            ActivityRequestBody(
                startDate = startDate,
                duration = duration,
                description = description,
                projectRoleId = projectRoleId,
                billable = billable,
                hasImage = hasImage,
                approvalState = approvalState
            )

        private fun createActivityRequestBodyToUpdate(
            id: Long,
            startDate: LocalDateTime,
            duration: Int,
            description: String = "",
            projectRoleId: Long = projectRoleLimited.id,
            billable: Boolean = false,
            hasImage: Boolean = false,
            approvalState: ApprovalState = ApprovalState.NA
        ): ActivityRequestBody =
            ActivityRequestBody(
                id = id,
                startDate = startDate,
                duration = duration,
                description = description,
                projectRoleId = projectRoleId,
                billable = billable,
                hasImage = hasImage,
                approvalState = approvalState
            )

        private fun createActivity(
            id: Long? = null,
            startDate: LocalDateTime,
            duration: Int,
            description: String = "",
            billable: Boolean = false,
            projectRole: ProjectRole,
            userId: Long = user.id,
            approvalState: ApprovalState = ApprovalState.NA
        ) = Activity(
            id = id,
            startDate = startDate,
            duration = duration,
            description = description,
            projectRole = projectRole,
            userId = userId,
            billable = billable,
            approvalState = approvalState
            )

        private val organization = Organization(1, "Organization", listOf())

        fun createProjectRoleWithLimit(
            id: Long = projectRoleLimited.id,
            name: String = "Role with limit",
            requireEvidence: Boolean = false,
            project: Project = Project(1, "Project", true, false, organization, listOf()),
            maxAllowed: Int
        ) = ProjectRole(
            id = id,
            name = name,
            requireEvidence = requireEvidence,
            project = project,
            maxAllowed = maxAllowed,
        )

        private fun someYearsAgoLocalDateTime(yearsAgo: Int) =
            LocalDateTime.of(
                today.year - yearsAgo,
                Month.DECEMBER,
                31,
                23,
                59,
                59
            )
    }

}
