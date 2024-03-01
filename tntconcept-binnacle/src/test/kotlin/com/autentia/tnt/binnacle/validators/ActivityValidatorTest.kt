package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.config.createProject
import com.autentia.tnt.binnacle.core.domain.*
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.services.*
import io.archimedesfw.commons.time.test.ClockTestUtils
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.time.*
import java.util.*


private val mockToday = LocalDate.of(2023, Month.MARCH, 1)
private val mockNow = LocalDateTime.of(mockToday, LocalTime.NOON)

@TestInstance(PER_CLASS)
internal class ActivityValidatorTest {




    private val holidayRepository = mock<HolidayRepository>()
    private val activityRepository = mock<ActivityRepository>()
    private val projectRepository = mock<ProjectRepository>()

    private val calendarFactory: CalendarFactory = CalendarFactory(holidayRepository)
    private val activitiesCalendarFactory: ActivitiesCalendarFactory = ActivitiesCalendarFactory(calendarFactory)
    private val activityCalendarService = ActivityCalendarService(calendarFactory, activitiesCalendarFactory)

    private val activityService = ActivityService(activityRepository)
    private val activityValidator =
        ActivityValidator(
            activityService,
            activityCalendarService,
            projectRepository
        )

    @Nested
    @TestInstance(PER_CLASS)
    inner class CheckActivityIsValidForCreation {

        @AfterEach
        fun resetMocks() {
            reset(
                projectRepository,
                holidayRepository,
                activityRepository
            )
        }

        @Test
        fun `do nothing when activity is valid`() {
            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(projectRole.project.id)


            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForCreation(newActivityInMarch, user)
            }

        }

