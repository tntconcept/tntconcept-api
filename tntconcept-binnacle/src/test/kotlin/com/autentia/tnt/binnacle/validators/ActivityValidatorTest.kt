package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.ActivityInterval
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
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

    private val holidayService = mock<HolidayService>()
    private val activityRepository = mock<ActivityRepository>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val activityValidator =
        ActivityValidator(activityRepository, activityCalendarService, projectRoleRepository)
    private val calendarFactory: CalendarFactory = CalendarFactory(holidayService)

    @TestInstance(PER_CLASS)
    @Nested
    inner class CheckActivityIsValidForCreation {
        @Test
        fun `do nothing when activity is valid`() {

            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(Optional.of(projectRole))

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
            activityRequestBody: ActivityRequestBody,
            projectRole: ProjectRole,
            user: User,
            expectedException: BinnacleException,
        ) {
            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(Optional.of(projectRole))

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
            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(Optional.of(projectRole))

            activityValidator.checkActivityIsValidForCreation(newActivityLastYear, user)
        }

        @Test
        fun `throw OverlapsAnotherTimeException when there is already an activity of that user at the same time`() {
            val newActivity = ActivityRequestBody(
                null,
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45),
                LocalDateTime.of(2022, Month.JULY, 7, 10, 0),
                75,
                "New activity",
                false,
                projectRole.id,
                false
            )

            doReturn(
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
                    )
                )
            ).whenever(activityRepository).findOverlapped(
                newActivity.start, newActivity.end
            )
            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(Optional.of(projectRole))

            assertThrows<OverlapsAnotherTimeException> {
                activityValidator.checkActivityIsValidForCreation(newActivity, user)
            }
        }

        private fun maxHoursRoleLimitProviderCreate() = arrayOf(
            arrayOf(
                "reached limit no remaining hours the year before",
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(
                    start = todayDateTime.minusYears(1L),
                    end = todayDateTime.minusYears(1L).plusMinutes(HOUR * 9L), duration = (HOUR * 9)
                ),
                projectRoleLimited.maxAllowed,
                0.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "reached limit no remaining hours",
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 9L),
                    duration = (HOUR * 9)
                ),
                projectRoleLimited.maxAllowed,
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "reached limit no remaining hours current day",
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 9L),
                    duration = (HOUR * 9)
                ),
                projectRoleLimited.maxAllowed,
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "reached limit no remaining hours half hour",
                createProjectRoleWithLimit(maxAllowed = 90),
                createActivityRequestBody(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 9L),
                    duration = (HOUR * 9)
                ),
                HOUR,
                0.5,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "not reached limit remaining hours left",
                createProjectRoleWithLimit(maxAllowed = (HOUR * 8)),
                createActivityRequestBody(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 10L),
                    duration = (HOUR * 10)
                ),
                HOUR * 5,
                3.0,
                firstDayOfYear,
                lastDayOfYear
            ),
        )

        @ParameterizedTest
        @MethodSource("maxHoursRoleLimitProviderCreate")
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`(
            testDescription: String,
            projectRoleLimited: ProjectRole,
            activityRequestBody: ActivityRequestBody,
            duration: Int,
            expectedRemainingHours: Double,
            firstDay: LocalDateTime,
            lastDay: LocalDateTime
        ) {

            val timeInterval = TimeInterval.of(firstDay, lastDay)
            val calendar = calendarFactory.create(timeInterval.getDateInterval())
            val activitiesIntervals = listOf(
                ActivityInterval(firstDay, lastDay, projectRoleLimited.timeUnit)
            )

            whenever(projectRoleRepository.findById(projectRoleLimited.id)).thenReturn(Optional.of(projectRoleLimited))
            whenever(activityCalendarService.createCalendar(timeInterval.getDateInterval())).thenReturn(calendar)
            whenever(
                activityRepository.findIntervals(
                    timeInterval.start,
                    timeInterval.end,
                    projectRoleLimited.id
                )
            ).thenReturn(activitiesIntervals)
            whenever(activityCalendarService.getSumActivitiesDuration(calendar, activitiesIntervals)).thenReturn(
                duration
            )

            whenever(
                activityCalendarService
                    .getDurationByCountingWorkingDays(
                        calendar,
                        activityRequestBody.getTimeInterval(),
                        projectRoleLimited.timeUnit
                    )
            ).thenReturn(activityRequestBody.duration)

            val exception = assertThrows<MaxHoursPerRoleException> {
                activityValidator.checkActivityIsValidForCreation(activityRequestBody, user)
            }

            assertEquals(projectRoleLimited.maxAllowed / DECIMAL_HOUR, exception.maxAllowedHours)
            assertEquals(expectedRemainingHours, exception.remainingHours)
        }

        @Test
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`() {
            val maxAllowed = 1440

            val projectRole = createProjectRoleWithLimit(1L, maxAllowed = maxAllowed)

            val activityRequestBody = createActivityRequestBody(
                start = LocalDateTime.of(2023, 1, 20, 0, 0, 0),
                end = LocalDateTime.of(2023, 1, 20, 0, 0, 0),
                duration = 480,
                projectRoleId = projectRole.id
            )

            val timeInterval2022 = TimeInterval.ofYear(2022)
            val timeInterval2023 = TimeInterval.ofYear(2023)

            val calendar2022 = calendarFactory.create(timeInterval2022.getDateInterval())
            val calendar2023 = calendarFactory.create(timeInterval2023.getDateInterval())
            val activitiesIntervals2022 = listOf(
                ActivityInterval(
                    LocalDateTime.of(2022, 12, 31, 0, 0, 0),
                    LocalDateTime.of(2022, 12, 31, 23, 59, 59), TimeUnit.DAYS
                )
            )
            val activitiesIntervals2023 = listOf(
                ActivityInterval(
                    LocalDateTime.of(2023, 1, 1, 8, 0, 0),
                    LocalDateTime.of(2023, 1, 1, 10, 0, 0), TimeUnit.MINUTES
                )
            )

            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(Optional.of(projectRole))
            whenever(
                activityRepository.findIntervals(
                    timeInterval2022.start,
                    timeInterval2022.end,
                    projectRole.id
                )
            ).thenReturn(activitiesIntervals2022)
            whenever(
                activityRepository.findIntervals(
                    timeInterval2023.start,
                    timeInterval2023.end,
                    projectRole.id
                )
            ).thenReturn(activitiesIntervals2023)

            whenever(activityCalendarService.createCalendar(timeInterval2022.getDateInterval())).thenReturn(calendar2022)
            whenever(activityCalendarService.createCalendar(timeInterval2023.getDateInterval())).thenReturn(calendar2023)

            whenever(
                activityCalendarService.getSumActivitiesDuration(
                    calendar2022,
                    activitiesIntervals2022
                )
            ).thenReturn(maxAllowed)
            whenever(
                activityCalendarService.getSumActivitiesDuration(
                    calendar2023,
                    activitiesIntervals2023
                )
            ).thenReturn(maxAllowed)

            whenever(
                activityCalendarService.getDurationByCountingWorkingDays(
                    calendar2023,
                    activityRequestBody.getTimeInterval(),
                    projectRole.timeUnit
                )
            ).thenReturn(activityRequestBody.duration)

            val exception = assertThrows<MaxHoursPerRoleException> {
                activityValidator.checkActivityIsValidForCreation(activityRequestBody, user)
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
        fun `throw ActivityPeriodInvalidException when TimeInterval is longer than a day for a Minutes TimeUnit project role`() {

            whenever(activityRepository.findById(1L)).thenReturn(currentActivity)
            whenever(projectRoleRepository.findById(any())).thenReturn(Optional.of(projectRole))

            assertThrows<ActivityPeriodNotValidException> {
                activityValidator.checkActivityIsValidForUpdate(activityInvalidPeriodForMinutesProjectRole, user)
            }
        }


        @Test
        fun `throw ProjectRoleNotFoundException with role id when project role is not in the database`() {
            val newActivity = ActivityRequestBody(
                1L,
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                60,
                "description",
                false,
                projectRole.id,
                false,
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

            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(Optional.empty())
            whenever(activityRepository.findById(1L)).thenReturn(currentActivity)

            val exception = assertThrows<ProjectRoleNotFoundException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, user)
            }
            assertEquals(projectRole.id, exception.id)
        }

        @Test
        fun `throw ProjectClosedException when chosen project is already closed`() {
            whenever(projectRoleRepository.findById(any())).thenReturn(Optional.of(closedProjectRole))

            val newActivity = ActivityRequestBody(
                1L,
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                60,
                "description",
                false,
                closedProjectRole.id,
                false,
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
                LocalDateTime.of(2022, Month.JULY, 7, 10, 0, 0),
                75,
                "description",
                false,
                projectRole.id,
                false,
            )
            given(activityRepository.findById(1L)).willReturn(currentActivity)

            given(
                activityRepository.findOverlapped(
                    newActivity.start, newActivity.end
                )
            ).willReturn(
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
                createProjectRoleWithLimit(maxAllowed = HOUR * 8),
                activityAYearAgoUpdated,
                createActivityRequestBodyToUpdate(
                    id = activityAYearAgoUpdated.id!!,
                    start = todayDateTime.minusYears(1L),
                    end = todayDateTime.minusYears(1L).plusMinutes((HOUR * 9).toLong()),
                    duration = HOUR * 9,
                ),
                projectRoleLimited.maxAllowed,
                0.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "reached limit remaining hours left related to the year before",
                createProjectRoleWithLimit(maxAllowed = HOUR * 8),
                activityAYearAgoUpdated,
                createActivityRequestBodyToUpdate(
                    id = activityNotReachedLimitUpdate.id!!,
                    start = todayDateTime.minusYears(1L),
                    end = todayDateTime.minusYears(1L).plusMinutes((HOUR * 10).toLong()),
                    duration = HOUR * 10
                ),
                projectRoleLimited.maxAllowed - 120,
                2.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "reached limit no remaining hours",
                createProjectRoleWithLimit(maxAllowed = HOUR * 8),
                activityReachedLimitUpdate,
                createActivityRequestBodyToUpdate(
                    id = activityReachedLimitUpdate.id!!,
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 9L),
                    duration = HOUR * 9
                ),
                projectRoleLimited.maxAllowed,
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "not reached limit remaining hours left",
                createProjectRoleWithLimit(maxAllowed = HOUR * 8),
                activityNotReachedLimitUpdate,
                createActivityRequestBodyToUpdate(
                    id = activityNotReachedLimitUpdate.id!!,
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(HOUR * 10L),
                    duration = HOUR * 10
                ),
                HOUR * 5,
                3.0,
                firstDayOfYear,
                lastDayOfYear,
            ),
        )

        @ParameterizedTest
        @MethodSource("maxHoursRoleLimitProviderUpdate")
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`(
            testDescription: String,
            projectRoleLimited: ProjectRole,
            currentActivity: Activity,
            activityRequestBodyToUpdate: ActivityRequestBody,
            duration: Int,
            expectedRemainingHours: Double,
            firstDay: LocalDateTime,
            lastDay: LocalDateTime
        ) {

            val timeInterval = TimeInterval.of(firstDay, lastDay)
            val yearTimeInterval = TimeInterval.ofYear(firstDay.year)
            val calendar = calendarFactory.create(timeInterval.getDateInterval())
            val activitiesIntervals = listOf(
                ActivityInterval(currentActivity.getTimeInterval().start, currentActivity.getTimeInterval().end, projectRoleLimited.timeUnit)
            )

            whenever(activityCalendarService.createCalendar(timeInterval.getDateInterval())).thenReturn(calendar)

            whenever(activityRepository.findById(currentActivity.id!!)).thenReturn(currentActivity)
            whenever(activityRepository.findIntervals(yearTimeInterval.start, yearTimeInterval.end, projectRoleLimited.id)).thenReturn(activitiesIntervals)
            whenever(projectRoleRepository.findById(projectRoleLimited.id)).thenReturn(Optional.of(projectRoleLimited))

            whenever(
                activityCalendarService
                    .getDurationByCountingWorkingDays(
                        calendar, activityRequestBodyToUpdate.getTimeInterval(), projectRoleLimited.timeUnit
                    )
            ).thenReturn(activityRequestBodyToUpdate.duration)
            whenever(activityCalendarService.getDurationByCountingWorkingDays(
                    calendar, currentActivity.getTimeInterval(), currentActivity.projectRole.timeUnit
           )).thenReturn(currentActivity.duration)

            whenever(activityCalendarService.getSumActivitiesDuration(calendar, activitiesIntervals)).thenReturn(duration)

            val exception = assertThrows<MaxHoursPerRoleException> {
                activityValidator.checkActivityIsValidForUpdate(activityRequestBodyToUpdate, user)
            }

            assertEquals(projectRoleLimited.maxAllowed / DECIMAL_HOUR, exception.maxAllowedHours)
            assertEquals(expectedRemainingHours, exception.remainingHours)

        }

        @Test
        fun `throw MaxHoursPerRoleException if user reaches max hours for a role`() {
            val maxAllowed = 1440

            val projectRole = createProjectRoleWithLimit(1L, maxAllowed = maxAllowed)

            val activityRequestBody = createActivityRequestBodyToUpdate(
                id = 1L,
                start = LocalDateTime.of(2023, 1, 20, 0, 0, 0),
                end = LocalDateTime.of(2023, 1, 20, 0, 0, 0),
                duration = 480,
                projectRoleId = projectRole.id
            )
            val activity = createActivity(
                id = 1L,
                start = activityRequestBody.start,
                end = activityRequestBody.end,
                duration = 480,
                projectRole = projectRoleLimited
            )
            val timeInterval2022 = TimeInterval.ofYear(2022)
            val timeInterval2023 = TimeInterval.ofYear(2023)

            val calendar2022 = calendarFactory.create(timeInterval2022.getDateInterval())
            val calendar2023 = calendarFactory.create(timeInterval2023.getDateInterval())
            val activitiesIntervals2022 = listOf(
                ActivityInterval(
                    LocalDateTime.of(2022, 12, 31, 0, 0, 0),
                    LocalDateTime.of(2022, 12, 31, 23, 59, 59), TimeUnit.DAYS
                )
            )
            val activitiesIntervals2023 = listOf(
                ActivityInterval(
                    LocalDateTime.of(2023, 1, 1, 8, 0, 0),
                    LocalDateTime.of(2023, 1, 1, 10, 0, 0), TimeUnit.MINUTES
                )
            )

            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(Optional.of(projectRole))
            whenever(activityRepository.findById(activity.id!!)).thenReturn(activity)
            whenever(activityRepository.findIntervals(timeInterval2022.start, timeInterval2022.end, projectRole.id)).thenReturn(activitiesIntervals2022)
            whenever(activityRepository.findIntervals(timeInterval2023.start, timeInterval2023.end, projectRole.id)).thenReturn(activitiesIntervals2023)

            whenever(activityCalendarService.createCalendar(timeInterval2022.getDateInterval())).thenReturn(calendar2022)
            whenever(activityCalendarService.createCalendar(timeInterval2023.getDateInterval())).thenReturn(calendar2023)

            whenever(activityCalendarService.getSumActivitiesDuration(calendar2022, activitiesIntervals2022)).thenReturn(maxAllowed)
            whenever(activityCalendarService.getSumActivitiesDuration(calendar2023, activitiesIntervals2023)).thenReturn(maxAllowed)

            whenever(activityCalendarService.getDurationByCountingWorkingDays(
                    calendar2023,
                    activityRequestBody.getTimeInterval(),
                    projectRole.timeUnit)).thenReturn(activityRequestBody.duration)

            val exception = assertThrows<MaxHoursPerRoleException> {
                activityValidator.checkActivityIsValidForUpdate(activityRequestBody, user)
            }

            assertEquals(projectRole.maxAllowed / DECIMAL_HOUR, exception.maxAllowedHours)
            assertEquals(0.0, exception.remainingHours)
            assertEquals(2023, exception.year)
        }

        @Test
        fun `not fail when the activity whose time is overlapped is the activity to be replaced`() {
            val newActivity = ActivityRequestBody(
                1L,
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0),
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0).plusMinutes(75),
                75,
                "New description",
                false,
                projectRole.id,
                false
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
            given(activityRepository.findById(1L)).willReturn(currentActivity)

            given(
                activityRepository.findOverlapped(
                    LocalDateTime.of(2022, Month.JULY, 7, 0, 0, 0),
                    LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),
                )
            ).willReturn(
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

                    )
                )
            )
            whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(Optional.of(projectRole))

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
                HOUR,
                "Updated activity",
                false,
                projectRole.id,
                false,
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
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
                HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
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
                someYearsAgoLocalDateTime(1).plusMinutes(HOUR.toLong()),
                HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
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
                someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
                HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
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
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now()).plusMinutes(HOUR * 5L),
            duration = HOUR * 5,
            projectRole = projectRoleLimited
        )

        private val activityReachedLimitUpdate = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
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

        private val activityReachedLimitTimeOnly = ActivityInterval(
            yesterdayDateTime,
            yesterdayDateTime.plusMinutes(projectRoleLimited.maxAllowed.toLong()),
            projectRoleLimited.timeUnit
        )

        private val activityReachedLimitTimeOnlyAYearAgo = ActivityInterval(
            yesterdayDateTime.minusYears(1L),
            yesterdayDateTime.minusYears(1L).plusMinutes(projectRoleLimited.maxAllowed.toLong()),
            projectRoleLimited.timeUnit
        )

        private val activityForLimitedProjectRoleAYearAgo = ActivityInterval(
            yesterdayDateTime.minusYears(1L),
            yesterdayDateTime.minusYears(1L).plusMinutes(projectRoleLimited.maxAllowed - 120L),
            projectRoleLimited.timeUnit
        )

        private val otherActivityForLimitedProjectRoleAYearAgo = ActivityInterval(
            yesterdayDateTime.minusYears(1L),
            yesterdayDateTime.minusYears(1L).plusMinutes(120),
            projectRoleLimited.timeUnit
        )

        private val activityNoLimitTimeOnly = ActivityInterval(
            yesterdayDateTime,
            yesterdayDateTime.plusMinutes(HOUR * 8L),
            projectRoleWithoutLimit.timeUnit
        )

        private val activityNoLimitTimeOnlyAYearAgo = ActivityInterval(
            yesterdayDateTime.minusYears(1L),
            yesterdayDateTime.minusYears(1L).plusMinutes(HOUR * 8L),
            projectRoleWithoutLimit.timeUnit
        )

        private val activityReachedLimitTodayTimeOnly = ActivityInterval(
            todayDateTime,
            todayDateTime.plusMinutes(projectRoleLimited.maxAllowed.toLong()),
            projectRoleLimited.timeUnit
        )

        private val activityReachedHalfHourTimeOnly = ActivityInterval(
            todayDateTime,
            todayDateTime.plusMinutes(HOUR.toLong()),
            projectRoleLimited.timeUnit
        )

        private val activityNotReachedLimitTimeOnly = ActivityInterval(
            todayDateTime,
            todayDateTime.plusMinutes(HOUR * 5L),
            projectRoleLimited.timeUnit
        )

        private val newActivityInMarch = ActivityRequestBody(
            null,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
            HOUR,
            "description",
            false,
            projectRole.id,
            false
        )

        private val newActivityInClosedProject = ActivityRequestBody(
            null,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
            HOUR,
            "description",
            false,
            closedProjectRole.id,
            false
        )
        private val newActivityLastYear = ActivityRequestBody(
            null,
            someYearsAgoLocalDateTime(1),
            someYearsAgoLocalDateTime(1).plusMinutes(HOUR.toLong()).plusMinutes(HOUR.toLong()),
            HOUR,
            "description",
            false,
            projectRole.id,
            false,
        )
        private val newActivityTwoYearsAgo = ActivityRequestBody(
            null,
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
            HOUR,
            "description",
            false,
            projectRole.id,
            false,
        )

        private val activityLastYear = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(1),
            someYearsAgoLocalDateTime(1).plusMinutes(HOUR.toLong()),
            HOUR,
            "Updated activity",
            false,
            projectRole.id,
            false
        )
        private val activityUpdateNonexistentID = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
            HOUR,
            "Updated activity",
            false,
            projectRole.id,
            false
        )

        private val activityInvalidPeriodForMinutesProjectRole = ActivityRequestBody(
            1,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 26, 10, 0, 0).plusMinutes(HOUR.toLong()),
            HOUR,
            "description",
            false,
            projectRole.id,
            false
        )

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
        val newActivityRequest = ActivityRequestBody(
            1L,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
            HOUR,
            "description",
            false,
            projectRole.id,
            false,
        )
        val activityUpdateTwoYearsAgo = ActivityRequestBody(
            1,
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(HOUR.toLong()),
            HOUR,
            "Updated activity",
            false,
            projectRole.id,
            false
        )
        private const val anyOtherUserId = 33L

        private val currentActivityAnotherUser = Activity(
            1L,
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1),
            LocalDateTime.of(2020, Month.JANUARY, 3, 2, 1).plusMinutes(23),
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
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(HOUR.toLong()),
            HOUR,
            "description",
            false,
            projectRole.id,
            false
        )

        private val newActivityBeforeHiringDate = ActivityRequestBody(
            null,
            LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month.minus(1), 3, 11, 45),
            LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month.minus(1), 3, 11, 45)
                .plusMinutes(HOUR.toLong()),
            HOUR,
            "description",
            false,
            projectRole.id,
            false
        )

        private val newActivityInvalidPeriodForMinutesProjectRole = ActivityRequestBody(
            null,
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 26, 10, 0, 0).plusMinutes(HOUR.toLong()),
            HOUR,
            "description",
            false,
            projectRole.id,
            false
        )

        private fun createActivityRequestBody(
            start: LocalDateTime,
            end: LocalDateTime,
            duration: Int,
            description: String = "",
            projectRoleId: Long = projectRoleLimited.id,
            billable: Boolean = false,
            hasEvidences: Boolean = false,
        ): ActivityRequestBody =
            ActivityRequestBody(
                start = start,
                end = end,
                duration = duration,
                description = description,
                projectRoleId = projectRoleId,
                billable = billable,
                hasEvidences = hasEvidences
            )

        private fun createActivityRequestBodyToUpdate(
            id: Long,
            start: LocalDateTime,
            end: LocalDateTime,
            duration: Int,
            description: String = "",
            projectRoleId: Long = projectRoleLimited.id,
            billable: Boolean = false,
            hasEvidences: Boolean = false
        ): ActivityRequestBody =
            ActivityRequestBody(
                id = id,
                start = start,
                end = end,
                duration = duration,
                description = description,
                projectRoleId = projectRoleId,
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
            approvalState: ApprovalState = ApprovalState.NA
        ) = Activity(
            id = id,
            start = start,
            end = end,
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
