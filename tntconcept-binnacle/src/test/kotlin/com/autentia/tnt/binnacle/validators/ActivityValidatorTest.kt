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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.util.Optional

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
                LocalDateTime.of(2022, Month.JULY, 7, 10, 0),
                "New activity",
                false,
                projectRole.id,
                projectRole.timeUnit,
                false
            )

            doReturn(
                listOf(
                    Activity(
                        1,
                        LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                        LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),

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
                "reached limit no remaining hours the year before",
                listOf(activityReachedLimitTimeOnlyAYearAgo, activityNoLimitTimeOnlyAYearAgo),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(
                    start = todayDateTime.minusYears(1L),
                    end = todayDateTime.minusYears(1L).plusMinutes(HOUR * 9L)
                ),
                0.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "reached limit no remaining hours",
                listOf(activityReachedLimitTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(start = todayDateTime, end = todayDateTime.plusMinutes(HOUR * 9L)),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "reached limit no remaining hours current day",
                listOf(activityReachedLimitTodayTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(start = todayDateTime, end = todayDateTime.plusMinutes(HOUR * 9L)),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "reached limit no remaining hours half hour",
                listOf(activityReachedHalfHourTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = 90),
                createActivityRequestBody(start = todayDateTime, end = todayDateTime.plusMinutes(HOUR * 9L)),
                0.5,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "not reached limit remaining hours left",
                listOf(activityNotReachedLimitTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(start = todayDateTime, end = todayDateTime.plusMinutes(HOUR * 10L)),
                3.0,
                firstDayOfYear,
                lastDayOfYear
            ),
        )

        @ParameterizedTest
        @MethodSource("maxHoursRoleLimitProviderCreate")
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`(
            testDescription: String,
            activitiesInTheYear: List<ActivityTimeOnly>,
            projectRoleLimited: ProjectRole,
            activityRequestBody: ActivityRequestBody,
            expectedRemainingHours: Double,
            firstDay: LocalDateTime,
            lastDay: LocalDateTime
        ) {

            doReturn(Optional.of(projectRoleLimited)).whenever(projectRoleRepository).findById(projectRoleLimited.id)

            doReturn(activitiesInTheYear)
                .whenever(activityRepository).workedMinutesBetweenDate(firstDay, lastDay, user.id)

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
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                "description",
                false,
                projectRole.id,
                projectRole.timeUnit,
                false,
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
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
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                "description",
                false,
                closedProjectRole.id,
                closedProjectRole.timeUnit,
                false,
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
                LocalDateTime.of(2022, Month.JULY, 7, 10, 0, 0),
                "description",
                false,
                projectRole.id,
                projectRole.timeUnit,
                false,
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
                        LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),
                        "Other activity",
                        projectRole,
                        user.id,
                        billable = false,
                        approvalState = ApprovalState.NA,
                        hasEvidences = false
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
                    "reached limit no remaining hours for activity related to the year before",
                    listOf(activityForLimitedProjectRoleAYearAgo, otherActivityForLimitedProjectRoleAYearAgo),
                    createProjectRoleWithLimit(maxAllowed = HOUR * 8),
                    activityAYearAgoUpdated,
                    createActivityRequestBodyToUpdate(
                        id = activityAYearAgoUpdated.id!!,
                        start = todayDateTime.minusYears(1L),
                        end = todayDateTime.minusYears(1L).plusMinutes((HOUR * 9).toLong())
                    ),
                    0.0,
                    firstDayOfYear.minusYears(1L),
                    lastDayOfYear.minusYears(1L)
                ),
                arrayOf(
                    "reached limit remaining hours left related to the year before",
                    listOf(activityForLimitedProjectRoleAYearAgo, activityNoLimitTimeOnlyAYearAgo),
                    createProjectRoleWithLimit(maxAllowed = HOUR * 8),
                    activityAYearAgoUpdated,
                    createActivityRequestBodyToUpdate(
                        id = activityNotReachedLimitUpdate.id!!,
                        start = todayDateTime.minusYears(1L),
                        end = todayDateTime.minusYears(1L).plusMinutes((HOUR * 10).toLong())
                    ),
                    2.0,
                    firstDayOfYear.minusYears(1L),
                    lastDayOfYear.minusYears(1L)
                ),
                arrayOf(
                    "reached limit no remaining hours",
                    listOf(activityReachedLimitTimeOnly, activityNoLimitTimeOnly),
                    createProjectRoleWithLimit(maxAllowed = HOUR * 8),
                    activityReachedLimitUpdate,
                    createActivityRequestBodyToUpdate(
                        id = activityReachedLimitUpdate.id!!,
                        start = todayDateTime,
                        end = todayDateTime.plusMinutes(HOUR * 9L)
                    ),
                    0.0,
                    firstDayOfYear,
                    lastDayOfYear
                ),
                arrayOf(
                    "not reached limit remaining hours left",
                    listOf(activityNotReachedLimitTimeOnly, activityNoLimitTimeOnly),
                    createProjectRoleWithLimit(maxAllowed = HOUR * 8),
                    activityNotReachedLimitUpdate,
                    createActivityRequestBodyToUpdate(
                        id = activityNotReachedLimitUpdate.id!!,
                        start = todayDateTime,
                        end = todayDateTime.plusMinutes(HOUR * 10L)
                    ),
                    3.0,
                    firstDayOfYear,
                    lastDayOfYear
                ),
            )

        @ParameterizedTest
        @MethodSource("maxHoursRoleLimitProviderUpdate")
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`(
            testDescription: String,
            activitiesInYear: List<ActivityTimeOnly>,
            projectRoleLimited: ProjectRole,
            currentActivity: Activity,
            activityRequestBodyToUpdate: ActivityRequestBody,
            expectedRemainingHours: Double,
            firstDay: LocalDateTime,
            lastDay: LocalDateTime
        ) {

            doReturn(activitiesInYear)
                .whenever(activityRepository).workedMinutesBetweenDate(firstDay, lastDay, user.id)

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
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0).plusMinutes(75),
                "New description",
                false,
                projectRole.id,
                projectRole.timeUnit,
                false,
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
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
                        LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                        "Other activity",
                        projectRole,
                        user.id,
                        billable = false,
                        hasEvidences = false,
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
                LocalDateTime.of(
                    userHiredLastYear.hiringDate.year,
                    userHiredLastYear.hiringDate.month.minus(1),
                    1,
                    2,
                    1
                ).plusMinutes(HOUR.toLong()),
                "Updated activity",
                false,
                projectRole.id,
                projectRole.timeUnit,
                false,
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month, 3, 2, 1),
                LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month, 3, 2, 1),
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
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
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
                someYearsAgoLocalDateTime(1),
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
                someYearsAgoLocalDateTime(2),
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
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
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
        private const val CLOSED_ID = 2L

        private val yesterdayDateTime = LocalDateTime.of(LocalDate.now().minusDays(2), LocalTime.now())
        private val todayDateTime =
            LocalDateTime.of(LocalDate.now().year, LocalDate.now().month, LocalDate.now().dayOfMonth, 0, 0)

        private val firstDayOfYear = LocalDateTime.of(LocalDate.now().year, Month.JANUARY, 1, 0, 0)
        private val lastDayOfYear = LocalDateTime.of(LocalDate.now().year, Month.DECEMBER, 31, 23, 59)

        private val vacationProject =
            Project(1, "Vacaciones", true, true, Organization(1, "Organization", emptyList()), emptyList())
        private val permisoProject =
            Project(2, "Vacaciones", true, true, Organization(1, "Organization", emptyList()), emptyList())
        private val projectRole =
            ProjectRole(1, "vac", RequireEvidence.NO, vacationProject, 0, true, false, TimeUnit.MINUTES)
        private val closedProject =
            Project(CLOSED_ID, "TNT", false, false, Organization(1, "Autentia", emptyList()), emptyList())
        private val closedProjectRole =
            ProjectRole(CLOSED_ID, "Architect", RequireEvidence.NO, closedProject, 0, true, false, TimeUnit.MINUTES)
        private val projectRoleWithoutLimit =
            ProjectRole(2, "perm", RequireEvidence.NO, permisoProject, 0, true, false, TimeUnit.MINUTES)
        private val projectRoleLimited =
            ProjectRole(3, "vac", RequireEvidence.NO, vacationProject, (HOUR * 8), false, false, TimeUnit.MINUTES)

        private val activityNotReachedLimitUpdate = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            projectRole = projectRoleLimited
        )

        private val activityReachedLimitUpdate = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            projectRole = projectRoleLimited
        )

        private val activityAYearAgoUpdated = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now().minusYears(1L), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now().minusYears(1L), LocalTime.now()).plusMinutes((projectRoleLimited.maxAllowed - 120).toLong()),
            projectRole = projectRoleLimited
        )

        private val activityReachedLimitTimeOnly = ActivityTimeOnly(
            yesterdayDateTime,
            yesterdayDateTime.plusMinutes(projectRoleLimited.maxAllowed.toLong()),
            projectRoleLimited.id,
            projectRoleLimited.timeUnit
        )

        private val activityReachedLimitTimeOnlyAYearAgo = ActivityTimeOnly(
            yesterdayDateTime.minusYears(1L),
            yesterdayDateTime.minusYears(1L).plusMinutes(projectRoleLimited.maxAllowed.toLong()),
            projectRoleLimited.id,
            projectRoleLimited.timeUnit
        )

        private val activityForLimitedProjectRoleAYearAgo = ActivityTimeOnly(
            yesterdayDateTime.minusYears(1L),
            yesterdayDateTime.minusYears(1L).plusMinutes(projectRoleLimited.maxAllowed - 120L),
            projectRoleLimited.id,
            projectRoleLimited.timeUnit
        )

        private val otherActivityForLimitedProjectRoleAYearAgo = ActivityTimeOnly(
            yesterdayDateTime.minusYears(1L),
            yesterdayDateTime.minusYears(1L).plusMinutes(120),
            projectRoleLimited.id,
            projectRoleLimited.timeUnit
        )

        private val activityNoLimitTimeOnly = ActivityTimeOnly(
            yesterdayDateTime,
            yesterdayDateTime.plusMinutes(HOUR * 8L),
            projectRoleWithoutLimit.id,
            projectRoleWithoutLimit.timeUnit
        )

        private val activityNoLimitTimeOnlyAYearAgo = ActivityTimeOnly(
            yesterdayDateTime.minusYears(1L),
            yesterdayDateTime.minusYears(1L).plusMinutes(HOUR * 8L),
            projectRoleWithoutLimit.id,
            projectRoleWithoutLimit.timeUnit
        )

        private val activityReachedLimitTodayTimeOnly = ActivityTimeOnly(
            todayDateTime,
            todayDateTime.plusMinutes(projectRoleLimited.maxAllowed.toLong()),
            projectRoleLimited.id,
            projectRoleLimited.timeUnit
        )
        private val activityReachedHalfHourTimeOnly = ActivityTimeOnly(
            todayDateTime,
            todayDateTime.plusMinutes(HOUR.toLong()),
            projectRoleLimited.id,
            projectRoleLimited.timeUnit
        )
        private val activityNotReachedLimitTimeOnly = ActivityTimeOnly(
            todayDateTime,
            todayDateTime.plusMinutes(HOUR * 5L),
            projectRoleLimited.id,
            projectRoleLimited.timeUnit
        )

        private val newActivityInMarch = ActivityRequestBody(
            null,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
            "description",
            false,
            projectRole.id,
            projectRole.timeUnit,
            false,
        )

        private val newActivityInClosedProject = ActivityRequestBody(
            null,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
            "description",
            false,
            closedProjectRole.id,
            closedProjectRole.timeUnit,
            false,
        )
        private val newActivityLastYear = ActivityRequestBody(
            null,
            someYearsAgoLocalDateTime(1),
            someYearsAgoLocalDateTime(1).plusMinutes(HOUR.toLong()).plusMinutes(HOUR.toLong()),
            "description",
            false,
            projectRole.id,
            projectRole.timeUnit,
            false,
        )
        private val newActivityTwoYearsAgo = ActivityRequestBody(
            null,
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
            "description",
            false,
            projectRole.id,
            projectRole.timeUnit,
            false,
        )

        private val activityLastYear = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(1),
            someYearsAgoLocalDateTime(1).plusMinutes(HOUR.toLong()),
            "Updated activity",
            false,
            projectRole.id,
            projectRole.timeUnit,
            false,
        )
        private val activityUpdateNonexistentID = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
            "Updated activity",
            false,
            projectRole.id,
            projectRole.timeUnit,
            false,
        )

        private val currentActivity = Activity(
            1L,
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
            "Old description",
            projectRole,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )
        val newActivityRequest = ActivityRequestBody(
            1L,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
            "description",
            false,
            projectRole.id,
            projectRole.timeUnit,
            false,
        )
        val activityUpdateTwoYearsAgo = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
            "Updated activity",
            false,
            projectRole.id,
            projectRole.timeUnit,
            false,
        )
        private const val anyOtherUserId = 33L

        private val currentActivityAnotherUser = Activity(
            1L,
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
            "Old description",
            projectRole,
            anyOtherUserId,
            false,
            approvalState = ApprovalState.NA
        )
        private val validActivityToUpdate = ActivityRequestBody(
            1L,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
            "description",
            false,
            projectRole.id,
            projectRole.timeUnit,
            false,
        )

        private val newActivityBeforeHiringDate = ActivityRequestBody(
            null,
            LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month.minus(1), 3, 11, 45),
            LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month.minus(1), 3, 11, 45)
                .plusMinutes(HOUR.toLong()),
            "description",
            false,
            projectRole.id,
            projectRole.timeUnit,
            false,
        )

        private fun createActivityRequestBody(
            start: LocalDateTime,
            end: LocalDateTime,
            description: String = "",
            projectRoleId: Long = projectRoleLimited.id,
            timeUnit: TimeUnit = projectRoleLimited.timeUnit,
            billable: Boolean = false,
            hasEvidences: Boolean = false,
        ): ActivityRequestBody =
            ActivityRequestBody(
                start = start,
                end = end,
                description = description,
                projectRoleId = projectRoleId,
                timeUnit = timeUnit,
                billable = billable,
                hasEvidences = hasEvidences,
            )

        private fun createActivityRequestBodyToUpdate(
            id: Long,
            start: LocalDateTime,
            end: LocalDateTime,
            description: String = "",
            projectRoleId: Long = projectRoleLimited.id,
            timeUnit: TimeUnit = projectRoleLimited.timeUnit,
            billable: Boolean = false,
            hasEvidences: Boolean = false,
        ): ActivityRequestBody =
            ActivityRequestBody(
                id = id,
                start = start,
                end = end,
                description = description,
                projectRoleId = projectRoleId,
                timeUnit = timeUnit,
                billable = billable,
                hasEvidences = hasEvidences
            )

        private fun createActivity(
            id: Long? = null,
            start: LocalDateTime,
            end: LocalDateTime,
            description: String = "",
            billable: Boolean = false,
            projectRole: ProjectRole,
            userId: Long = user.id,
            approvalState: ApprovalState = ApprovalState.NA
        ) = Activity(
            id = id,
            start = start,
            end = end,
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
            requireEvidence: RequireEvidence = RequireEvidence.NO,
            project: Project = Project(1, "Project", true, false, organization, listOf()),
            maxAllowed: Int
        ) = ProjectRole(
            id,
            name,
            requireEvidence,
            project,
            maxAllowed,
            true,
            false,
            TimeUnit.MINUTES
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
