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
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.ProjectRepository
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
    private val holidayRepository = mock<HolidayRepository>()
    private val activityService = mock<ActivityService>()
    private val projectRoleService = mock<ProjectRoleService>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val projectRepository = mock<ProjectRepository>()
    private val activityValidator =
        ActivityValidator(
            activityService,
            activityCalendarService,
            projectRepository
        )
    private val calendarFactory: CalendarFactory = CalendarFactory(holidayRepository)

    @AfterEach
    fun resetMocks() {
        reset(
            projectRoleService,
            holidayRepository,
            activityService,
            activityCalendarService,
            projectRepository
        )
    }

    @TestInstance(PER_CLASS)
    @Nested
    inner class CheckActivityIsValidForCreation {
        @Test
        fun `do nothing when activity is valid`() {
            whenever(projectRepository.findById(projectRole.project.id)).thenReturn(Optional.of(vacationProject))

            activityValidator.checkActivityIsValidForCreation(newActivityInMarch, user)
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
            whenever(projectRepository.findById(projectRole.project.id)).thenReturn(Optional.of(projectRole.project))


            val exception = assertThrows<BinnacleException> {
                activityValidator.checkActivityIsValidForCreation(activityToValidate, user)
            }

            assertEquals(expectedException.message, exception.message)
        }

        @Test
        fun `do nothing when activity started last year`() {
            whenever(projectRepository.findById(projectRole.project.id)).thenReturn(Optional.of(vacationProject))

            activityValidator.checkActivityIsValidForCreation(newActivityLastYear, user)
        }

        @Test
        fun `do nothing when activity started after block project`() {
            whenever(projectRepository.findById(blockedProjectRole.project.id)).thenReturn(Optional.of(blockedProject))

            activityValidator.checkActivityIsValidForCreation(newActivityAfterBlockedProject, user)
        }

        @Test
        fun `throw ActivityForBlockedProjectException when activity started the same day as a project is blocked`() {
            whenever(projectRepository.findById(blockedProjectRole.project.id)).thenReturn(Optional.of(blockedProject))

            val exception = assertThrows<ProjectBlockedException> {
                activityValidator.checkActivityIsValidForCreation(newActivitySameDayBlockedProject, user)
            }

            assertEquals(blockedProject.blockDate!!, exception.blockedDate)
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
                ApprovalState.NA,
                null
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
            whenever(projectRepository.findById(projectRole.project.id)).thenReturn(Optional.of(vacationProject))

            assertThrows<OverlapsAnotherTimeException> {
                activityValidator.checkActivityIsValidForCreation(newActivity, user)
            }
        }

        private fun maxTimeRoleLimitProviderCreate() = arrayOf(
            arrayOf(
                "reached limit no remaining time the year before",
                listOf(activityReachedLimitTimeOnlyAYearAgo),
                createProjectRoleWithLimit(
                    maxTimeAllowedByYear = (MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                    maxTimeAllowedByActivity = 0
                ),
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
                "reached limit no remaining time",
                listOf(activityReachedLimitTimeOnly),
                createProjectRoleWithLimit(
                    maxTimeAllowedByYear = (MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                    maxTimeAllowedByActivity = 0
                ),
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
                "reached limit no remaining time current day",
                listOf(activityReachedLimitTodayTimeOnly),
                createProjectRoleWithLimit(
                    maxTimeAllowedByYear = (MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                    maxTimeAllowedByActivity = 0
                ),
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
                "reached limit no remaining time for projectRole with days configuration",
                listOf(
                    createActivity(
                        start = LocalDate.of(2023, 6, 21).atTime(LocalTime.MIN),
                        end = LocalDate.of(2023, 6, 21).plusDays(1L).atTime(23, 59, 59),
                        duration = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                        projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.DAYS)
                            .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                    )
                ),
                createProjectRoleWithLimit(
                    maxTimeAllowedByYear = (DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                    maxTimeAllowedByActivity = 0,
                    timeUnit = TimeUnit.DAYS
                ),
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
                "reached limit no remaining time for projectRole with natural days configuration",
                listOf(
                    createActivity(
                        start = LocalDate.of(2023, 6, 21).atTime(LocalTime.MIN),
                        end = LocalDate.of(2023, 6, 21).plusDays(1L).atTime(23, 59, 59),
                        duration = (DAYS - 1) * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY,
                        projectRole = projectRoleLimitedByYear.copy(timeUnit = TimeUnit.NATURAL_DAYS)
                            .copy(maxTimeAllowedByYear = DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY)
                    )
                ),
                createProjectRoleWithLimit(
                    maxTimeAllowedByYear = (DAYS * MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                    maxTimeAllowedByActivity = 0,
                    timeUnit = TimeUnit.NATURAL_DAYS
                ),
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
                "reached limit no remaining time half hour",
                listOf(activityReachedHalfHourTimeOnly),
                createProjectRoleWithLimit(maxTimeAllowedByYear = 90, maxTimeAllowedByActivity = 0),
                createActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 9L),
                    duration = (MINUTES_IN_HOUR * 9),
                    projectRole = createProjectRoleWithLimit(
                        maxTimeAllowedByYear = 90,
                        maxTimeAllowedByActivity = 0
                    )
                ).copy(id = null),
                30,
                firstDayOfYear,
                lastDayOfYear
            ),
            arrayOf(
                "not reached limit remaining time left",
                listOf(activityNotReachedLimitTimeOnly),
                createProjectRoleWithLimit(
                    maxTimeAllowedByYear = (MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY),
                    maxTimeAllowedByActivity = 0
                ),
                createActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 10L),
                    duration = (MINUTES_IN_HOUR * 10)
                ).copy(id = null),
                180,
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
            val calendar = calendarFactory.create(timeInterval.getDateInterval())

            whenever(projectRepository.findById(projectRole.project.id)).thenReturn(Optional.of(vacationProject))
            whenever(activityCalendarService.createCalendar(timeInterval.getDateInterval())).thenReturn(calendar)
            whenever(
                activityService.getActivitiesByProjectRoleIds(
                    timeInterval,
                    listOf(activity.projectRole.id),
                    user.id
                )
            ).thenReturn(activitiesInTheYear)

            val exception = assertThrows<MaxTimePerRoleException> {
                activityValidator.checkActivityIsValidForCreation(activity, user)
            }

            assertEquals(projectRoleLimited.toDomain().getMaxTimeAllowedByYearInTimeUnits(), exception.maxAllowedTime)
            assertEquals(expectedRemainingHours, exception.remainingTime)
        }

        @Test
        fun `throw MaxTimePerRoleException if user reaches max time for a role 2`() {
            val maxAllowed = 480

            val projectRole = createProjectRoleWithLimit(
                1L,
                maxTimeAllowedByYear = maxAllowed,
                maxTimeAllowedByActivity = 0,
                timeUnit = TimeUnit.MINUTES
            )

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
                    projectRole = projectRole.toDomain().copy(
                        timeInfo = TimeInfo(
                            MaxTimeAllowed(
                                projectRole.maxTimeAllowedByYear,
                                projectRole.maxTimeAllowedByActivity
                            ), TimeUnit.DAYS
                        )
                    )
                )
            )
            val activities2023 = listOf(
                createDomainActivity(
                    start = LocalDateTime.of(2023, 1, 1, 8, 0, 0),
                    end = LocalDateTime.of(2023, 1, 1, 16, 0, 0),
                    projectRole = projectRole.toDomain().copy(
                        timeInfo = TimeInfo(
                            MaxTimeAllowed(
                                projectRole.maxTimeAllowedByYear,
                                projectRole.maxTimeAllowedByActivity
                            ), TimeUnit.MINUTES
                        )
                    )
                )
            )

            whenever(projectRepository.findById(Companion.projectRole.project.id)).thenReturn(
                Optional.of(
                    vacationProject
                )
            )
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

            val exception = assertThrows<MaxTimePerRoleException> {
                activityValidator.checkActivityIsValidForCreation(activity, user)
            }

            assertEquals(projectRole.maxTimeAllowedByYear.toDouble(), exception.maxAllowedTime)
            assertEquals(0.0, exception.remainingTime)
            assertEquals(2023, exception.year)
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

            val calendar =
                calendarFactory.create(TimeInterval.of(activity.getStart(), activity.getEnd()).getDateInterval())

            whenever(projectRepository.findById(vacationProject.id)).thenReturn(Optional.of(vacationProject))
            whenever(activityCalendarService.createCalendar(any())).thenReturn(calendar)

            val exception = assertThrows<MaxTimePerActivityRoleException> {
                activityValidator.checkActivityIsValidForCreation(activity, user)
            }

            assertEquals(projectRoleLimited.maxTimeAllowedByActivity, exception.maxAllowedTime)
        }

    }

    @TestInstance(PER_CLASS)
    @Nested
    inner class CheckActivityIsValidForUpdate {
        @Test
        fun `do nothing when activity is valid`() {
            whenever(projectRepository.findById(1L)).thenReturn(Optional.of(nonBlockedProject))

            activityValidator.checkActivityIsValidForUpdate(validActivityToUpdate, validActivityToUpdate, user)
        }

        @Test
        fun `throw ActivityPeriodInvalidException when TimeInterval is longer than a day for a Minutes TimeUnit project role`() {
            whenever(projectRepository.findById(1L)).thenReturn(Optional.of(nonBlockedProject))

            assertThrows<ActivityPeriodNotValidException> {
                activityValidator.checkActivityIsValidForUpdate(
                    activityInvalidPeriodForMinutesProjectRole,
                    activityInvalidPeriodForMinutesProjectRole,
                    user
                )
            }
        }

        @Test
        fun `throw NoEvidenceInActivityException when activity evidence is incoherent`() {
            val firstIncoherentActivity =
                createDomainActivity().copy(hasEvidences = true, evidence = null)
            val secondIncoherentActivity =
                createDomainActivity().copy(hasEvidences = false, evidence = Evidence("", ""))

            whenever(projectRepository.findById(firstIncoherentActivity.projectRole.project.id)).thenReturn(
                Optional.of(
                    createProject()
                )
            )
            whenever(projectRepository.findById(secondIncoherentActivity.projectRole.project.id)).thenReturn(
                Optional.of(
                    createProject()
                )
            )

            assertThrows<NoEvidenceInActivityException> {
                activityValidator.checkActivityIsValidForUpdate(firstIncoherentActivity, firstIncoherentActivity, user)
            }
            assertThrows<NoEvidenceInActivityException> {
                activityValidator.checkActivityIsValidForUpdate(
                    secondIncoherentActivity,
                    secondIncoherentActivity,
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
            whenever(projectRepository.findById(nonBlockedProject.id)).thenReturn(Optional.of(nonBlockedProject))
            whenever(projectRepository.findById(blockedProject.id)).thenReturn(Optional.of(blockedProject))

            val exception = assertThrows<ProjectBlockedException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain(), user)
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
            whenever(projectRepository.findById(blockedPastProject.id)).thenReturn(Optional.of(blockedPastProject))

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
            whenever(projectRepository.findById(nonBlockedProject.id)).thenReturn(Optional.of(nonBlockedProject))
            whenever(projectRepository.findById(blockedProject.id)).thenReturn(Optional.of(blockedProject))

            val exception = assertThrows<ProjectBlockedException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain(), user)
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
            whenever(projectRepository.findById(closedProject.id)).thenReturn(Optional.of(closedProject))

            assertThrows<ProjectClosedException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, user)
            }
        }

        @Test
        fun `do nothing when updated activity started last year`() {
            whenever(projectRepository.findById(1L)).thenReturn(Optional.of(nonBlockedProject))

            activityValidator.checkActivityIsValidForUpdate(activityLastYear, activityLastYear, user)
        }

        @Test
        fun `throw ActivityPeriodClosedException when updated activity started more than one year ago`() {
            whenever(projectRepository.findById(1L)).thenReturn(Optional.of(nonBlockedProject))

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
            whenever(projectRepository.findById(1L)).thenReturn(Optional.of(nonBlockedProject))

            assertThrows<OverlapsAnotherTimeException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, user)
            }
        }

        @Test
        fun `throw IllegalArgumentException when the CURRENT activity to update is already approved`() {
            val approvedActivity = validActivityToUpdate.copy(approvalState = ApprovalState.ACCEPTED)

            assertThrows<IllegalArgumentException> {
                activityValidator.checkActivityIsValidForUpdate(approvedActivity, approvedActivity, user)
            }
        }

        @Test
        fun `throw IllegalArgumentException when the activity to update has not an id`() {
            assertThrows<IllegalArgumentException> {
                activityValidator.checkActivityIsValidForUpdate(
                    activityUpdateNonexistentID,
                    activityUpdateNonexistentID,
                    user
                )
            }
        }

        @Test
        fun `throw IllegalArgumentException when the activity to delete is already approved`() {
            val approvedActivity = validActivityToUpdate.copy(approvalState = ApprovalState.ACCEPTED)
            whenever(projectRepository.findById(approvedActivity.projectRole.project.id)).thenReturn(
                Optional.of(
                    createProject()
                )
            )

            assertThrows<IllegalArgumentException> {
                activityValidator.checkActivityIsValidForDeletion(approvedActivity)
            }
        }

        private fun maxTimeRoleLimitProviderUpdate() = arrayOf(
            arrayOf(
                "reached limit no remaining time for activity related to the year before",
                listOf(activityForLimitedProjectRoleAYearAgo, otherActivityForLimitedProjectRoleAYearAgo),
                activityAYearAgoUpdated,
                createDomainActivity(
                    start = todayDateTime.minusYears(1L),
                    end = todayDateTime.minusYears(1L).plusMinutes((MINUTES_IN_HOUR * 9).toLong()),
                    duration = MINUTES_IN_HOUR * 9,
                    projectRole = projectRoleLimitedByYear.toDomain()
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
                    end = todayDateTime.minusYears(1L).plusMinutes((MINUTES_IN_HOUR * 10).toLong()),
                    duration = MINUTES_IN_HOUR * 10,
                    projectRole = projectRoleLimitedByYear.toDomain()
                ).copy(id = activityNotReachedLimitUpdate.id),
                120.0,
                firstDayOfYear.minusYears(1L),
                lastDayOfYear.minusYears(1L)
            ),
            arrayOf(
                "reached limit no remaining hours",
                listOf(activityReachedLimitTimeOnly),
                activityReachedLimitUpdate,
                createDomainActivity(
                    start = todayDateTime,
                    end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 9L),
                    duration = MINUTES_IN_HOUR * 9,
                    projectRole = projectRoleLimitedByYear.toDomain()
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
                    end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 10L),
                    duration = MINUTES_IN_HOUR * 10,
                    projectRole = projectRoleLimitedByYear.toDomain()
                ).copy(id = activityNotReachedLimitUpdate.id),
                180.0,
                firstDayOfYear,
                lastDayOfYear,
            ),
        )

        @ParameterizedTest
        @MethodSource("maxTimeRoleLimitProviderUpdate")
        fun `throw MaxTimePerRoleException if user reaches max time for a role`(
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
            whenever(
                activityService.getActivitiesByProjectRoleIds(
                    yearTimeInterval,
                    listOf(projectRoleLimitedByYear.id),
                    user.id
                )
            ).thenReturn(activitiesInTheYear)
            whenever(projectRepository.findById(1L)).thenReturn(Optional.of(nonBlockedProject))

            val exception = assertThrows<MaxTimePerRoleException> {
                activityValidator.checkActivityIsValidForUpdate(activityToUpdate, currentActivity, user)
            }

            assertEquals(projectRoleLimitedByYear.maxTimeAllowedByYear.toDouble(), exception.maxAllowedTime)
            assertEquals(expectedRemainingHours, exception.remainingTime)
        }

        @Test
        fun `throw MaxTimePerRoleException if user reaches max time for a role`() {
            val maxAllowed = 480

            val projectRole = createProjectRoleWithLimit(
                1L,
                maxTimeAllowedByYear = maxAllowed,
                maxTimeAllowedByActivity = 0,
                timeUnit = TimeUnit.MINUTES
            )

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
                    projectRole = projectRoleLimitedByYear
                )

            val timeInterval2022 = TimeInterval.ofYear(2022)
            val timeInterval2023 = TimeInterval.ofYear(2023)

            val calendar2022 = calendarFactory.create(timeInterval2022.getDateInterval())
            val calendar2023 = calendarFactory.create(timeInterval2023.getDateInterval())
            val activities2022 = listOf(
                createDomainActivity(
                    start = LocalDateTime.of(2022, 12, 31, 0, 0, 0),
                    end = LocalDateTime.of(2022, 12, 31, 23, 59, 59),
                    projectRole = projectRole.toDomain().copy(
                        timeInfo = TimeInfo(
                            MaxTimeAllowed(
                                projectRole.maxTimeAllowedByYear,
                                projectRole.maxTimeAllowedByActivity
                            ), TimeUnit.DAYS
                        )
                    )
                )
            )
            val activities2023 = listOf(
                createDomainActivity(
                    start = LocalDateTime.of(2023, 1, 1, 8, 0, 0),
                    end = LocalDateTime.of(2023, 1, 1, 16, 0, 0),
                    projectRole = projectRole.toDomain()
                )
            )

            whenever(projectRepository.findById(1L)).thenReturn(Optional.of(nonBlockedProject))
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

            val exception = assertThrows<MaxTimePerRoleException> {
                activityValidator.checkActivityIsValidForUpdate(activityToValidate, currentActivity, user)
            }

            assertEquals(projectRole.maxTimeAllowedByYear.toDouble(), exception.maxAllowedTime)
            assertEquals(0.0, exception.remainingTime)
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


            whenever(projectRepository.findById(1L)).thenReturn(Optional.of(nonBlockedProject))

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
                ).plusMinutes(MINUTES_IN_HOUR.toLong()),
                MINUTES_IN_HOUR,
                projectRole.toDomain()
            )

            whenever(projectRepository.findById(1L)).thenReturn(Optional.of(nonBlockedProject))

            assertThrows<ActivityBeforeHiringDateException> {
                activityValidator.checkActivityIsValidForUpdate(newActivity, newActivity, userHiredLastYear)
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

            val timeInterval = TimeInterval.of(updatedActivity.getStart(), updatedActivity.getEnd())
            val calendar = calendarFactory.create(timeInterval.getDateInterval())

            whenever(activityCalendarService.createCalendar(timeInterval.getDateInterval())).thenReturn(calendar)
            whenever(projectRepository.findById(vacationProject.id)).thenReturn(Optional.of(vacationProject))

            val exception = assertThrows<MaxTimePerActivityRoleException> {
                activityValidator.checkActivityIsValidForUpdate(updatedActivity, currentActivity, user)
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

            val timeInterval = TimeInterval.of(updatedActivity.getStart(), updatedActivity.getEnd())
            val calendar = calendarFactory.create(timeInterval.getDateInterval())

            whenever(activityCalendarService.createCalendar(timeInterval.getDateInterval())).thenReturn(calendar)
            whenever(projectRepository.findById(vacationProject.id)).thenReturn(Optional.of(vacationProject))

            activityValidator.checkActivityIsValidForUpdate(updatedActivity, currentActivity, user)
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

            whenever(projectRepository.findById(vacationProject.id)).thenReturn(Optional.of(vacationProject))

            activityValidator.checkActivityIsValidForDeletion(activity.toDomain())
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

            whenever(projectRepository.findById(vacationProject.id)).thenReturn(Optional.of(vacationProject))

            activityValidator.checkActivityIsValidForDeletion(activity.toDomain())
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

            whenever(projectRepository.findById(vacationProject.id)).thenReturn(Optional.of(vacationProject))

            assertThrows<ActivityPeriodClosedException> {
                activityValidator.checkActivityIsValidForDeletion(activity.toDomain())
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

            whenever(projectRepository.findById(blockedProject.id)).thenReturn(Optional.of(blockedProject))

            val exception = assertThrows<ProjectBlockedException> {
                activityValidator.checkActivityIsValidForDeletion(activity.toDomain())
            }

            assertEquals(blockedProject.blockDate!!, exception.blockedDate)
        }
    }

    @Nested
    inner class CheckActivityIsValidForApproval {
        @Test
        fun `throw InvalidActivityApprovalStateException when activity approval state is accepted`() {
            assertThrows<InvalidActivityApprovalStateException> {
                activityValidator.checkActivityIsValidForApproval(activityWithAcceptedApprovalState)
            }
        }

        @Test
        fun `throw InvalidActivityApprovalStateException when activity approval state is not applicable`() {
            assertThrows<InvalidActivityApprovalStateException> {
                activityValidator.checkActivityIsValidForApproval(activityWithNotApplicableApprovalState)
            }
        }

        @Test
        fun `throw NoEvidenceInActivityException when activity has no evidences`() {
            assertThrows<NoEvidenceInActivityException> {
                activityValidator.checkActivityIsValidForApproval(activityWithoutEvidence)
            }
        }

        @Test
        fun `no exception is thrown when activity is valid for approval`() {
            assertDoesNotThrow { activityValidator.checkActivityIsValidForApproval(activityValidForApproval) }
        }
    }

    private companion object {

        private val user = createDomainUser()
        private val today = LocalDate.now()
        private val userHiredLastYear = createDomainUser(hiringDate = LocalDate.of(today.year - 1, Month.FEBRUARY, 22))

        private const val MINUTES_IN_HOUR = 60
        private const val DAYS = 2
        private const val CLOSED_ID = 2L
        private const val WORKABLE_HOURS_BY_DAY = 8

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
                LocalDate.now().minusYears(5),
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
                (MINUTES_IN_HOUR * 8),
                0,
                false,
                false,
                TimeUnit.MINUTES
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

        private val activityReachedLimitUpdate = createActivity(
            id = 1L,
            start = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            end = LocalDateTime.of(LocalDate.now(), LocalTime.now())
                .plusMinutes(projectRoleLimitedByYear.maxTimeAllowedByYear.toLong()),
            duration = projectRoleLimitedByYear.maxTimeAllowedByYear,
            projectRole = projectRoleLimitedByYear
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

        private val activityReachedHalfHourTimeOnly = createActivity(
            start = todayDateTime,
            end = todayDateTime.plusMinutes(MINUTES_IN_HOUR.toLong()),
            duration = MINUTES_IN_HOUR,
            projectRole = projectRoleLimitedByYear
        )

        private val activityNotReachedLimitTimeOnly = createActivity(
            start = todayDateTime,
            end = todayDateTime.plusMinutes(MINUTES_IN_HOUR * 5L),
            duration = MINUTES_IN_HOUR * 5,
            projectRole = projectRoleLimitedByYear.toDomain()
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

        private val organization = Organization(1, "Organization", listOf())

        fun createProjectRoleWithLimit(
            id: Long = projectRoleLimitedByYear.id,
            name: String = "Role with limit",
            requireEvidence: RequireEvidence = RequireEvidence.NO,
            project: Project = Project(
                1, "Project", true, false, LocalDate.now(), null, null,
                organization, listOf()
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