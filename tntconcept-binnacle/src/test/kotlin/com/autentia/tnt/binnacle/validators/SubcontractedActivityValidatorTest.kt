package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainUser
import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.core.domain.User
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import io.archimedesfw.commons.time.ClockUtils
import io.archimedesfw.commons.time.test.ClockTestUtils
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.time.*
import java.util.*

private val mockToday = ClockUtils.nowUtc().toLocalDate()
private val mockNow = LocalDateTime.of(mockToday, LocalTime.NOON)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SubcontractedActivityValidatorTest {

    private val holidayRepository = mock<HolidayRepository>()
    private val activityRepository = mock<ActivityRepository>()
    private val projectRepository = mock<ProjectRepository>()
    private val activityService = mock<ActivityService>()
    private val calendarService = mock<ActivityCalendarService>()
    private val subcontractedActivityValidator =
        SubcontractedActivityValidator(
            activityService,
            calendarService,
            projectRepository
        )

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
            Mockito.doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(projectRoleWithNonBlockedProject.project.id)


            ClockTestUtils.runWithFixed(mockNow) {
                subcontractedActivityValidator.checkActivityIsValidForCreation(newActivity, user)
            }

        }

        @Test
        fun `do nothing when activity started last year`() {
            Mockito.doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(projectRoleWithNonBlockedProject.project.id)
            ClockTestUtils.runWithFixed(mockNow) {
                subcontractedActivityValidator.checkActivityIsValidForCreation(newActivityLastYear, user)
            }
        }

        @Test
        fun `do nothing when activity started after block project`() {
            Mockito.doReturn(Optional.of(blockedProject))
                .whenever(projectRepository)
                .findById(blockedProjectRole.project.id)
            ClockTestUtils.runWithFixed(mockNow) {
                subcontractedActivityValidator.checkActivityIsValidForCreation(newActivityAfterBlockedProject, user)
            }
        }

        @Test
        fun `throw ProjectClosedException when try to create an activity with a closed project`() {
            Mockito.doReturn(Optional.of(closedProject))
                    .whenever(projectRepository)
                    .findById(2L)

            assertThrows<ProjectClosedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForCreation(
                            newActivityInClosedProject,
                            user
                    )
                }
            }
        }
        @Test
        fun `throw ActivityPeriodClosedException when try to create an activity that starts two years ago`() {
            Mockito.doReturn(Optional.of(nonBlockedProjectTwoYearsAgo))
                    .whenever(projectRepository)
                    .findById(3L)

            assertThrows<ActivityPeriodClosedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForCreation(
                            newActivityTwoYearsAgo,
                            user
                    )
                }
            }
        }

        @Test
        fun `throw ActivityForBlockedProjectException when activity started the same day as a project is blocked`() {
            Mockito.doReturn(Optional.of(blockedProject))
                .whenever(projectRepository)
                .findById(blockedProjectRole.project.id)

            val exception = assertThrows<ProjectBlockedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForCreation(newActivitySameDayBlockedProject, user)
                }
            }

            Assertions.assertEquals(blockedProject.blockDate!!, exception.blockedDate)
        }

        @Test
        fun `throw ActivityBeforeProjectCreationDateException when activity started before the project wash created`() {
            Mockito.doReturn(Optional.of(nonBlockedProject))
                    .whenever(projectRepository)
                    .findById(1L)

            assertThrows<ActivityBeforeProjectCreationDateException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForCreation(
                            newActivityBeforeProjectCreationDate,
                            user
                    )
                }
            }
        }

        @Test
        fun `throw InvalidDurationFormatException when activity duration is negative`() {
            Mockito.doReturn(Optional.of(nonBlockedProject))
                    .whenever(projectRepository)
                    .findById(1L)

            assertThrows<InvalidDurationFormatException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForCreation(
                            newActivityWithNegativeDuration,
                            user
                    )
                }
            }
        }


        private fun exceptionProvider() = arrayOf(
            arrayOf(
                "ProjectClosedException",
                newActivityInClosedProject,
                closedProjectRole,
                user,
                ProjectClosedException()
            ),
            arrayOf(
                "ActivityPeriodClosedException",
                    newActivityTwoYearsAgo,
                    projectRoleWithNonBlockedProjectTwoYearsAgo,
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
                "ActivityBeforeProjectCreationDateException",
                newActivityBeforeProjectCreationDate,
                projectRoleWithNonBlockedProject,
                user,
                ActivityBeforeProjectCreationDateException()
            ),
                arrayOf(
                        "InvalidDurationFormatException",
                        newActivityWithNegativeDuration,
                        projectRoleWithNonBlockedProject,
                        user,
                        InvalidDurationFormatException()
                ),
        )

        @ParameterizedTest
        @MethodSource("exceptionProvider")
        fun `throw exceptions`(
            testDescription: String,
            activityToValidate: Activity,
            projectRole: ProjectRole,
            user: User,
            expectedException: BinnacleException,
        ) {

            Mockito.doReturn(Optional.of(projectRole.project))
                .whenever(projectRepository)
                .findById(projectRole.project.id)

            val exception = assertThrows<BinnacleException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForCreation(activityToValidate, user)
                }
            }

            Assertions.assertEquals(expectedException.message, exception.message)
        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CheckActivityIsValidForUpdate {
        @Test
        fun `do nothing when activity is valid`() {
            Mockito.doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)
            ClockTestUtils.runWithFixed(mockNow) {
                subcontractedActivityValidator.checkActivityIsValidForUpdate(validActivityToUpdate, validActivityToUpdate)
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
            val currentActivity = com.autentia.tnt.binnacle.entities.Activity(
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

            Mockito.doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(nonBlockedProject.id)

            Mockito.doReturn(Optional.of(blockedProject))
                .whenever(projectRepository)
                .findById(blockedProject.id)

            val exception = assertThrows<ProjectBlockedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain())
                }
            }

            Assertions.assertEquals(blockedProject.blockDate!!, exception.blockedDate)
        }

        @Test
        fun `do nothing when blocked date does not block current change`() {
            val newActivity = createDomainActivity(
                    mockNow,
                    mockNow.plusMonths(1),
                    3360,
                projectRoleWithPastBlockedProject.toDomain()
            )
            val currentActivity = com.autentia.tnt.binnacle.entities.Activity(
                1L,
                    mockNow.minusMonths(1),
                    mockNow,
                    3360,
                "Old description",
                projectRoleWithPastBlockedProject,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            Mockito.doReturn(Optional.of(blockedPastProject))
                .whenever(projectRepository)
                .findById(blockedPastProject.id)
            ClockTestUtils.runWithFixed(mockNow) {
                subcontractedActivityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain())
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
            val currentActivity = com.autentia.tnt.binnacle.entities.Activity(
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

            Mockito.doReturn(Optional.of(blockedProject))
                .whenever(projectRepository)
                .findById(blockedProject.id)


            Mockito.doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(nonBlockedProject.id)

            val exception = assertThrows<ProjectBlockedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForUpdate(newActivity, currentActivity.toDomain())
                }
            }

            Assertions.assertEquals(blockedProject.blockDate!!, exception.blockedDate)
        }

        @Test
        fun `throw ProjectClosedException when chosen project is already closed`() {
            val newActivity = createDomainActivity(
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(60L),
                60,
                closedProjectRole.toDomain()
            )

            Mockito.doReturn(Optional.of(closedProject))
                .whenever(projectRepository)
                .findById(closedProject.id)

            assertThrows<ProjectClosedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForUpdate(newActivity, newActivity)
                }
            }
        }

        @Test
        fun `do nothing when updated activity started last year`() {
            Mockito.doReturn(Optional.of(nonBlockedProject))
                .whenever(projectRepository)
                .findById(1L)
            ClockTestUtils.runWithFixed(mockNow) {
                subcontractedActivityValidator.checkActivityIsValidForUpdate(activityLastYear, activityLastYear)
            }
        }

        @Test
        fun `throw ActivityPeriodClosedException when updated activity started more than one year ago`() {
            Mockito.doReturn(Optional.of(nonBlockedProjectTwoYearsAgo))
                .whenever(projectRepository)
                .findById(3L)

            assertThrows<ActivityPeriodClosedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForUpdate(
                        activityUpdateTwoYearsAgo,
                        activityUpdateTwoYearsAgo
                    )
                }
            }
        }

        @Test
        fun `throw IllegalArgumentException when the activity to update has not an id`() {
            assertThrows<IllegalArgumentException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForUpdate(
                        activityUpdateNonexistentID,
                        activityUpdateNonexistentID
                    )
                }
            }
        }

        @Test
        fun `do nothing when activity with a change of year is updated`() {

//            val projectRole = createProjectRoleWithLimit(
//                1L,
//                maxTimeAllowedByYear = MINUTES_IN_HOUR * WORKABLE_HOURS_BY_DAY * 4,
//                maxTimeAllowedByActivity = 0,
//                timeUnit = TimeUnit.NATURAL_DAYS
//            )

            val activities2023 = listOf(
                com.autentia.tnt.binnacle.entities.Activity.of(
                    createDomainActivity(
                        start = LocalDate.of(2023, 5, 15).atTime(LocalTime.MIN),
                        end = LocalDate.of(2023, 5, 15).atTime(LocalTime.MAX),
                        duration = 480,
                    ),
                        projectRoleWithNonBlockedProject
                )
            )

            val currentActivity = createDomainActivity(
                start = LocalDate.of(2023, 12, 31).atTime(LocalTime.MIN),
                end = LocalDate.of(2024, 1, 1).atTime(LocalTime.MAX),
                duration = 960,
                    projectRoleWithNonBlockedProject.toDomain()
            )

            val activity = createDomainActivity(
                start = LocalDate.of(2023, 12, 31).atTime(LocalTime.MIN),
                end = LocalDate.of(2024, 1, 2).atTime(LocalTime.MAX),
                duration = 1440,
                    projectRoleWithNonBlockedProject.toDomain()
            )

            val timeInterval2023 = TimeInterval.ofYear(2023)

            Mockito.doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(Companion.projectRoleWithNonBlockedProject.project.id)

            Mockito.doReturn(activities2023)
                .whenever(activityRepository)
                .findByProjectRoleIds(
                    timeInterval2023.start,
                    timeInterval2023.end,
                    listOf(projectRoleWithNonBlockedProject.id),
                    user.id
                )
            ClockTestUtils.runWithFixed(mockNow) {
                subcontractedActivityValidator.checkActivityIsValidForUpdate(activity, currentActivity)
            }
        }
    }

    @Nested
    inner class CheckActivityIsValidForDeletion {

        @Test
        fun `do nothing when activity to delete is valid`() {
            val id = 1L
            val activity = com.autentia.tnt.binnacle.entities.Activity(
                id,
                mockNow,
                mockNow.plusDays(30),
                MINUTES_IN_HOUR,
                "description",
                    projectRoleWithNonBlockedProject,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            Mockito.doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(vacationProject.id)
            ClockTestUtils.runWithFixed(mockNow) {
                subcontractedActivityValidator.checkActivityIsValidForDeletion(activity.toDomain())
            }
        }

        @Test
        fun `do nothing when activity started last year`() {
            val id = 1L
            val activity = com.autentia.tnt.binnacle.entities.Activity(
                id,
                mockNow.minusYears(1),
                mockNow.minusYears(1).plusDays(30),
                MINUTES_IN_HOUR,
                "description",
                    projectRoleWithNonBlockedProject,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            Mockito.doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(vacationProject.id)
            ClockTestUtils.runWithFixed(mockNow) {
                subcontractedActivityValidator.checkActivityIsValidForDeletion(activity.toDomain())
            }
        }

        @Test
        fun `throw ActivityPeriodClosedException when activity started more than one year ago`() {
            val id = 1L
            val activity = com.autentia.tnt.binnacle.entities.Activity(
                id,
                mockNow.minusYears(2),
                mockNow.minusYears(2).plusMonths(1),
                MINUTES_IN_HOUR,
                "description",
                projectRoleWithNonBlockedProject,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            Mockito.doReturn(Optional.of(vacationProject))
                .whenever(projectRepository)
                .findById(vacationProject.id)

            assertThrows<ActivityPeriodClosedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForDeletion(activity.toDomain())
                }
            }
        }

        @Test
        fun `throw ProjectBlockedException when project is blocked`() {
            val id = 1L
            val activity = com.autentia.tnt.binnacle.entities.Activity(
                id,
                mockNow.minusYears(2),
                mockNow.minusYears(2).plusMonths(1),
                MINUTES_IN_HOUR,
                "description",
                projectRoleWithBlockedProject,
                user.id,
                false,
                approvalState = ApprovalState.NA
            )

            Mockito.doReturn(Optional.of(blockedProject))
                .whenever(projectRepository)
                .findById(blockedProject.id)

            val exception = assertThrows<ProjectBlockedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForDeletion(activity.toDomain())
                }
            }

            Assertions.assertEquals(blockedProject.blockDate!!, exception.blockedDate)
        }


        @Test
        fun `do not allow deletion of a subcontracted activity when project is closed`() {
            val id = 1L
            val activity = com.autentia.tnt.binnacle.entities.Activity(
                id,
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0),
                LocalDateTime.of(2022, Month.MARCH, 25, 10, 0, 0).plusMinutes(MINUTES_IN_HOUR.toLong()),
                MINUTES_IN_HOUR,
                "description",
                closedProjectRole,
                user.id,
                false,
                approvalState = ApprovalState.ACCEPTED
            )

            Mockito.doReturn(Optional.of(closedProject))
                .whenever(projectRepository)
                .findById(closedProject.id)

            assertThrows<ProjectClosedException> {
                ClockTestUtils.runWithFixed(mockNow) {
                    subcontractedActivityValidator.checkActivityIsValidForDeletion(activity.toDomain())
                }
            }
        }
    }

    private companion object {

        private val user = createDomainUser()
        private val today = mockToday

        private const val MINUTES_IN_HOUR = 18000
        private const val CLOSED_ID = 2L


        private val nonBlockedProject = Project(
            1,
            "NonBlockedProject",
            true,
            true,
            mockNow.toLocalDate(),
            null,
            null,
            Organization(1, "Organization", 1, emptyList()),
            emptyList()
        )

        private val nonBlockedProjectTwoYearsAgo = Project(
                3,
                "nonBlockedProjectTwoYearsAgo",
                true,
                true,
                mockNow.toLocalDate().minusYears(2),
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
            mockNow.toLocalDate().minusYears(1),
            mockNow.toLocalDate().minusMonths(2),
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
                mockNow.toLocalDate().minusYears(1),
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

        private val projectRoleWithNonBlockedProjectTwoYearsAgo = ProjectRole(
                1,
                "blocked",
                RequireEvidence.NO,
                nonBlockedProjectTwoYearsAgo,
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
                mockNow.toLocalDate(),
                mockNow.toLocalDate().minusDays(1),
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


        private val newActivity = Activity.of(
            null,
            TimeInterval.of(
                    mockNow,
                    mockNow.plusDays(30),
            ),
            MINUTES_IN_HOUR,
            "description",
            projectRoleWithNonBlockedProject.toDomain(),
            1L,
            false,
            null,
            null,
            false,
            ApprovalState.NA,
            null
        )

        private val newActivityWithNegativeDuration = createDomainActivity(
                mockNow.plusMonths(2),
                mockNow.plusMonths(3),
                -800,
                projectRoleWithNonBlockedProject.toDomain()
        ).copy(id = null)

        private val newActivityInClosedProject = createDomainActivity(
            mockNow.plusMonths(2),
            mockNow.plusMonths(3),
            MINUTES_IN_HOUR,
            closedProjectRole.toDomain()
        ).copy(id = null)

        private val newActivityLastYear = createDomainActivity(
            mockNow.minusYears(1),
            mockNow.minusYears(1).plusDays(30),
            MINUTES_IN_HOUR,
            projectRoleWithNonBlockedProject.toDomain()
        ).copy(id = null)

        private val activityUpdateNonexistentID = createDomainActivity(
            mockNow.minusYears(2),
            mockNow.minusYears(2).plusDays(30),
            MINUTES_IN_HOUR,
                projectRoleWithNonBlockedProject.toDomain()
        ).copy(id = null)

        private val newActivityTwoYearsAgo = createDomainActivity(
            mockNow.minusYears(2),
            mockNow.minusYears(2).plusDays(30),
            MINUTES_IN_HOUR,
            projectRoleWithNonBlockedProjectTwoYearsAgo.toDomain()
        ).copy(id = null)

        private val activityLastYear = createDomainActivity(
            mockNow.minusYears(1),
            mockNow.minusYears(1).plusDays(30),
            MINUTES_IN_HOUR,
                projectRoleWithNonBlockedProject.toDomain()
        )

        private val newActivityBeforeBlockedProject = createDomainActivity(
            mockNow.minusYears(1),
            mockNow.minusYears(1).plusDays(30),
            MINUTES_IN_HOUR,
            blockedProjectRole.toDomain()
        ).copy(id = null)

        private val newActivityAfterBlockedProject = createDomainActivity(
            mockNow,
            mockNow.plusDays(30),
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
            mockNow.minusYears(2),
            mockNow.minusYears(2).plusDays(30),
            MINUTES_IN_HOUR,
            projectRoleWithNonBlockedProjectTwoYearsAgo.toDomain()
        )

        private val validActivityToUpdate = createDomainActivity(
            mockNow,
            mockNow.plusDays(30),
            MINUTES_IN_HOUR,
            projectRoleWithNonBlockedProject.toDomain()
        )

        private val newActivityBeforeProjectCreationDate = createDomainActivity(
            mockNow.minusMonths(1),
            mockNow.minusMonths(1).plusDays(15),
            MINUTES_IN_HOUR,
            projectRoleWithNonBlockedProject.toDomain()
        ).copy(id = null)

    }
}