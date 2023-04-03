package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.User
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

    private val activityValidator = ActivityValidator(
        activityRepository,
        projectRoleRepository
    )

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
                false
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
                    )
                )
            ).whenever(activityRepository).find(
                LocalDateTime.of(2022, Month.JULY, 7, 0, 0, 0),
                LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59)
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
                createActivityRequestBody(startDate = todayDateTime.minusYears(1L), duration = (HOUR * 9)),
                0.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "reached limit no remaining hours",
                listOf(activityReachedLimitTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(startDate = todayDateTime, duration = (HOUR * 9)),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "reached limit no remaining hours current day",
                listOf(activityReachedLimitTodayTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(startDate = todayDateTime, duration = (HOUR * 9)),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "reached limit no remaining hours half hour",
                listOf(activityReachedHalfHourTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = 90),
                createActivityRequestBody(startDate = todayDateTime, duration = (HOUR * 9)),
                0.5,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "not reached limit remaining hours left",
                listOf(activityNotReachedLimitTimeOnly, activityNoLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(startDate = todayDateTime, duration = (HOUR * 10)),
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
                .whenever(activityRepository).findWorkedMinutes(firstDay, lastDay)

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
            whenever(activityRepository.findById(1L)).thenReturn(currentActivity)
            whenever(projectRoleRepository.findById(any())).thenReturn(Optional.of(projectRole))

            activityValidator.checkActivityIsValidForUpdate(validActivityToUpdate, user)
        }

        @Test
        fun `throw ActivityNotFoundException with activity id when the activity to be replaced does not exist`() {
            whenever(activityRepository.findById(1L)).thenReturn(null)
            whenever(projectRoleRepository.findById(any())).thenReturn(Optional.of(projectRole))

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
                false
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
                23,
                "Old description",
                projectRole,
                user.id,
                false
            )

            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(Optional.empty())
            whenever(activityRepository.findById(1L)).thenReturn(currentActivity)

            val exception = assertThrows<ProjectRoleNotFoundException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, user)
            }
            assertEquals(projectRole.id, exception.id)
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
                false
            )

            whenever(activityRepository.findById(1L)).thenReturn(currentActivity)
            whenever(projectRoleRepository.findById(any())).thenReturn(Optional.of(closedProjectRole))

            assertThrows<ProjectClosedException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, user)
            }
        }

        @Test
        fun `do nothing when updated activity started last year`() {
            whenever(activityRepository.findById(1L)).thenReturn(currentActivity)
            whenever(projectRoleRepository.findById(any())).thenReturn(Optional.of(projectRole))

            activityValidator.checkActivityIsValidForUpdate(activityLastYear, user)
        }

        @Test
        fun `throw ActivityPeriodClosedException when updated activity started more than one year ago`() {
            whenever(activityRepository.findById(1L)).thenReturn(currentActivity)
            whenever(projectRoleRepository.findById(any())).thenReturn(Optional.of(projectRole))

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
                false
            )
            given(activityRepository.findById(1L)).willReturn(currentActivity)

            given(
                activityRepository.find(
                    LocalDateTime.of(2022, Month.JULY, 7, 0, 0, 0),
                    LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),
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
                    )
                )
            )
            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(Optional.of(projectRole))

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
                        startDate = todayDateTime.minusYears(1L),
                        duration = HOUR * 9
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
                        startDate = todayDateTime.minusYears(1L),
                        duration = HOUR * 10
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
                        startDate = todayDateTime,
                        duration = HOUR * 9
                    ),
                    0.0,
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

            whenever(activityRepository.findWorkedMinutes(firstDay, lastDay)).thenReturn(activitiesInYear)
            whenever(activityRepository.findById(currentActivity.id!!)).thenReturn(currentActivity)
            whenever(projectRoleRepository.findById(projectRoleLimited.id)).thenReturn(Optional.of(projectRoleLimited))

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
                false
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                23,
                "Old description",
                projectRole,
                user.id,
                false
            )
            given(activityRepository.findById(1L)).willReturn(currentActivity)

            given(
                activityRepository.find(
                    LocalDateTime.of(2022, Month.JULY, 7, 0, 0, 0),
                    LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),
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
                        hasImage = false
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
                false
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month, 3, 2, 1),
                23,
                "Old description",
                projectRole,
                userHiredLastYear.id,
                false
            )

            whenever(activityRepository.findById(1L)).thenReturn(currentActivity)
            whenever(projectRoleRepository.findById(any())).thenReturn(Optional.of(projectRole))

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
                false
            )

            whenever(activityRepository.findById(id)).thenReturn(activity)

            activityValidator.checkActivityIsValidForDeletion(id, user)
        }

        @Test
        fun `throw ActivityNotFoundException with id when activity is not in the database`() {
            val id = 1L

            given(activityRepository.findById(id)).willReturn(null)

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
                false
            )

            whenever(activityRepository.findById(id)).thenReturn(activity)

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
                false
            )
            whenever(activityRepository.findById(id)).thenReturn(activity)

            assertThrows<ActivityPeriodClosedException> {
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

        private val projectRoleWithoutLimit = ProjectRole(2, "perm", false, permisoProject, 0)
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

        private val activityAYearAgoUpdated = createActivity(
            id = 1L,
            startDate = LocalDateTime.of(LocalDate.now().minusYears(1L), LocalTime.now()),
            duration = projectRoleLimited.maxAllowed - 120,
            projectRole = projectRoleLimited
        )

        private val activityReachedLimitTimeOnly = ActivityTimeOnly(
            yesterdayDateTime,
            projectRoleLimited.maxAllowed,
            projectRoleLimited.id
        )

        private val activityReachedLimitTimeOnlyAYearAgo = ActivityTimeOnly(
            yesterdayDateTime.minusYears(1L),
            projectRoleLimited.maxAllowed,
            projectRoleLimited.id
        )

        private val activityForLimitedProjectRoleAYearAgo = ActivityTimeOnly(
            yesterdayDateTime.minusYears(1L),
            projectRoleLimited.maxAllowed - 120,
            projectRoleLimited.id
        )

        private val otherActivityForLimitedProjectRoleAYearAgo = ActivityTimeOnly(
            yesterdayDateTime.minusYears(1L),
            120,
            projectRoleLimited.id
        )

        private val activityNoLimitTimeOnly = ActivityTimeOnly(
            yesterdayDateTime,
            (HOUR * 8),
            projectRoleWithoutLimit.id
        )

        private val activityNoLimitTimeOnlyAYearAgo = ActivityTimeOnly(
            yesterdayDateTime.minusYears(1L),
            (HOUR * 8),
            projectRoleWithoutLimit.id
        )

        private val activityReachedLimitTodayTimeOnly = ActivityTimeOnly(
            todayDateTime,
            projectRoleLimited.maxAllowed,
            projectRoleLimited.id
        )

        private val activityReachedHalfHourTimeOnly = ActivityTimeOnly(
            todayDateTime,
            HOUR,
            projectRoleLimited.id
        )

        private val activityNotReachedLimitTimeOnly = ActivityTimeOnly(
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
            false
        )
        private const val CLOSED_ID = 2L

        private val newActivityInClosedProject = ActivityRequestBody(
            null,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            HOUR,
            "description",
            false,
            CLOSED_ID,
            false
        )
        private val newActivityLastYear = ActivityRequestBody(
            null,
            someYearsAgoLocalDateTime(1),
            HOUR,
            "description",
            false,
            user.id,
            false
        )
        private val newActivityTwoYearsAgo = ActivityRequestBody(
            null,
            someYearsAgoLocalDateTime(2),
            HOUR,
            "description",
            false,
            user.id,
            false
        )

        private val activityLastYear = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(1),
            HOUR,
            "Updated activity",
            false,
            user.id,
            false
        )
        private val activityUpdateNonexistentID = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(2),
            HOUR,
            "Updated activity",
            false,
            user.id,
            false
        )

        private val currentActivity = Activity(
            1L,
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
            23,
            "Old description",
            projectRole,
            user.id,
            false
        )
        val newActivityRequest = ActivityRequestBody(
            1L,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            HOUR,
            "description",
            false,
            user.id,
            false
        )
        val activityUpdateTwoYearsAgo = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(2),
            HOUR,
            "Updated activity",
            false,
            user.id,
            false
        )
        private const val anyOtherUserId = 33L

        private val currentActivityAnotherUser = Activity(
            1L,
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
            23,
            "Old description",
            projectRole,
            anyOtherUserId,
            false
        )
        private val validActivityToUpdate = ActivityRequestBody(
            1L,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            HOUR,
            "description",
            false,
            user.id,
            false
        )

        private val newActivityBeforeHiringDate = ActivityRequestBody(
            null,
            LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month.minus(1), 3, 11, 45),
            HOUR,
            "description",
            false,
            userHiredLastYear.id,
            false
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
        ): ActivityRequestBody =
            ActivityRequestBody(
                startDate = startDate,
                duration = duration,
                description = description,
                projectRoleId = projectRoleId,
                billable = billable,
                hasImage = hasImage,
            )

        private fun createActivityRequestBodyToUpdate(
            id: Long,
            startDate: LocalDateTime,
            duration: Int,
            description: String = "",
            projectRoleId: Long = projectRoleLimited.id,
            billable: Boolean = false,
            hasImage: Boolean = false,
        ): ActivityRequestBody =
            ActivityRequestBody(
                id = id,
                startDate = startDate,
                duration = duration,
                description = description,
                projectRoleId = projectRoleId,
                billable = billable,
                hasImage = hasImage,
            )

        private fun createActivity(
            id: Long? = null,
            startDate: LocalDateTime,
            duration: Int,
            description: String = "",
            billable: Boolean = false,
            projectRole: ProjectRole,
            userId: Long = user.id
        ) = Activity(
            id = id,
            startDate = startDate,
            duration = duration,
            description = description,
            projectRole = projectRole,
            userId = userId,
            billable = billable,

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