        @Test
        fun `do nothing when activity started last year`() {
            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(projectRole.project.id)
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForCreation(newActivityLastYear, user)
            }
        }

        @Test
        fun `do nothing when activity started after block project`() {
            doReturn(Optional.of(blockedProject))
                .whenever(projectRepository)
                .findById(blockedProjectRole.project.id)
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForCreation(newActivityAfterBlockedProject, user)
            }
        }

        @Test
        fun `do nothing when activity is created with not reached limit by year`() {
            val projectRole = createProjectRoleWithLimit(
                1L,
                maxTimeAllowedByYear = MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY * 4,
                maxTimeAllowedByActivity = 0,
                timeUnit = TimeUnit.NATURAL_DAYS
            )

            val activities = listOf(
                Activity.of(
                    createDomainActivity(
                        start = LocalDateTime.of(2023, 5, 15, 0, 0, 0),
                        end = LocalDateTime.of(2023, 5, 16, 23, 59, 59),
                        duration = 960,
                    ),
                    projectRole
                )
            )

            val activityToCreate = createDomainActivity(
                start = LocalDateTime.of(2023, 12, 31, 0, 0, 0),
                end = LocalDateTime.of(2024, 1, 1, 23, 59, 59),
                duration = 960,
                projectRole.toDomain()
            ).copy(id = null)

            val timeInterval = TimeInterval.ofYear(2023)

            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(Companion.projectRole.project.id)

            doReturn(activities)
                .whenever(activityRepository)
                .findByProjectRoleIds(
                    timeInterval.start,
                    timeInterval.end,
                    listOf(projectRole.id),
                    user.id
                )
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForCreation(activityToCreate, user)
            }
        }

        @Test
        fun `throw OverlapsAnotherTimeException when there is already an activity of that user at the same time`() {

            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)

            doReturn(listOf(overlappedExistentActivity))
                .whenever(activityRepository)
                .findOverlapped(
                    overlappedActivityToCreate.getStart(),
                    overlappedActivityToCreate.getEnd(),
                    user.id
                )

            assertThrows<OverlapsAnotherTimeException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(
                        overlappedActivityToCreate,
                        overlappedActivityToCreate,
                        user
                    )
                }
            }
        }

        @Test
        fun `throw ActivityForBlockedProjectException when activity started the same day as a project is blocked`() {
            doReturn(Optional.of(blockedProject))
                .whenever(projectRepository)
                .findById(blockedProjectRole.project.id)

            val exception = assertThrows<ProjectBlockedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForCreation(newActivitySameDayBlockedProject, user)
                }
            }

            assertEquals(blockedProject.blockDate!!, exception.blockedDate)
        }

        @Test
        fun `throw MaxTimePerActivityRoleException if user reaches max time for activity`() {

            val projectRoleLimited = createProjectRoleWithLimit(maxTimeAllowedByYear = 0, maxTimeAllowedByActivity = 20)

            val activity = createActivity(
                start = todayDateTime,
                end = todayDateTime.plusMinutes(30L),
                duration = 30,
                projectRole = projectRoleLimited
            )

            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(vacationProject.id)

            val exception = assertThrows<MaxTimePerActivityRoleException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForCreation(activity, user)
                }
            }

            assertEquals(projectRoleLimited.maxTimeAllowedByActivity, exception.maxAllowedTime)
        }

        @Test
        fun `throw MaxTimePerRoleException if user reaches max time by year for a with a change of year`() {

            val projectRole = createProjectRoleWithLimit(
                1L,
                maxTimeAllowedByYear = MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY * 4,
                maxTimeAllowedByActivity = 0,
                timeUnit = TimeUnit.NATURAL_DAYS
            )

            val activities2023 = listOf(
                Activity.of(
                    createDomainActivity(
                        start = LocalDateTime.of(2023, 5, 15, 0, 0, 0),
                        end = LocalDateTime.of(2023, 5, 17, 23, 59, 59),
                        duration = 1440,
                    ),
                    projectRole
                )
            )

            val activity = createDomainActivity(
                start = LocalDateTime.of(2023, 12, 31, 0, 0, 0),
                end = LocalDateTime.of(2024, 1, 1, 23, 59, 59),
                duration = 960,
                projectRole.toDomain()
            ).copy(id = null)

            val timeInterval2023 = TimeInterval.ofYear(2023)

            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(Companion.projectRole.project.id)

            doReturn(activities2023)
                .whenever(activityRepository)
                .findByProjectRoleIds(
                    timeInterval2023.start,
                    timeInterval2023.end,
                    listOf(projectRole.id),
                    user.id
                )

            val exception = assertThrows<MaxTimePerRoleException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForCreation(activity, user)
                }
            }

            assertEquals(1.0, exception.remainingTime)
            assertEquals(2023, exception.year)
        }

        private fun exceptionProvider() = arrayOf(
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
                "ProjectBlockedException",
                newActivityBeforeBlockedProject,
                blockedProjectRole,
                user,
                ProjectBlockedException(blockedProject.blockDate!!)
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
            arrayOf(
                "ActivityBeforeProjectCreationDateException",
                newActivityBeforeProjectCreationDate,
                projectRoleWithNonBlockedProject,
                user,
                ActivityBeforeProjectCreationDateException()
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

            doReturn(Optional.of(projectRole.project))
                .whenever(projectRepository)
                .findById(projectRole.project.id)

            val exception = assertThrows<BinnacleException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForCreation(activityToValidate, user)
                }
            }

            assertEquals(expectedException.message, exception.message)
        }

        private fun maxTimeRoleLimitProviderCreate() = arrayOf(
            arrayOf(
                "with nothing imputed, activity in minutes is added that exceeds the limit",
                emptyList<Activity>(),
                projectRoleLimitedByYear,
                createActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 9L),
                    duration = (MINUTES_IN_HOUR * 9)
                ).copy(id = null),
                480.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "with activities imputed the year before reached limit, activity in minutes is added that exceeds the limit",
                listOf(
                    Activity.of(
                        activityReachedLimitTimeOnlyAYearAgo, projectRoleLimitedByYear
                    )
                ),
                projectRoleLimitedByYear,
                createActivity(
                    start = todayDateTime.minusYears(1L),
                    end = todayDateTime.minusYears(1L).plusMinutes(MINUTES_IN_HOUR * 9L),
                    duration = (MINUTES_IN_HOUR * 9)
                ).copy(id = null),
                0.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "with activities imputed and not reached the limit, activity in minutes is added that exceeds the limit",
                listOf(
                    Activity.of(
                        activityNotReachedLimitTimeOnly,
                        projectRoleLimitedByYear
                    )
                ),
                projectRoleLimitedByYear,
                createActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 10L),
                    duration = (MINUTES_IN_HOUR * 10)
                ).copy(id = null),
                180,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "with activities imputed reached limit, activity in minutes is added that exceeds the limit",
                listOf(Activity.of(activityReachedLimitTimeOnly, projectRoleLimitedByYear)),
                projectRoleLimitedByYear,
                createActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 9L),
                    duration = (MINUTES_IN_HOUR * 9)
                ).copy(id = null),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "with activities imputed the same day reached limit, activity in minutes is added that exceeds the limit",
                listOf(Activity.of(activityReachedLimitTodayTimeOnly, projectRoleLimitedByYear)),
                projectRoleLimitedByYear,
                createActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 9L),
                    duration = (MINUTES_IN_HOUR * 9)
                ).copy(id = null),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "with activities imputed reached limit, activity in days is added that exceeds the limit",
                listOf(
                    Activity.of(
                        createActivity(
                            start = LocalDate.of(2023, 6, 21).atTime(LocalTime.MIN),
                            end = LocalDate.of(2023, 6, 22).atTime(23, 59, 59),
                            duration = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                            projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                                .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                        ),
                        projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                            .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                    )
                ),
                projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                    .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                createActivity(
                    start = LocalDate.of(2023, 6, 21).plusDays(2L).atTime(LocalTime.MIN),
                    end = LocalDate.of(2023, 6, 21).plusDays(2L).atTime(23, 59, 59),
                    duration = MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                    projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                        .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                ).copy(id = null),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ), arrayOf(
                "with nothing imputed, activity in days is added that exceeds the limit",
                emptyList<Activity>(),
                projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS),
                createActivity(
                    start = LocalDate.of(2023, 6, 21).atTime(LocalTime.MIN),
                    end = LocalDate.of(2023, 6, 22).atTime(23, 59, 59),
                    duration = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                    projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                ).copy(id = null),
                1.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "with activities imputed reached limit, activity in days is added that exceeds the limit",
                listOf(
                    Activity.of(
                        createActivity(
                            start = LocalDate.of(2023, 6, 21).atTime(LocalTime.MIN),
                            end = LocalDate.of(2023, 6, 21).plusDays(1L).atTime(23, 59, 59),
                            duration = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                            projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                                .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                        ),
                        projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                            .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                    )
                ),
                projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                    .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                createActivity(
                    start = LocalDate.of(2023, 6, 21).plusDays(2L).atTime(LocalTime.MIN),
                    end = LocalDate.of(2023, 6, 21).plusDays(2L).atTime(23, 59, 59),
                    duration = MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                    projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                        .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                ).copy(id = null),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "with activities imputed not reached limit, activity in days is added that exceeds the limit",
                listOf(
                    Activity.of(
                        createActivity(
                            start = LocalDate.of(2023, 6, 21).atTime(LocalTime.MIN),
                            end = LocalDate.of(2023, 6, 21).atTime(23, 59, 59),
                            duration = MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                            projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                                .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                        ),
                        projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                            .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                    )
                ),
                projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                    .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                createActivity(
                    start = LocalDate.of(2023, 6, 22).atTime(LocalTime.MIN),
                    end = LocalDate.of(2023, 6, 23).atTime(23, 59, 59),
                    duration = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                    projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                        .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                ).copy(id = null),
                1.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "with nothing imputed, activity in natural days is added that exceeds the limit",
                emptyList<Activity>(),
                projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS),
                createActivity(
                    start = LocalDate.of(2023, 6, 21).atTime(LocalTime.MIN),
                    end = LocalDate.of(2023, 6, 22).atTime(23, 59, 59),
                    duration = MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                    projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS)
                ).copy(id = null),
                1.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "with activities imputed reached limit, activity in natural days is added that exceeds the limit",
                listOf(
                    Activity.of(
                        createActivity(
                            start = LocalDate.of(2023, 6, 21).atTime(LocalTime.MIN),
                            end = LocalDate.of(2023, 6, 21).plusDays(1L).atTime(23, 59, 59),
                            duration = (DAYS - 1) * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                            projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS)
                                .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                        ), projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS)
                            .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                    )
                ),
                projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS)
                    .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                createActivity(
                    start = LocalDate.of(2023, 6, 21).plusDays(2L).atTime(LocalTime.MIN),
                    end = LocalDate.of(2023, 6, 21).plusDays(3L).atTime(23, 59, 59),
                    duration = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                    projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS)
                        .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                ).copy(id = null),
                0.0,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "with activities imputed not reached limit, activity in natural days is added that exceeds the limit",
                listOf(
                    Activity.of(
                        createActivity(
                            start = LocalDate.of(2023, 6, 21).atTime(LocalTime.MIN),
                            end = LocalDate.of(2023, 6, 21).atTime(23, 59, 59),
                            duration = (DAYS - 1) * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                            projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS)
                                .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                        ), projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS)
                            .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                    )
                ),
                projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS)
                    .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                createActivity(
                    start = LocalDate.of(2023, 6, 21).plusDays(1L).atTime(LocalTime.MIN),
                    end = LocalDate.of(2023, 6, 21).plusDays(2L).atTime(23, 59, 59),
                    duration = MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                    projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS)
                        .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                ).copy(id = null),
                1.0,
                firstDayOfYear,
                lastDayOfYear
            )
        )

        @ParameterizedTest
        @MethodSource("maxTimeRoleLimitProviderCreate")
        fun `throw MaxTimePerRoleException if user reaches max time for a role`(
            testDescription: String,
            activitiesInTheYear: List<com.autentia.tnt.binnacle.core.domain.Activity>,
            projectRoleLimited: ProjectRole,
            activity: com.autentia.tnt.binnacle.core.domain.Activity,
            expectedRemainingHours: Double,
            firstDay: LocalDateTime,
            lastDay: LocalDateTime,
        ) {
            val timeInterval = TimeInterval.of(firstDay, lastDay)

            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(projectRole.project.id)

            doReturn(activitiesInTheYear)
                .whenever(activityRepository)
                .findByProjectRoleIds(
                    timeInterval.start,
                    timeInterval.end,
                    listOf(activity.projectRole.id),
                    user.id
                )

            val exception = assertThrows<MaxTimePerRoleException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForCreation(activity, user)
                }
            }

            assertEquals(projectRoleLimited.toDomain().getMaxTimeAllowedByYearInTimeUnits(), exception.maxAllowedTime)
            assertEquals(expectedRemainingHours, exception.remainingTime)
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    inner class CheckActivityIsValidForUpdate {
        @Test
        fun `do nothing when activity is valid`() {
            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForUpdate(validActivityToUpdate, validActivityToUpdate, user)
            }
        }

        @Test
        fun `throw ActivityPeriodInvalidException when TimeInterval is longer than a day for a Minutes TimeUnit project role`() {
            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)

            assertThrows<ActivityPeriodNotValidException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(
                        activityInvalidPeriodForMinutesProjectRole,
                        activityInvalidPeriodForMinutesProjectRole,
                        user
                    )
                }
            }
        }

        @Test
        fun `throw NoEvidenceInActivityException when activity evidence is incoherent`() {
            val firstIncoherentActivity =
                createDomainActivity().copy(hasEvidences = true, evidence = null)
            val secondIncoherentActivity =
                createDomainActivity().copy(hasEvidences = false, evidence = Evidence("", ""))


            doReturn(Optional.of(createProject()))
                .whenever(projectRepository)
                .findById(firstIncoherentActivity.projectRole.project.id)

            doReturn(Optional.of(createProject()))
                .whenever(projectRepository)
                .findById(secondIncoherentActivity.projectRole.project.id)

            assertThrows<NoEvidenceInActivityException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(
                        firstIncoherentActivity,
                        firstIncoherentActivity,
                        user
                    )
                }
            }
            assertThrows<NoEvidenceInActivityException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(
                        secondIncoherentActivity,
                        secondIncoherentActivity,
                        user
                    )
                }
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

            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(nonBlockedProject.id)

            doReturn(Optional.of(blockedProject))
                .whenever(projectRepository)
                .findById(blockedProject.id)

            val exception = assertThrows<ProjectBlockedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain(), user)
                }
            }

            assertEquals(blockedProject.blockDate!!, exception.blockedDate)
        }

        @Test
        fun `do nothing when blocked date does not block current change`() {
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

            doReturn(Optional.of(blockedPastProject))
                .whenever(projectRepository)
                .findById(blockedPastProject.id)
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain(), user)
            }
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

            doReturn(Optional.of(blockedProject))
                .whenever(projectRepository)
                .findById(blockedProject.id)


            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(nonBlockedProject.id)

            val exception = assertThrows<ProjectBlockedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain(), user)
                }
            }

            assertEquals(blockedProject.blockDate!!, exception.blockedDate)
        }

        @Test
        fun `throw ProjectClosedException when chosen project is already closed`() {
            val newActivity = createDomainActivity(
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                60,
                closedProjectRole.toDomain()
            )

            doReturn(Optional.of(closedProject))
                .whenever(projectRepository)
                .findById(closedProject.id)

            assertThrows<ProjectClosedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, user)
                }
            }
        }

        @Test
        fun `do nothing when updated activity started last year`() {
            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForUpdate(activityLastYear, activityLastYear, user)
            }
        }

        @Test
        fun `throw ActivityPeriodClosedException when updated activity started more than one year ago`() {
            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)

            assertThrows<ActivityPeriodClosedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(
                        activityUpdateTwoYearsAgo,
                        activityUpdateTwoYearsAgo,
                        user
                    )
                }
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

            val activities = listOf(
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

            doReturn(activities)
                .whenever(activityRepository)
                .findOverlapped(
                    newActivity.getStart(),
                    newActivity.getEnd(),
                    user.id
                )

            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)

            assertThrows<OverlapsAnotherTimeException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, user)
                }
            }
        }

        @Test
        fun `throw IllegalArgumentException when the CURRENT activity to update is already approved`() {
            val approvedActivity = validActivityToUpdate.copy(approvalState = ApprovalState.ACCEPTED)

            assertThrows<IllegalArgumentException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(approvedActivity, approvedActivity, user)
                }
            }
        }

        @Test
        fun `throw IllegalArgumentException when the activity to update has not an id`() {
            assertThrows<IllegalArgumentException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(
                        activityUpdateNonexistentID,
                        activityUpdateNonexistentID,
                        user
                    )
                }
            }
        }

        @Test
        fun `throw IllegalArgumentException when the activity to delete is already approved`() {
            val approvedActivity = validActivityToUpdate.copy(approvalState = ApprovalState.ACCEPTED)

            doReturn(Optional.of(createProject()))
                .whenever(projectRepository)
                .findById(approvedActivity.projectRole.project.id)

            assertThrows<IllegalArgumentException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForDeletion(approvedActivity)
                }
            }
        }

        private fun maxTimeRoleLimitProviderUpdate() = arrayOf(
            arrayOf(
                "reached limit no remaining hours for activity related to the year before",
                projectRoleLimitedByYear,
                listOf(activityForLimitedProjectRoleAYearAgo, otherActivityForLimitedProjectRoleAYearAgo),
                activityForLimitedProjectRoleAYearAgo,
                activityForLimitedProjectRoleAYearAgoUpdated.copy(id = activityAYearAgoUpdated.id),
                0.0
            ),
            arrayOf(
                "not reached limit remaining hours left related to the year before",
                projectRoleLimitedByYear,
                listOf(activityForLimitedProjectRoleAYearAgo),
                activityForLimitedProjectRoleAYearAgo,
                activityForLimitedProjectRoleAYearAgoUpdated.copy(id = activityAYearAgoUpdated.id),
                120.0
            ),
            arrayOf(
                "reached limit no remaining hours",
                projectRoleLimitedByYear,
                listOf(activityReachedLimitTimeOnly),
                activityReachedLimitUpdate,
                activity9HoursReachedLimit.copy(id = activityReachedLimitUpdate.id),
                0.0
            ),
            arrayOf(
                "not reached limit remaining hours left",
                projectRoleLimitedByYear,
                listOf(activityNotReachedLimitTimeOnly),
                activityNotReachedLimitUpdate,
                activity9HoursReachedLimit.copy(id = activityNotReachedLimitUpdate.id),
                180.0
            ),
            arrayOf(
                "reached limit no remaining days",
                projectRoleLimitedByYearInDays,
                listOf(activityReachedLimitDaysTimeOnly),
                activityReachedLimitDaysUpdate,
                activity3DaysReachedLimit.copy(id = activityReachedLimitDaysUpdate.id),
                0.0
            ),
            arrayOf(
                "not reached limit remaining days left",
                projectRoleLimitedByYearInDays,
                listOf(activityNotReachedLimitDaysTimeOnly),
                activityNotReachedLimitDaysUpdate,
                activity5DaysReachedLimit.copy(id = activityNotReachedLimitDaysUpdate.id),
                1.0
            ),
            arrayOf(
                "reached limit no remaining days when activity has a change of year",
                projectRoleFourDaysLimitedByYear,
                listOf(activityNotReachedLimitTwoDays, activityTwoDaysWithChangeOfYear),
                activityTwoDaysWithChangeOfYear,
                activityThreeDaysWithChangeOfYear.copy(id = activityTwoDaysWithChangeOfYear.id),
                0.0
            )
        )

        @ParameterizedTest
        @MethodSource("maxTimeRoleLimitProviderUpdate")
        fun `throw MaxTimePerRoleException if user reaches max time for a role`(
            testDescription: String,
            projectRole: ProjectRole,
            activitiesInTheYear: List<com.autentia.tnt.binnacle.core.domain.Activity>,
            currentActivity: com.autentia.tnt.binnacle.core.domain.Activity,
            activityToUpdate: com.autentia.tnt.binnacle.core.domain.Activity,
            expectedRemainingHours: Double,
        ) {

            doReturn(activitiesInTheYear.map { Activity.of(it, projectRole) })
                .whenever(activityRepository)
                .findByProjectRoleIds(
                    any(),
                    any(),
                    any(),
                    any(),
                )

            val isInMinutes = projectRole.timeUnit == TimeUnit.MINUTES

            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)

            val exception = assertThrows<MaxTimePerRoleException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(activityToUpdate, currentActivity, user)
                }
            }

            if (isInMinutes) {
                assertEquals(projectRole.maxTimeAllowedByYear.toDouble(), exception.maxAllowedTime)
            } else {
                assertEquals(projectRole.maxTimeAllowedByYear.toDouble() / (60 * 8), exception.maxAllowedTime)
            }

            assertEquals(expectedRemainingHours, exception.remainingTime)
        }

        @Test
        fun `not fail when the activity whose time is overlapped is the activity to be replaced`() {
            val newActivity = createDomainActivity(
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0),
                LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0).plusMinutes(75),
                75,
                projectRole.toDomain()
            )

            doReturn(
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
                .whenever(activityRepository)
                .findOverlapped(
                    LocalDateTime.of(2022, Month.JULY, 7, 0, 0, 0),
                    LocalDateTime.of(2022, Month.JULY, 7, 23, 59, 59),
                    user.id
                )

            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, user)
            }
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
                ).plusMinutes(MINUTES_IN_HOUR.toLong()),
                MINUTES_IN_HOUR,
                projectRole.toDomain()
            )

            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)

            assertThrows<ActivityBeforeHiringDateException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, userHiredLastYear)
                }
            }
        }

        @Test
        fun `throw MaxTimePerActivityException if user reaches max time limit for activity`() {

            val currentActivity = createActivity(
                id = 1L,
                start = todayDateTime,
                end = todayDateTime.plusHours(2L),
                duration = (MINUTES_IN_HOUR * 2),
                projectRole = projectRoleLimitedByActivity
            )

            val updatedActivity = createActivity(
                id = 1L,
                start = todayDateTime,
                end = todayDateTime.plusHours(4L),
                duration = (MINUTES_IN_HOUR * 4),
                projectRole = projectRoleLimitedByActivity
            )

            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(vacationProject.id)

            val exception = assertThrows<MaxTimePerActivityRoleException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForUpdate(updatedActivity, currentActivity, user)
                }
            }

            assertEquals(projectRoleLimitedByActivity.maxTimeAllowedByActivity, exception.maxAllowedTime)
        }

        @Test
        fun `do nothing when activity is updated without reach limit per activity`() {

            val currentActivity = createActivity(
                id = 1L,
                start = todayDateTime,
                end = todayDateTime.plusHours(2L),
                duration = (MINUTES_IN_HOUR * 2),
                projectRole = projectRoleLimitedByActivity
            )

            val updatedActivity = createActivity(
                id = 1L,
                start = todayDateTime,
                end = todayDateTime.plusHours(1L),
                duration = (MINUTES_IN_HOUR * 1),
                projectRole = projectRoleLimitedByActivity
            )

            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(vacationProject.id)

            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForUpdate(updatedActivity, currentActivity, user)
            }
        }

        @Test
        fun `do nothing when activity with a change of year is updated`() {

            val projectRole = createProjectRoleWithLimit(
                1L,
                maxTimeAllowedByYear = MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY * 4,
                maxTimeAllowedByActivity = 0,
                timeUnit = TimeUnit.NATURAL_DAYS
            )

            val activities2023 = listOf(
                Activity.of(
                    createDomainActivity(
                        start = LocalDate.of(2023, 5, 15).atTime(LocalTime.MIN),
                        end = LocalDate.of(2023, 5, 15).atTime(LocalTime.MAX),
                        duration = 480,
                    ),
                    projectRole
                )
            )

            val currentActivity = createDomainActivity(
                start = LocalDate.of(2023, 12, 31).atTime(LocalTime.MIN),
                end = LocalDate.of(2024, 1, 1).atTime(LocalTime.MAX),
                duration = 960,
                projectRole.toDomain()
            )

            val activity = createDomainActivity(
                start = LocalDate.of(2023, 12, 31).atTime(LocalTime.MIN),
                end = LocalDate.of(2024, 1, 2).atTime(LocalTime.MAX),
                duration = 1440,
                projectRole.toDomain()
            )

            val timeInterval2023 = TimeInterval.ofYear(2023)

            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(Companion.projectRole.project.id)

            doReturn(activities2023)
                .whenever(activityRepository)
                .findByProjectRoleIds(
                    timeInterval2023.start,
                    timeInterval2023.end,
                    listOf(projectRole.id),
                    user.id
                )
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForUpdate(activity, currentActivity, user)
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
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(MINUTES_IN_HOUR.toLong()),
                MINUTES_IN_HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(vacationProject.id)
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForDeletion(activity.toDomain())
            }
        }

        @Test
        fun `do nothing when activity started last year`() {
            val id = 1L
            val activity = Activity(
                id,
                someYearsAgoLocalDateTime(1),
                someYearsAgoLocalDateTime(1).plusMinutes(MINUTES_IN_HOUR.toLong()),
                MINUTES_IN_HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(vacationProject.id)
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkActivityIsValidForDeletion(activity.toDomain())
            }
        }

        @Test
        fun `throw ActivityPeriodClosedException when activity started more than one year ago`() {
            val id = 1L
            val activity = Activity(
                id,
                someYearsAgoLocalDateTime(2),
                someYearsAgoLocalDateTime(2).plusMinutes(MINUTES_IN_HOUR.toLong()),
                MINUTES_IN_HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(vacationProject.id)

            assertThrows<ActivityPeriodClosedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForDeletion(activity.toDomain())
                }
            }
        }

        @Test
        fun `throw ProjectBlockedException when project is blocked`() {
            val id = 1L
            val activity = Activity(
                id,
                someYearsAgoLocalDateTime(2),
                someYearsAgoLocalDateTime(2).plusMinutes(MINUTES_IN_HOUR.toLong()),
                MINUTES_IN_HOUR,
                "description",
                projectRoleWithBlockedProject,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            doReturn(Optional.of(blockedProject))
                .whenever(projectRepository)
                .findById(blockedProject.id)

            val exception = assertThrows<ProjectBlockedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForDeletion(activity.toDomain())
                }
            }

            assertEquals(blockedProject.blockDate!!, exception.blockedDate)
        }

        @Test
        fun `allow deletion of an accepted or approved activity when can access to all activities`() {
            val id = 1L
            val activity = Activity(
                id,
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(MINUTES_IN_HOUR.toLong()),
                MINUTES_IN_HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.ACCEPTED
            )

            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(nonBlockedProject.id)
            ClockTestUtils.runWithFixed(mockNow) {
                activityValidator.checkAllAccessActivityIsValidForDeletion(activity.toDomain())
            }
        }

        @Test
        fun `do not allow deletion of an accepted or approved activity when cannot access to all activities`() {
            val id = 1L
            val activity = Activity(
                id,
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(MINUTES_IN_HOUR.toLong()),
                MINUTES_IN_HOUR,
                "description",
                projectRole,
                user.id,
                false,
                approvalState = ApprovalState.ACCEPTED
            )

            doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(nonBlockedProject.id)

            assertThrows<IllegalArgumentException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    activityValidator.checkActivityIsValidForDeletion(activity.toDomain())
                }
            }
        }
    }

    private companion object {

        private val user = createDomainUser()
        private val today = mockToday
        private val userHiredLastYear = createDomainUser(hiringDate = LocalDate.of(today.year - 1, Month.FEBRUARY, 22))

        private const val MINUTES_IN_HOUR = 60
        private const val DAYS = 2
        private const val CLOSED_ID = 2L
        private const val WORKABLE_HOURS_BY_DAY = 8

        private val yesterdayDateTime = LocalDateTime.of(today.minusDays(2), LocalTime.now())
        private val todayDateTime =
            LocalDateTime.of(today.year, today.month, today.dayOfMonth, 0, 0)

        private val firstDayOfYear = LocalDateTime.of(today.year, Month.JANUARY, 1, 0, 0)
        private val lastDayOfYear = LocalDateTime.of(today.year, Month.DECEMBER, 31, 23, 59)

        private val nonBlockedProject = Project(
            1,
            "NonBlockedProject",
            true,
            true,
            LocalDate.now(),
            null,
            null,
            Organization(1, "Organization", 1, emptyList()),
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
            Organization(1, "Organization", 1, emptyList()),
            emptyList()
        )

        private val vacationProject =
            Project(
                1,
                "Vacaciones",
                true,
                true,
                LocalDate.now().minusYears(5),
                null,
                null,
                Organization(1, "Organization", 1, emptyList()),
                emptyList()
            )

        private val projectRoleWithPastBlockedProject = ProjectRole(
            1,
            "blocked",
            RequireEvidence.NO,
            blockedPastProject,
            0,
            0,
            true,
            false,
            TimeUnit.MINUTES
        )

        private val projectRoleWithNonBlockedProject = ProjectRole(
            1,
            "blocked",
            RequireEvidence.NO,
            nonBlockedProject,
            0,
            0,
            true,
            false,
            TimeUnit.MINUTES
        )

        private val projectRole =
            ProjectRole(
                1,
                "vac",
                RequireEvidence.NO,
                vacationProject,
                0,
                0,
                true,
                false,
                TimeUnit.MINUTES
            )
        private val closedProject =
            Project(
                CLOSED_ID,
                "TNT",
                false,
                false,
                LocalDate.now(),
                null,
                null,
                Organization(1, "Autentia", 1, emptyList()),
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
                Organization(1, "Organization", 1, emptyList()),
                emptyList()
            )
        private val blockedProjectRole =
            ProjectRole(
                4,
                "Architect",
                RequireEvidence.NO,
                blockedProject,
                0,
                0,
                true,
                false,
                TimeUnit.MINUTES
            )
        private val closedProjectRole =
            ProjectRole(
                CLOSED_ID,
                "Architect",
                RequireEvidence.NO,
                closedProject,
                0,
                0,
                true,
                false,
                TimeUnit.MINUTES
            )

        private val projectRoleLimitedByYear =
            ProjectRole(
                3,
                "vac",
                RequireEvidence.NO,
                vacationProject,
                (MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                0,
                false,
                false,
                TimeUnit.MINUTES
            )

        private val projectRoleLimitedByYearInDays =
            ProjectRole(
                3,
                "vac",
                RequireEvidence.NO,
                vacationProject,
                2 * (MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                0,
                false,
                false,
                TimeUnit.NATURAL_DAYS
            )

        private val projectRoleFourDaysLimitedByYear =
            ProjectRole(
                1L,
                "4 days project role",
                RequireEvidence.NO,
                vacationProject,
                4 * (MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                0,
                false,
                false,
                TimeUnit.NATURAL_DAYS
            )

        private val projectRoleLimitedByActivity =
            ProjectRole(
                3,
                "vac",
                RequireEvidence.NO,
                vacationProject,
                0,
                (MINUTES_IN_HOUR * 2),
                false,
                false,
                TimeUnit.MINUTES
            )

        private val projectRoleWithBlockedProject = ProjectRole(
            1,
            "blocked",
            RequireEvidence.NO,
            blockedProject,
            0,
            0,
            true,
            false,
            TimeUnit.MINUTES
        )

        private val activityNotReachedLimitUpdate = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now()).plusMinutes(MINUTES_IN_HOUR * 5L),
            duration = MINUTES_IN_HOUR * 5,
            projectRole = projectRoleLimitedByYear
        )

        private val activityNotReachedLimitDaysUpdate = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now()).plusDays(1L),
            duration = 0,
            projectRole = projectRoleLimitedByYearInDays
        )

        private val activityReachedLimitUpdate = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now())
                .plusMinutes(projectRoleLimitedByYear.maxTimeAllowedByYear.toLong()),
            duration = projectRoleLimitedByYear.maxTimeAllowedByYear,
            projectRole = projectRoleLimitedByYear
        )

        private val activityHalfLimit = createActivity(
            id = 3L,
            start = LocalDate.of(2023, 11, 5).atTime(9, 0, 0),
            end = LocalDate.of(2023, 11, 5).atTime(13, 0, 0),
            duration = 240,
            projectRole = projectRoleLimitedByYear
        )

        private val previousActivityHalfLimit = createActivity(
            id = 1L,
            start = LocalDate.of(2023, 2, 5).atTime(9, 0, 0),
            end = LocalDate.of(2023, 2, 5).atTime(13, 0, 0),
            duration = 240,
            projectRole = projectRoleLimitedByYear
        )

        private val previousActivityHalfLimitUpdated = createActivity(
            id = 1L,
            start = LocalDate.of(2023, 2, 5).atTime(9, 0, 0),
            end = LocalDate.of(2023, 2, 5).atTime(14, 0, 0),
            duration = 300,
            projectRole = projectRoleLimitedByYear
        )

        private val activityReachedLimitDaysUpdate = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now())
                .plusMinutes(projectRoleLimitedByYearInDays.maxTimeAllowedByYear.toLong()),
            duration = projectRoleLimitedByYearInDays.maxTimeAllowedByYear,
            projectRole = projectRoleLimitedByYearInDays
        )

        private val activityAYearAgoUpdated = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now().minusYears(1L), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now().minusYears(1L), LocalTime.now())
                .plusMinutes((projectRoleLimitedByYear.maxTimeAllowedByYear - 120).toLong()),
            duration = projectRoleLimitedByYear.maxTimeAllowedByYear - 120,
            projectRole = projectRoleLimitedByYear
        )

        private val activityReachedLimitTimeOnly = createActivity(
            start = yesterdayDateTime,
            end = yesterdayDateTime.plusMinutes(projectRoleLimitedByYear.maxTimeAllowedByYear.toLong()),
            duration = projectRoleLimitedByYear.maxTimeAllowedByYear,
            projectRole = projectRoleLimitedByYear
        )

        private val activityReachedLimitTimeOnlyUpdated = createActivity(
            start = yesterdayDateTime,
            end = yesterdayDateTime.plusMinutes(projectRoleLimitedByYear.maxTimeAllowedByYear.toLong()),
            duration = projectRoleLimitedByYear.maxTimeAllowedByYear,
            projectRole = projectRoleLimitedByYear
        )

        private val activityReachedLimitDaysTimeOnly = createActivity(
            start = yesterdayDateTime,
            end = yesterdayDateTime.plusDays(1L),
            duration = WORKABLE_HOURS_BY_DAY * MINUTES_IN_HOUR,
            projectRole = projectRoleLimitedByYearInDays
        )

        private val activityReachedLimitTimeOnlyAYearAgo = createActivity(
            start = yesterdayDateTime.minusYears(1L),
            end = yesterdayDateTime.minusYears(1L).plusMinutes(projectRoleLimitedByYear.maxTimeAllowedByYear.toLong()),
            duration = projectRoleLimitedByYear.maxTimeAllowedByYear,
            projectRole = projectRoleLimitedByYear
        )

        private val activityForLimitedProjectRoleAYearAgo = createActivity(
            id = 1L,
            start = yesterdayDateTime.minusYears(1L),
            end = yesterdayDateTime.minusYears(1L).plusMinutes(projectRoleLimitedByYear.maxTimeAllowedByYear - 120L),
            duration = projectRoleLimitedByYear.maxTimeAllowedByYear - 120,
            projectRole = projectRoleLimitedByYear
        )

        private val activityForLimitedProjectRoleAYearAgoUpdated = createActivity(
            id = 1L,
            start = yesterdayDateTime.minusYears(1L),
            end = yesterdayDateTime.minusYears(1L).plusMinutes(projectRoleLimitedByYear.maxTimeAllowedByYear + 120L),
            duration = projectRoleLimitedByYear.maxTimeAllowedByYear + 120,
            projectRole = projectRoleLimitedByYear
        )

        private val otherActivityForLimitedProjectRoleAYearAgo = createActivity(
            start = yesterdayDateTime.minusYears(1L),
            end = yesterdayDateTime.minusYears(1L).plusMinutes(120),
            duration = 120,
            projectRole = projectRoleLimitedByYear.toDomain(),
        )

        private val activityReachedLimitTodayTimeOnly = createActivity(
            start = todayDateTime,
            end = todayDateTime.plusMinutes(projectRoleLimitedByYear.maxTimeAllowedByYear.toLong()),
            duration = projectRoleLimitedByYear.maxTimeAllowedByYear,
            projectRole = projectRoleLimitedByYear
        )

        private val activityNotReachedLimitTimeOnly = createActivity(
            start = todayDateTime,
            end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 5L),
            duration = MINUTES_IN_HOUR * 5,
            projectRole = projectRoleLimitedByYear.toDomain()
        )

        private val activityNotReachedLimitDaysTimeOnly = createActivity(
            start = todayDateTime,
            end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 5L),
            duration = MINUTES_IN_HOUR * 5,
            projectRole = projectRoleLimitedByYearInDays.toDomain()
        )

        private val activityNotReachedLimitTwoDays = createActivity(
            start = LocalDate.of(2023, 5, 15).atTime(LocalTime.MIN),
            end = LocalDate.of(2023, 5, 16).atTime(LocalTime.MAX),
            duration = 960,
            projectRole = projectRole
        )

        private val activityTwoDaysWithChangeOfYear = createActivity(
            id = 1L,
            start = LocalDate.of(2023, 12, 31).atTime(LocalTime.MIN),
            end = LocalDate.of(2024, 1, 1).atTime(LocalTime.MAX),
            duration = 960,
            projectRole = projectRoleFourDaysLimitedByYear
        )

        private val activityThreeDaysWithChangeOfYear = createActivity(
            start = LocalDate.of(2023, 12, 31).atTime(LocalTime.MIN),
            end = LocalDate.of(2024, 1, 2).atTime(LocalTime.MAX),
            duration = 1440,
            projectRole = projectRoleFourDaysLimitedByYear
        )

        private val overlappedExistentActivity = Activity(
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

        private val overlappedActivityToCreate = createDomainActivity(
            LocalDateTime.of(2022, Month.JULY, 7, 8, 45, 0),
            LocalDateTime.of(2022, Month.JULY, 7, 10, 0, 0),
            75,
            projectRole.toDomain()
        )

        private val newActivityInMarch = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null,
            TimeInterval.of(
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(MINUTES_IN_HOUR.toLong())
            ),
            MINUTES_IN_HOUR,
            "description",
            projectRole.toDomain(),
            1L,
            false,
            null,
            null,
            false,
            ApprovalState.NA,
            null
        )

        private val newActivityInClosedProject = createDomainActivity(
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            closedProjectRole.toDomain()
        ).copy(id = null)

        private val newActivityLastYear = createDomainActivity(
            someYearsAgoLocalDateTime(1),
            someYearsAgoLocalDateTime(1).plusMinutes(MINUTES_IN_HOUR.toLong()).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            projectRole.toDomain()
        ).copy(id = null)

        private val activityUpdateNonexistentID = createDomainActivity(
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            projectRole.toDomain()
        ).copy(id = null)

        private val newActivityTwoYearsAgo = createDomainActivity(
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            projectRole.toDomain()
        ).copy(id = null)

        private val activityLastYear = createDomainActivity(
            someYearsAgoLocalDateTime(1),
            someYearsAgoLocalDateTime(1).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            projectRole.toDomain()
        )

        private val activityInvalidPeriodForMinutesProjectRole = createDomainActivity(
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 26, 10, 0, 0).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            projectRole.toDomain()
        )

        private val newActivityBeforeBlockedProject = createDomainActivity(
            someYearsAgoLocalDateTime(1),
            someYearsAgoLocalDateTime(1).plusMinutes(MINUTES_IN_HOUR.toLong()).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            blockedProjectRole.toDomain()
        ).copy(id = null)

        private val newActivityAfterBlockedProject = createDomainActivity(
            someYearsLaterLocalDateTime(1),
            someYearsLaterLocalDateTime(1).plusMinutes(MINUTES_IN_HOUR.toLong()).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            blockedProjectRole.toDomain()
        ).copy(id = null)

        private val newActivitySameDayBlockedProject = createDomainActivity(
            blockedProject.blockDate!!.atTime(LocalTime.of(8, 30)),
            blockedProject.blockDate!!.atTime(LocalTime.of(14, 0)),
            MINUTES_IN_HOUR,
            blockedProjectRole.toDomain()
        ).copy(id = null)

        val activityUpdateTwoYearsAgo = createDomainActivity(
            someYearsAgoLocalDateTime(2),
            someYearsAgoLocalDateTime(2).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            projectRole.toDomain()
        )

        private val validActivityToUpdate = createDomainActivity(
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            projectRole.toDomain()
        )

        private val newActivityBeforeHiringDate = createDomainActivity(
            LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month.minus(1), 3, 11, 45),
            LocalDateTime.of(userHiredLastYear.hiringDate.year, userHiredLastYear.hiringDate.month.minus(1), 3, 11, 45)
                .plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            projectRole.toDomain()
        ).copy(id = null)

        private val newActivityInvalidPeriodForMinutesProjectRole = createDomainActivity(
            LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2022, Month.MARCH, 26, 10, 0, 0).plusMinutes(MINUTES_IN_HOUR.toLong()),
            481,
            projectRole.toDomain()
        ).copy(id = null)

        private val newActivityBeforeProjectCreationDate = createDomainActivity(
            LocalDateTime.of(2023, Month.MARCH, 25, 10, 0, 0),
            LocalDateTime.of(2023, Month.MARCH, 25, 10, 0, 0).plusMinutes(MINUTES_IN_HOUR.toLong()),
            MINUTES_IN_HOUR,
            projectRole.toDomain()
        ).copy(id = null)

        private val activity9HoursReachedLimit = createDomainActivity(
            start = todayDateTime,
            end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 9L),
            duration = MINUTES_IN_HOUR * 9,
            projectRole = projectRoleLimitedByYear.toDomain()
        )

        private val activity3DaysReachedLimit = createDomainActivity(
            start = todayDateTime,
            end = todayDateTime.plusDays(2L),
            duration = 1440,
            projectRole = projectRoleLimitedByYearInDays.toDomain()
        )

        private val activity5DaysReachedLimit = createDomainActivity(
            start = todayDateTime,
            end = todayDateTime.plusDays(4L),
            duration = 2400,
            projectRole = projectRoleLimitedByYearInDays.toDomain()
        )

        private val activityWithAcceptedApprovalState =
            createDomainActivity().copy(approvalState = ApprovalState.ACCEPTED)
        private val activityWithNotApplicableApprovalState =
            createDomainActivity().copy(approvalState = ApprovalState.NA)
        private val activityWithoutEvidence = createDomainActivity().copy(approvalState = ApprovalState.PENDING)
        private val activityValidForApproval =
            createDomainActivity().copy(approvalState = ApprovalState.PENDING, hasEvidences = true)

        private fun createActivity(
            start: LocalDateTime,
            end: LocalDateTime,
            duration: Int,
            description: String = "",
            projectRole: com.autentia.tnt.binnacle.core.domain.ProjectRole = projectRoleLimitedByYear.toDomain(),
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

        private val organization = Organization(1, "Organization", 1, listOf())

        fun createProjectRoleWithLimit(
            id: Long = projectRoleLimitedByYear.id,
            name: String = "Role with limit",
            requireEvidence: RequireEvidence = RequireEvidence.NO,
            project: Project = Project(
                1, "Project", true, false, LocalDate.now(), null, null, organization, listOf()
            ),
            maxTimeAllowedByYear: Int,
            maxTimeAllowedByActivity: Int,
            timeUnit: TimeUnit = TimeUnit.MINUTES,
        ) = ProjectRole(
            id,
            name,
            requireEvidence,
            project,
            maxTimeAllowedByYear,
            maxTimeAllowedByActivity,
            true,
            false,
            timeUnit
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
