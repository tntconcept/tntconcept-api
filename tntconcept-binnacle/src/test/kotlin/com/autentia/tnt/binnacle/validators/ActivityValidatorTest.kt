package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.services.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.time.*
import java.util.*

@TestInstance(PER_CLASS)
internal class ActivityValidatorTest {
    private val holidayService = mock<HolidayService>()
    private val activityService = mock<ActivityService>()
    private val projectRoleService = mock<ProjectRoleService>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val projectService = mock<ProjectService>()
    private val activityValidator =
        ActivityValidator(
            activityService,
            activityCalendarService,
            projectRoleService,
            projectService
        )
    private val calendarFactory: CalendarFactory = CalendarFactory(holidayService)

    @AfterEach
    fun resetMocks() {
        reset(
            projectRoleService,
            holidayService,
            activityService,
            activityCalendarService,
            projectService
        )
    }

    @TestInstance(PER_CLASS)
    @Nested
    inner class CheckActivityIsValidForCreation {
        @Test
        fun `do nothing when activity is valid`() {
            whenever(projectRoleService.getByProjectRoleId(projectRole.id)).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(projectRole.project.id)).thenReturn(vacationProject.toDomain())

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
                "ProjectNotFoundException",
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
                "ActivityForBlockedProjectException",
                newActivityBeforeBlockedProject,
                blockedProjectRole,
                user,
                ActivityForBlockedProjectException()
            ),
            arrayOf(
                "ActivityBeforeHiringDateException",
                newActivityBeforeHiringDate,
                projectRole,
                userHiredLastYear,
                ActivityBeforeHiringDateException()
            ),
            arrayOf(
                "ActivityInvalidPeriodException",
                newActivityInvalidPeriodForMinutesProjectRole,
                projectRole,
                user,
                ActivityPeriodNotValidException()
            ),
        )

        @ParameterizedTest
        @MethodSource("exceptionProvider")
        fun `throw exceptions`(
            testDescription: String,
            activityToValidate: com.autentia.tnt.binnacle.core.domain.Activity,
            projectRole: ProjectRole,
            user: com.autentia.tnt.binnacle.core.domain.User,
            expectedException: BinnacleException,
        ) {
            whenever(projectService.findById(projectRole.project.id)).thenReturn(projectRole.project.toDomain())

            whenever(projectRoleService.getByProjectRoleId(projectRole.id)).thenReturn(projectRole.toDomain())

            val exception = assertThrows<BinnacleException> {
                activityValidator.checkActivityIsValidForCreation(activityToValidate, user)
            }

            assertEquals(expectedException.message, exception.message)
        }

        @Test
        fun `throw ProjectRoleNotFoundException with role id when project role is not in the database`() {
            whenever(projectRoleService.getByProjectRoleId(projectRole.id)).thenThrow(
                ProjectRoleNotFoundException(
                    projectRole.id
                )
            )

            val exception = assertThrows<ProjectRoleNotFoundException> {
                activityValidator.checkActivityIsValidForCreation(newActivityInMarch, user)
            }
            assertEquals(projectRole.id, exception.id)
        }

        @Test
        fun `do nothing when activity started last year`() {
            whenever(projectRoleService.getByProjectRoleId(projectRole.id)).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(projectRole.project.id)).thenReturn(vacationProject.toDomain())

            activityValidator.checkActivityIsValidForCreation(newActivityLastYear, user)
        }

        @Test
        fun `do nothing when activity started after block project`() {
            whenever(projectRoleService.getByProjectRoleId(blockedProjectRole.id)).thenReturn(blockedProjectRole.toDomain())
            whenever(projectService.findById(blockedProjectRole.project.id)).thenReturn(blockedProject.toDomain())

            activityValidator.checkActivityIsValidForCreation(newActivityAfterBlockedProject, user)
        }

        @Test
        fun `throw ActivityForBlockedProjectException when activity started the same day as a project is blocked`() {
            whenever(projectRoleService.getByProjectRoleId(blockedProjectRole.id)).thenReturn(blockedProjectRole.toDomain())
            whenever(projectService.findById(blockedProjectRole.project.id)).thenReturn(blockedProject.toDomain())

            assertThrows<ActivityForBlockedProjectException> {
                activityValidator.checkActivityIsValidForCreation(newActivitySameDayBlockedProject, user)
            }
        }

        @Test
        fun `throw OverlapsAnotherTimeException when there is already an activity of that user at the same time`() {
            val newActivity = com.autentia.tnt.binnacle.core.domain.Activity.of(
                null,
                TimeInterval.of(
                    LocalDateTime.of(2022, Month.JULY, 7, 8, 45),
                    LocalDateTime.of(2022, Month.JULY, 7, 10, 0)
                ),
                75,
                "New activity",
                projectRole.toDomain(),
                1L,
                false,
                null,
                LocalDateTime.now(),
                false,
                ApprovalState.NA
            )

            whenever(
                activityService.findOverlappedActivities(
                    newActivity.getStart(), newActivity.getEnd(), user.id
                )
            ).thenReturn(
                listOf(
                    Activity(
                        1,
                        LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                        LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),
                        120,
                        "Other activity",
                        projectRole,
                        user.id,
                        false,
                        approvalState = ApprovalState.NA
                    ).toDomain()
                )
            )
            whenever(projectRoleService.getByProjectRoleId(projectRole.id)).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(projectRole.project.id)).thenReturn(vacationProject.toDomain())

            assertThrows<OverlapsAnotherTimeException> {
                activityValidator.checkActivityIsValidForCreation(newActivity, user)
            }
        }

        private fun maxHoursRoleLimitProviderCreate() = arrayOf(
            arrayOf(
                "reached limit no remaining hours the year before",
                listOf(activityReachedLimitTimeOnlyAYearAgo),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivity(
                    start = todayDateTime.minusYears(1L),
                    end = todayDateTime.minusYears(1L).plusMinutes(HOUR * 9L), duration = (HOUR * 9)
                ).copy(id = null),
                0.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "reached limit no remaining hours",
                listOf(activityReachedLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 9L),
                    duration = (HOUR * 9)
                ).copy(id = null),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "reached limit no remaining hours current day",
                listOf(activityReachedLimitTodayTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 9L),
                    duration = (HOUR * 9)
                ).copy(id = null),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "reached limit no remaining hours half hour",
                listOf(activityReachedHalfHourTimeOnly),
                createProjectRoleWithLimit(maxAllowed = 90),
                createActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 9L),
                    duration = (HOUR * 9),
                    projectRole = createProjectRoleWithLimit(maxAllowed = 90)
                ).copy(id = null),
                0.5,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "not reached limit remaining hours left",
                listOf(activityNotReachedLimitTimeOnly),
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 10L),
                    duration = (HOUR * 10)
                ).copy(id = null),
                3.0,
                firstDayOfYear,
                lastDayOfYear
            )
        )

        @ParameterizedTest
        @MethodSource("maxHoursRoleLimitProviderCreate")
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`(
            testDescription: String,
            activitiesInTheYear: List<com.autentia.tnt.binnacle.core.domain.Activity>,
            projectRoleLimited: ProjectRole,
            activity: com.autentia.tnt.binnacle.core.domain.Activity,
            expectedRemainingHours: Double,
            firstDay: LocalDateTime,
            lastDay: LocalDateTime,
        ) {
            val timeInterval = TimeInterval.of(firstDay, lastDay)
            val calendar = calendarFactory.create(timeInterval.getDateInterval())

            whenever(projectRoleService.getByProjectRoleId(projectRoleLimited.id)).thenReturn(projectRoleLimited.toDomain())
            whenever(projectService.findById(projectRole.project.id)).thenReturn(vacationProject.toDomain())
            whenever(activityCalendarService.createCalendar(timeInterval.getDateInterval())).thenReturn(calendar)
            whenever(
                activityService.getActivitiesByProjectRoleIds(
                    timeInterval,
                    listOf(activity.projectRole.id),
                    user.id
                )
            ).thenReturn(activitiesInTheYear)

            val exception = assertThrows<MaxHoursPerRoleException> {
                activityValidator.checkActivityIsValidForCreation(activity, user)
            }

            assertEquals(projectRoleLimited.maxAllowed / DECIMAL_HOUR, exception.maxAllowedHours)
            assertEquals(expectedRemainingHours, exception.remainingHours)
        }

        @Test
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role 2`() {
            val maxAllowed = 480

            val projectRole = createProjectRoleWithLimit(1L, maxAllowed = maxAllowed)

            val activity = createDomainActivity(
                start = LocalDateTime.of(2023, 1, 20, 0, 0, 0),
                end = LocalDateTime.of(2023, 1, 20, 0, 0, 0).plusMinutes(480),
                duration = 480,
                projectRole.toDomain()
            ).copy(id = null)

            val timeInterval2022 = TimeInterval.ofYear(2022)
            val timeInterval2023 = TimeInterval.ofYear(2023)

            val calendar2022 = calendarFactory.create(timeInterval2022.getDateInterval())
            val calendar2023 = calendarFactory.create(timeInterval2023.getDateInterval())
            val activities2022 = listOf(
                createDomainActivity(
                    start = LocalDateTime.of(2022, 12, 31, 0, 0, 0),
                    end = LocalDateTime.of(2022, 12, 31, 23, 59, 59),
                    projectRole = projectRole.toDomain().copy(timeUnit = TimeUnit.DAYS)
                )
            )
            val activities2023 = listOf(
                createDomainActivity(
                    start = LocalDateTime.of(2023, 1, 1, 8, 0, 0),
                    end = LocalDateTime.of(2023, 1, 1, 16, 0, 0),
                    projectRole = projectRole.toDomain().copy(timeUnit = TimeUnit.MINUTES)
                )
            )

            whenever(projectRoleService.getByProjectRoleId(projectRole.id)).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(Companion.projectRole.project.id)).thenReturn(vacationProject.toDomain())
            whenever(
                activityService.getActivitiesByProjectRoleIds(
                    timeInterval2022,
                    listOf(projectRole.id),
                    user.id
                )
            ).thenReturn(activities2022)
            whenever(
                activityService.getActivitiesByProjectRoleIds(
                    timeInterval2023,
                    listOf(projectRole.id),
                    user.id
                )
            ).thenReturn(activities2023)

            whenever(activityCalendarService.createCalendar(timeInterval2022.getDateInterval())).thenReturn(calendar2022)
            whenever(activityCalendarService.createCalendar(timeInterval2023.getDateInterval())).thenReturn(calendar2023)

            val exception = assertThrows<MaxHoursPerRoleException> {
                activityValidator.checkActivityIsValidForCreation(activity, user)
            }

            assertEquals(projectRole.maxAllowed / DECIMAL_HOUR, exception.maxAllowedHours)
            assertEquals(0.0, exception.remainingHours)
            assertEquals(2023, exception.year)
        }
    }

    @TestInstance(PER_CLASS)
    @Nested
    inner class CheckActivityIsValidForUpdate {
        @Test
        fun `do nothing when activity is valid`() {
            whenever(activityService.getActivityById(1L)).thenReturn(currentActivity.toDomain())
            whenever(projectRoleService.getByProjectRoleId(1L)).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(1L)).thenReturn(nonBlockedProject.toDomain())

            activityValidator.checkActivityIsValidForUpdate(validActivityToUpdate, validActivityToUpdate, user)
        }

        @Test
        fun `throw ActivityPeriodInvalidException when TimeInterval is longer than a day for a Minutes TimeUnit project role`() {

            whenever(activityService.getActivityById(1L)).thenReturn(currentActivity.toDomain())
            whenever(projectRoleService.getByProjectRoleId(any())).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(1L)).thenReturn(nonBlockedProject.toDomain())

            assertThrows<ActivityPeriodNotValidException> {
                activityValidator.checkActivityIsValidForUpdate(
                    activityInvalidPeriodForMinutesProjectRole,
                    activityInvalidPeriodForMinutesProjectRole,
                    user
                )
            }
        }

        @Test
        fun `throw ProjectBlockedException when currentProject is blocked`() {
            val newActivity = createDomainActivity(
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                60,
                projectRoleWithNonBlockedProject.toDomain()
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 24),
                23,
                "Old description",
                projectRoleWithBlockedProject,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )
            whenever(activityService.getActivityById(1L)).thenReturn(Companion.currentActivity.toDomain())
            whenever(projectRoleService.getByProjectRoleId(any())).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(nonBlockedProject.id)).thenReturn(nonBlockedProject.toDomain())
            whenever(projectService.findById(blockedProject.id)).thenReturn(blockedProject.toDomain())

            assertThrows<ProjectBlockedException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain(), user)
            }
        }

        @Test
        fun `do nothing when blocked date doesnt block current change`() {
            val newActivity = createDomainActivity(
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                60,
                projectRoleWithPastBlockedProject.toDomain()
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 24),
                23,
                "Old description",
                projectRoleWithPastBlockedProject,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )
            whenever(activityService.getActivityById(1L)).thenReturn(Companion.currentActivity.toDomain())
            whenever(projectRoleService.getByProjectRoleId(any())).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(blockedPastProject.id)).thenReturn(blockedPastProject.toDomain())

            activityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain(), user)
        }

        @Test
        fun `throw ProjectBlockedException when attempting to change activity to a blocked project`() {
            val newActivity = createDomainActivity(
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                60,
                projectRoleWithBlockedProject.toDomain()
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 24),
                23,
                "Old description",
                projectRoleWithNonBlockedProject,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )
            whenever(activityService.getActivityById(1L)).thenReturn(Companion.currentActivity.toDomain())
            whenever(projectRoleService.getByProjectRoleId(any())).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(nonBlockedProject.id)).thenReturn(nonBlockedProject.toDomain())
            whenever(projectService.findById(blockedProject.id)).thenReturn(blockedProject.toDomain())

            assertThrows<ProjectBlockedException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain(), user)
            }
        }

        @Test
        fun `throw ProjectRoleNotFoundException with role id when project role is not in the database`() {
            val newActivity = createDomainActivity(
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                60,
                projectRole.toDomain()
            )

            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
                LocalDateTime.of(2020, Month.JANUARY, 3, 2, 24),
                23,
                "Old description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            whenever(projectService.findById(1L)).thenReturn(nonBlockedProject.toDomain())
            whenever(projectRoleService.getByProjectRoleId(projectRole.id)).thenThrow(
                ProjectRoleNotFoundException(
                    projectRole.id
                )
            )
            whenever(activityService.getActivityById(1L)).thenReturn(Companion.currentActivity.toDomain())

            val exception = assertThrows<ProjectRoleNotFoundException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, user)
            }
            assertEquals(projectRole.id, exception.id)
        }

        @Test
        fun `throw ProjectClosedException when chosen project is already closed`() {
            val newActivity = createDomainActivity(
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                60,
                closedProjectRole.toDomain()
            )
            whenever(projectService.findById(closedProject.id)).thenReturn(closedProject.toDomain())
            whenever(activityService.getActivityById(1L)).thenReturn(currentActivity.toDomain())
            whenever(projectRoleService.getByProjectRoleId(closedProjectRole.id)).thenReturn(closedProjectRole.toDomain())

            assertThrows<ProjectClosedException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, user)
            }
        }

        @Test
        fun `do nothing when updated activity started last year`() {
            whenever(activityService.getActivityById(1L)).thenReturn(currentActivity.toDomain())
            whenever(projectRoleService.getByProjectRoleId(1L)).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(1L)).thenReturn(nonBlockedProject.toDomain())

            activityValidator.checkActivityIsValidForUpdate(activityLastYear, activityLastYear, user)
        }

        @Test
        fun `throw ActivityPeriodClosedException when updated activity started more than one year ago`() {
            whenever(activityService.getActivityById(1L)).thenReturn(currentActivity.toDomain())
            whenever(projectRoleService.getByProjectRoleId(1L)).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(1L)).thenReturn(nonBlockedProject.toDomain())

            assertThrows<ActivityPeriodClosedException> {
                activityValidator.checkActivityIsValidForUpdate(
                    activityUpdateTwoYearsAgo,
                    activityUpdateTwoYearsAgo,
                    user
                )
            }
        }

        @Test
        fun `throw OverlapsAnotherTimeException when there is already an activity of that user at the same time`() {
            val newActivity = createDomainActivity(
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0),
                LocalDateTime.of(2022, Month.JULY, 7, 10, 0, 0),
                75,
                projectRole.toDomain()
            )
            whenever(activityService.getActivityById(1L)).thenReturn(currentActivity.toDomain())


            whenever(
                activityService.findOverlappedActivities(
                    newActivity.getStart(),
                    newActivity.getEnd(),
                    user.id
                )
            ).thenReturn(
                listOf(
                    Activity(
                        33,
                        LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                        LocalDateTime.of(2022, Month.JULY, 7, 11, 30, 0),
                        120,
                        "Other activity",
                        projectRole,
                        user.id,
                        billable = false,
                        approvalState = ApprovalState.NA,
                        hasEvidences = false
                    ).toDomain()
                )
            )
            whenever(projectRoleService.getByProjectRoleId(projectRole.id)).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(1L)).thenReturn(nonBlockedProject.toDomain())

            assertThrows<OverlapsAnotherTimeException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, user)
            }
        }

        private fun maxHoursRoleLimitProviderUpdate() = arrayOf(
            arrayOf(
                "reached limit no remaining hours for activity related to the year before",
                listOf(activityForLimitedProjectRoleAYearAgo, otherActivityForLimitedProjectRoleAYearAgo),
                activityAYearAgoUpdated,
                createDomainActivity(
                    start = todayDateTime.minusYears(1L),
                    end = todayDateTime.minusYears(1L).plusMinutes((HOUR * 9).toLong()),
                    duration = HOUR * 9,
                    projectRole = projectRoleLimited.toDomain()
                ).copy(id = activityAYearAgoUpdated.id),
                0.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "reached limit remaining hours left related to the year before",
                listOf(activityForLimitedProjectRoleAYearAgo),
                activityAYearAgoUpdated,
                createDomainActivity(
                    start = todayDateTime.minusYears(1L),
                    end = todayDateTime.minusYears(1L).plusMinutes((HOUR * 10).toLong()),
                    duration = HOUR * 10,
                    projectRole = projectRoleLimited.toDomain()
                ).copy(id = activityNotReachedLimitUpdate.id),
                2.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "reached limit no remaining hours",
                listOf(activityReachedLimitTimeOnly),
                activityReachedLimitUpdate,
                createDomainActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 9L),
                    duration = HOUR * 9,
                    projectRole = projectRoleLimited.toDomain()
                ).copy(id = activityReachedLimitUpdate.id),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "not reached limit remaining hours left",
                listOf(activityNotReachedLimitTimeOnly),
                activityNotReachedLimitUpdate,
                createDomainActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 10L),
                    duration = HOUR * 10,
                    projectRole = projectRoleLimited.toDomain()
                ).copy(id = activityNotReachedLimitUpdate.id),
                3.0,
                firstDayOfYear,
                lastDayOfYear,
            ),
        )

        @ParameterizedTest
        @MethodSource("maxHoursRoleLimitProviderUpdate")
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`(
            testDescription: String,
            activitiesInTheYear: List<com.autentia.tnt.binnacle.core.domain.Activity>,
            currentActivity: com.autentia.tnt.binnacle.core.domain.Activity,
            activityToUpdate: com.autentia.tnt.binnacle.core.domain.Activity,
            expectedRemainingHours: Double,
            firstDay: LocalDateTime,
            lastDay: LocalDateTime,
        ) {

            val timeInterval = TimeInterval.of(firstDay, lastDay)
            val yearTimeInterval = TimeInterval.ofYear(firstDay.year)
            val calendar = calendarFactory.create(timeInterval.getDateInterval())

            whenever(activityCalendarService.createCalendar(timeInterval.getDateInterval())).thenReturn(calendar)
            whenever(activityService.getActivityById(currentActivity.id!!)).thenReturn(
                Activity.of(
                    currentActivity,
                    projectRoleLimited
                ).toDomain()
            )
            whenever(
                activityService.getActivitiesByProjectRoleIds(yearTimeInterval, listOf(projectRoleLimited.id), user.id)
            ).thenReturn(activitiesInTheYear)
            whenever(projectRoleService.getByProjectRoleId(projectRoleLimited.id)).thenReturn(projectRoleLimited.toDomain())
            whenever(projectService.findById(1L)).thenReturn(nonBlockedProject.toDomain())

            val exception = assertThrows<MaxHoursPerRoleException> {
                activityValidator.checkActivityIsValidForUpdate(activityToUpdate, currentActivity, user)
            }

            assertEquals(projectRoleLimited.maxAllowed / DECIMAL_HOUR, exception.maxAllowedHours)
            assertEquals(expectedRemainingHours, exception.remainingHours)
        }

        @Test
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`() {
            val maxAllowed = 480

            val projectRole = createProjectRoleWithLimit(1L, maxAllowed = maxAllowed)

            val activityToValidate = createDomainActivity(
                start = LocalDateTime.of(2023, 1, 20, 0, 0, 0),
                end = LocalDateTime.of(2023, 1, 20, 0, 0, 0).plusMinutes(480),
                duration = 480,
                projectRole = projectRole.toDomain()
            )

            val currentActivity =
                createActivity(
                    id = 1L,
                    start = activityToValidate.getStart(),
                    end = activityToValidate.getEnd(),
                    duration = 480,
                    projectRole = projectRoleLimited
                )

            val activity = Activity.of(currentActivity, projectRoleLimited)

            val timeInterval2022 = TimeInterval.ofYear(2022)
            val timeInterval2023 = TimeInterval.ofYear(2023)

            val calendar2022 = calendarFactory.create(timeInterval2022.getDateInterval())
            val calendar2023 = calendarFactory.create(timeInterval2023.getDateInterval())
            val activities2022 = listOf(
                createDomainActivity(
                    start = LocalDateTime.of(2022, 12, 31, 0, 0, 0),
                    end = LocalDateTime.of(2022, 12, 31, 23, 59, 59),
                    projectRole = projectRole.toDomain().copy(timeUnit = TimeUnit.DAYS)
                )
            )
            val activities2023 = listOf(
                createDomainActivity(
                    start = LocalDateTime.of(2023, 1, 1, 8, 0, 0),
                    end = LocalDateTime.of(2023, 1, 1, 16, 0, 0),
                    projectRole = projectRole.toDomain()
                )
            )

            whenever(projectService.findById(1L)).thenReturn(nonBlockedProject.toDomain())
            whenever(projectRoleService.getByProjectRoleId(projectRole.id)).thenReturn(projectRole.toDomain())
            whenever(activityService.getActivityById(activity.id!!)).thenReturn(activity.toDomain())
            whenever(
                activityService.getActivitiesByProjectRoleIds(
                    timeInterval2022,
                    listOf(projectRole.id),
                    user.id
                )
            ).thenReturn(
                activities2022
            )
            whenever(
                activityService.getActivitiesByProjectRoleIds(
                    timeInterval2023,
                    listOf(projectRole.id),
                    user.id
                )
            ).thenReturn(
                activities2023
            )

            whenever(activityCalendarService.createCalendar(timeInterval2022.getDateInterval())).thenReturn(calendar2022)
            whenever(activityCalendarService.createCalendar(timeInterval2023.getDateInterval())).thenReturn(calendar2023)

            val exception = assertThrows<MaxHoursPerRoleException> {
                activityValidator.checkActivityIsValidForUpdate(activityToValidate, currentActivity, user)
            }

            assertEquals(projectRole.maxAllowed / DECIMAL_HOUR, exception.maxAllowedHours)
            assertEquals(0.0, exception.remainingHours)
            assertEquals(2023, exception.year)
        }

        @Test
        fun `not fail when the activity whose time is overlapped is the activity to be replaced`() {
            val newActivity = createDomainActivity(
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0),
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0).plusMinutes(75),
                75,
                projectRole.toDomain()
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                LocalDateTime.of(2022, Month.JULY, 7, 9, 53, 0),
                23,
                "Old description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )
            whenever(activityService.getActivityById(1L)).thenReturn(currentActivity.toDomain())

            whenever(
                activityService.findOverlappedActivities(
                    LocalDateTime.of(2022, Month.JULY, 7, 0, 0, 0),
                    LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),
                    user.id
                )
            ).thenReturn(
                listOf(
                    Activity(
                        1L,
                        LocalDateTime.of(2022, Month.JULY, 7, 9, 30, 0),
                        LocalDateTime.of(2022, Month.JULY, 7, 9, 53, 0),
                        23,
                        "Other activity",
                        projectRole,
                        user.id,
                        billable = false,
                        hasEvidences = false,
                        approvalState = ApprovalState.NA

                    ).toDomain()
                )
            )


            whenever(projectRoleService.getByProjectRoleId(projectRole.id)).thenReturn(projectRole.toDomain())
            whenever(projectService.findById(1L)).thenReturn(nonBlockedProject.toDomain())

            activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, user)
        }

        @Test
        fun `throw ActivityBeforeHiringDateException when updated activity starting date is before that user hiring date`() {

            val newActivity = createDomainActivity(
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
                HOUR,
                projectRole.toDomain()
            )
            val currentActivity = Activity(
                1L,
                LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month, 3, 2, 1),
                LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month, 3, 2, 1)
                    .plusMinutes(23),
                23,
                "Old description",
                projectRole,
                userHiredLastYear.id,
                false,
                approvalState = ApprovalState.NA
            )

            whenever(activityService.getActivityById(1L)).thenReturn(currentActivity.toDomain())
            whenever(projectService.findById(1L)).thenReturn(nonBlockedProject.toDomain())
            whenever(projectRoleService.getByProjectRoleId(any())).thenReturn(projectRole.toDomain())

            assertThrows<ActivityBeforeHiringDateException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, userHiredLastYear)
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
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
                HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            whenever(activityService.getActivityById(id)).thenReturn(activity.toDomain())
            whenever(projectService.findById(vacationProject.id)).thenReturn(vacationProject.toDomain())

            activityValidator.checkActivityIsValidForDeletion(id)
        }

        @Test
        fun `throw ActivityNotFoundException with id when activity is not in the database`() {
            val id = 1L

            whenever(activityService.getActivityById(1L)).thenThrow(ActivityNotFoundException(1))
            whenever(projectService.findById(vacationProject.id)).thenReturn(vacationProject.toDomain())

            val exception = assertThrows<ActivityNotFoundException> {
                activityValidator.checkActivityIsValidForDeletion(id)
            }
            assertEquals(id, exception.id)
        }

        @Test
        fun `do nothing when activity started last year`() {
            val id = 1L
            val activity = Activity(
                id,
                someYearsAgoLocalDateTime(1),
                someYearsAgoLocalDateTime(1).plusMinutes(HOUR.toLong()),
                HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            whenever(activityService.getActivityById(id)).thenReturn(activity.toDomain())
            whenever(projectService.findById(vacationProject.id)).thenReturn(vacationProject.toDomain())

            activityValidator.checkActivityIsValidForDeletion(id)
        }

        @Test
        fun `throw ActivityPeriodClosedException when activity started more than one year ago`() {
            val id = 1L
            val activity = Activity(
                id,
                someYearsAgoLocalDateTime(2),
                someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
                HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )
            whenever(activityService.getActivityById(id)).thenReturn(activity.toDomain())
            whenever(projectService.findById(vacationProject.id)).thenReturn(vacationProject.toDomain())

            assertThrows<ActivityPeriodClosedException> {
                activityValidator.checkActivityIsValidForDeletion(id)
            }
        }

        @Test
        fun `throw ProjectBlockedException when project is blocked`() {
            val id = 1L
            val activity = Activity(
                id,
                someYearsAgoLocalDateTime(2),
                someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
                HOUR,
                "description",
                projectRoleWithBlockedProject,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )
            whenever(activityService.getActivityById(id)).thenReturn(activity.toDomain())
            whenever(projectService.findById(blockedProject.id)).thenReturn(blockedProject.toDomain())

            assertThrows<ProjectBlockedException> {
                activityValidator.checkActivityIsValidForDeletion(id)
            }
        }
    }

    private companion object {

        private val user = createDomainUser()
        private val today = LocalDate.now()
        private val userHiredLastYear = createDomainUser(hiringDate = LocalDate.of(today.year - 1, Month.FEBRUARY, 22))

        private const val HOUR = 60
        private const val DECIMAL_HOUR = 60.0
        private const val CLOSED_ID = 2L

        private val yesterdayDateTime = LocalDateTime.of(LocalDate.now().minusDays(2), LocalTime.now())
        private val todayDateTime =
            LocalDateTime.of(LocalDate.now().year, LocalDate.now().month, LocalDate.now().dayOfMonth, 0, 0)

        private val firstDayOfYear = LocalDateTime.of(LocalDate.now().year, Month.JANUARY, 1, 0, 0)
        private val lastDayOfYear = LocalDateTime.of(LocalDate.now().year, Month.DECEMBER, 31, 23, 59)

        private val nonBlockedProject = Project(
            1,
            "NonBlockedProject",
            true,
            true,
            LocalDate.now(),
            null,
            null,
            Organization(1, "Organization", emptyList()),
            emptyList()
        )

        private val blockedPastProject = Project(
            3,
            "NonBlockedProject",
            true,
            true,
            LocalDate.now(),
            LocalDate.parse("2000-01-01"),
            user.id,
            Organization(1, "Organization", emptyList()),
            emptyList()
        )

        private val vacationProject =
            Project(
                1,
                "Vacaciones",
                true,
                true,
                LocalDate.now(),
                null,
                null,
                Organization(1, "Organization", emptyList()),
                emptyList()
            )

        private val projectRoleWithPastBlockedProject = ProjectRole(
            1,
            "blocked",
            RequireEvidence.NO,
            blockedPastProject,
            0, true, false, TimeUnit.MINUTES
        )

        private val projectRoleWithNonBlockedProject = ProjectRole(
            1,
            "blocked",
            RequireEvidence.NO,
            nonBlockedProject,
            0, true, false, TimeUnit.MINUTES
        )

        private val projectRole =
            ProjectRole(1, "vac", RequireEvidence.NO, vacationProject, 0, true, false, TimeUnit.MINUTES)
        private val closedProject =
            Project(
                CLOSED_ID,
                "TNT",
                false,
                false,
                LocalDate.now(),
                null,
                null,
                Organization(1, "Autentia", emptyList()),
                emptyList()
            )
        private val blockedProject =
            Project(
                3,
                "Blocked Project",
                true,
                true,
                LocalDate.now(),
                LocalDate.of(Year.now().value, 1, 1),
                null,
                Organization(1, "Organization", emptyList()),
                emptyList()
            )
        private val blockedProjectRole =
            ProjectRole(4, "Architect", RequireEvidence.NO, blockedProject, 0, true, false, TimeUnit.MINUTES)
        private val closedProjectRole =
            ProjectRole(CLOSED_ID, "Architect", RequireEvidence.NO, closedProject, 0, true, false, TimeUnit.MINUTES)
        private val projectRoleLimited =
            ProjectRole(3, "vac", RequireEvidence.NO, vacationProject, (HOUR * 8), false, false, TimeUnit.MINUTES)
        private val projectRoleWithBlockedProject = ProjectRole(
            1,
            "blocked",
            RequireEvidence.NO,
            blockedProject,
            0, true, false, TimeUnit.MINUTES
        )


        private val activityNotReachedLimitUpdate = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now()).plusMinutes(HOUR * 5L),
            duration = HOUR * 5,
            projectRole = projectRoleLimited
        )

        private val activityReachedLimitUpdate = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now())
                .plusMinutes(projectRoleLimited.maxAllowed.toLong()),
            duration = projectRoleLimited.maxAllowed,
            projectRole = projectRoleLimited
        )

        private val activityAYearAgoUpdated = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now().minusYears(1L), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now().minusYears(1L), LocalTime.now())
                .plusMinutes((projectRoleLimited.maxAllowed - 120).toLong()),
            duration = projectRoleLimited.maxAllowed - 120,
            projectRole = projectRoleLimited
        )

        private val activityReachedLimitTimeOnly = createActivity(
            start = yesterdayDateTime,
            end = yesterdayDateTime.plusMinutes(projectRoleLimited.maxAllowed.toLong()),
            duration = projectRoleLimited.maxAllowed,
            projectRole = projectRoleLimited
        )

        private val activityReachedLimitTimeOnlyAYearAgo = createActivity(
            start = yesterdayDateTime.minusYears(1L),
            end = yesterdayDateTime.minusYears(1L).plusMinutes(projectRoleLimited.maxAllowed.toLong()),
            duration = projectRoleLimited.maxAllowed,
            projectRole = projectRoleLimited
        )

        private val activityForLimitedProjectRoleAYearAgo = createActivity(
            id = 1L,
            start = yesterdayDateTime.minusYears(1L),
            end = yesterdayDateTime.minusYears(1L).plusMinutes(projectRoleLimited.maxAllowed - 120L),
            duration = projectRoleLimited.maxAllowed - 120,
            projectRole = projectRoleLimited
        )

        private val otherActivityForLimitedProjectRoleAYearAgo = createActivity(
            start = yesterdayDateTime.minusYears(1L),
            end = yesterdayDateTime.minusYears(1L).plusMinutes(120),
            duration = 120,
            projectRole = projectRoleLimited.toDomain(),
        )

        private val activityReachedLimitTodayTimeOnly = createActivity(
            start = todayDateTime,
            end = todayDateTime.plusMinutes(projectRoleLimited.maxAllowed.toLong()),
            duration = projectRoleLimited.maxAllowed,
            projectRole = projectRoleLimited
        )

        private val activityReachedHalfHourTimeOnly = createActivity(
            start = todayDateTime,
            end = todayDateTime.plusMinutes(HOUR.toLong()),
            duration = HOUR,
            projectRole = projectRoleLimited
        )

        private val activityNotReachedLimitTimeOnly = createActivity(
            start = todayDateTime,
            end = todayDateTime.plusMinutes(HOUR * 5L),
            duration = HOUR * 5,
            projectRole = projectRoleLimited.toDomain()
        )

        private val newActivityInMarch = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null,
            TimeInterval.of(
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong())
            ),
            HOUR,
            "description",
            projectRole.toDomain(),
            1L,
            false,
            null,
            null,
            false,
            ApprovalState.NA
        )

        private val newActivityInClosedProject = createDomainActivity(
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
            HOUR,
            closedProjectRole.toDomain()
        ).copy(id = null)

        private val newActivityLastYear = createDomainActivity(
            someYearsAgoLocalDateTime(1),
            someYearsAgoLocalDateTime(1).plusMinutes(HOUR.toLong()).plusMinutes(HOUR.toLong()),
            HOUR,
            projectRole.toDomain()
        ).copy(id = null)

        private val newActivityTwoYearsAgo = createDomainActivity(
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
            HOUR,
            projectRole.toDomain()
        ).copy(id = null)

        private val activityLastYear = createDomainActivity(
            someYearsAgoLocalDateTime(1),
            someYearsAgoLocalDateTime(1).plusMinutes(HOUR.toLong()),
            HOUR,
            projectRole.toDomain()
        )
        private val activityUpdateNonexistentID = createDomainActivity(
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
            HOUR,
            projectRole.toDomain()
        )

        private val activityInvalidPeriodForMinutesProjectRole = createDomainActivity(
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 26, 10, 0, 0).plusMinutes(HOUR.toLong()),
            HOUR,
            projectRole.toDomain()
        )

        private val newActivityBeforeBlockedProject = createDomainActivity(
            someYearsAgoLocalDateTime(1),
            someYearsAgoLocalDateTime(1).plusMinutes(HOUR.toLong()).plusMinutes(HOUR.toLong()),
            HOUR,
            blockedProjectRole.toDomain()
        ).copy(id = null)

        private val newActivityAfterBlockedProject = createDomainActivity(
            someYearsLaterLocalDateTime(1),
            someYearsLaterLocalDateTime(1).plusMinutes(HOUR.toLong()).plusMinutes(HOUR.toLong()),
            HOUR,
            blockedProjectRole.toDomain()
        ).copy(id = null)

        private val newActivitySameDayBlockedProject = createDomainActivity(
            blockedProject.blockDate!!.atTime(LocalTime.of(8, 30)),
            blockedProject.blockDate!!.atTime(LocalTime.of(14, 0)),
            HOUR,
            blockedProjectRole.toDomain()
        ).copy(id = null)


        private val currentActivity = Activity(
            1L,
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1).plusMinutes(23),
            23,
            "Old description",
            projectRole,
            user.id,
            false,
            approvalState = ApprovalState.NA
        )
        val activityUpdateTwoYearsAgo = createDomainActivity(
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
            HOUR,
            projectRole.toDomain()
        )

        private val validActivityToUpdate = createDomainActivity(
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
            HOUR,
            projectRole.toDomain()
        )

        private val newActivityBeforeHiringDate = createDomainActivity(
            LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month.minus(1), 3, 11, 45),
            LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month.minus(1), 3, 11, 45)
                .plusMinutes(HOUR.toLong()),
            HOUR,
            projectRole.toDomain()
        ).copy(id = null)

        private val newActivityInvalidPeriodForMinutesProjectRole = createDomainActivity(
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 26, 10, 0, 0).plusMinutes(HOUR.toLong()),
            481,
            projectRole.toDomain()
        ).copy(id = null)

        private fun createActivity(
            start: LocalDateTime,
            end: LocalDateTime,
            duration: Int,
            description: String = "",
            projectRole: com.autentia.tnt.binnacle.core.domain.ProjectRole = projectRoleLimited.toDomain(),
            billable: Boolean = false,
            hasEvidences: Boolean = false,
        ) = createDomainActivity(start, end, duration, projectRole).copy(
            description = description,
            billable = billable,
            hasEvidences = hasEvidences
        )

        private fun createActivity(
            id: Long? = null,
            start: LocalDateTime,
            end: LocalDateTime,
            duration: Int,
            description: String = "",
            billable: Boolean = false,
            projectRole: ProjectRole,
            userId: Long = user.id,
            approvalState: ApprovalState = ApprovalState.NA,
        ) = createDomainActivity(
            start = start,
            end = end,
            duration = duration,
            projectRole = projectRole.toDomain()
        ).copy(id = id, description = description, billable = billable, userId = userId, approvalState = approvalState)

        private val organization = Organization(1, "Organization", listOf())

        fun createProjectRoleWithLimit(
            id: Long = projectRoleLimited.id,
            name: String = "Role with limit",
            requireEvidence: RequireEvidence = RequireEvidence.NO,
            project: Project = Project(1, "Project", true, false, LocalDate.now(), null, null, organization, listOf()),
            maxAllowed: Int,
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

        private fun someYearsLaterLocalDateTime(yearsLater: Int) =
            LocalDateTime.of(
                today.year + yearsLater,
                Month.DECEMBER,
                31,
                23,
                59,
                59
            )

    }
}