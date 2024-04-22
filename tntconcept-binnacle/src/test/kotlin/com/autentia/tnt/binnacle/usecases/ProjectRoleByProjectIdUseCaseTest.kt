package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.converters.ProjectRoleConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.MaxTimeAllowedDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.entities.dto.TimeInfoDTO
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ProjectRoleByProjectIdUseCaseTest {

    private val activityRepository: ActivityRepository = mock()
    private val holidayRepository = mock<HolidayRepository>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val calendarFactory = CalendarFactory(holidayRepository)
    private val securityService = mock<SecurityService>()
    private val activityCalendarFactory = ActivitiesCalendarFactory(calendarFactory)
    private val activityCalendarService = ActivityCalendarService(calendarFactory, activityCalendarFactory)
    private val projectRoleResponseConverter = ProjectRoleResponseConverter()
    private val projectRoleConverter = ProjectRoleConverter()
    private val projectRoleByProjectIdUseCase =
        ProjectRoleByProjectIdUseCase(
            activityCalendarService,
            securityService,
            projectRoleRepository,
            activityRepository,
            projectRoleResponseConverter,
            projectRoleConverter
        )

    private fun userInputs() = arrayOf(
        arrayOf(null, USER_LOGGED_ID),
        arrayOf(OTHER_USER_ID, OTHER_USER_ID)
    )

    @ParameterizedTest
    @MethodSource("userInputs")
    fun `return the expected project role for authenticated user`(
        requestedUserId: Long?,
        projectRoleUserId: Long
    ) {
        val projectRoles = listOf(
            ProjectRole(
                id = 1L,
                name = "Role ID 1",
                project = PROJECT,
                maxTimeAllowedByYear = 120,
                maxTimeAllowedByActivity = 0,
                timeUnit = TimeUnit.MINUTES,
                requireEvidence = RequireEvidence.WEEKLY,
                isApprovalRequired = false,
                isWorkingTime = true
            ),
            ProjectRole(
                id = 2L,
                name = "Role ID 2",
                project = PROJECT,
                maxTimeAllowedByYear = 90,
                maxTimeAllowedByActivity = 0,
                timeUnit = TimeUnit.MINUTES,
                requireEvidence = RequireEvidence.WEEKLY,
                isApprovalRequired = false,
                isWorkingTime = true
            ),
            ProjectRole(
                id = 3L,
                name = "Role ID 3",
                project = PROJECT,
                maxTimeAllowedByYear = 0,
                maxTimeAllowedByActivity = 0,
                timeUnit = TimeUnit.MINUTES,
                requireEvidence = RequireEvidence.WEEKLY,
                isApprovalRequired = false,
                isWorkingTime = true
            ),
            ProjectRole(
                id = 4L,
                name = "Role ID 4",
                project = PROJECT,
                maxTimeAllowedByYear = 1920,
                maxTimeAllowedByActivity = 0,
                timeUnit = TimeUnit.NATURAL_DAYS,
                requireEvidence = RequireEvidence.WEEKLY,
                isApprovalRequired = false,
                isWorkingTime = true
            ),
            ProjectRole(
                id = 5L,
                name = "Role ID 5",
                project = PROJECT,
                maxTimeAllowedByYear = 2400,
                maxTimeAllowedByActivity = 0,
                timeUnit = TimeUnit.DAYS,
                requireEvidence = RequireEvidence.WEEKLY,
                isApprovalRequired = false,
                isWorkingTime = true
            ),
        )
        val activity = createActivity(projectRoles[0]).copy(userId = projectRoleUserId)
        val otherActivity = createActivity(projectRoles[1]).copy(userId = projectRoleUserId)
        val year = activity.getTimeInterval().getYearOfStart()

        val activitiesProjectRole1 = listOf(
            activity,
            activity.copy(
                start = activity.end,
                end = activity.end.plusMinutes(10),
                duration = 10,
                userId = projectRoleUserId
            ),
            activity.copy(
                start = activity.end.minusYears(1L),
                end = activity.end.plusMinutes(10).minusYears(1L),
                duration = 10,
                userId = projectRoleUserId
            )
        )
        val activitiesProjectRole2 = listOf(
            otherActivity
        )

        val activitiesProjectRole4 = listOf(
            activity.copy(
                start = LocalDate.of(2023, 12, 30).atTime(LocalTime.MIN),
                end = LocalDate.of(2024, 1, 1).atTime(LocalTime.MAX),
                duration = 1920,
                projectRole = projectRoles[3],
                userId = projectRoleUserId
            )
        )

        val activitiesProjectRole5 = listOf(
            activity.copy(
                start = LocalDate.of(2023, 12, 29).atTime(LocalTime.MIN),
                end = LocalDate.of(2024, 1, 2).atTime(LocalTime.MAX),
                duration = 1440,
                projectRole = projectRoles[4],
                userId = projectRoleUserId
            )
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authentication))

        doReturn(projectRoles)
            .whenever(projectRoleRepository)
            .getAllByProjectId(PROJECT_ID)

        doReturn(activitiesProjectRole1)
            .whenever(activityRepository)
            .findByProjectRoleIdAndUserId(1L, projectRoleUserId)

        doReturn(activitiesProjectRole2)
            .whenever(activityRepository)
            .findByProjectRoleIdAndUserId(2L, projectRoleUserId)

        doReturn(emptyList<Activity>())
            .whenever(activityRepository)
            .findByProjectRoleIdAndUserId(3L, projectRoleUserId)

        doReturn(activitiesProjectRole4)
            .whenever(activityRepository)
            .findByProjectRoleIdAndUserId(4L, projectRoleUserId)

        doReturn(activitiesProjectRole5)
            .whenever(activityRepository)
            .findByProjectRoleIdAndUserId(5L, projectRoleUserId)


        val expectedProjectRoles = listOf(
            buildProjectRoleUserDTO(1L, 120, 50, TimeUnit.MINUTES, projectRoleUserId),
            buildProjectRoleUserDTO(2L, 90, 30, TimeUnit.MINUTES, projectRoleUserId),
            buildProjectRoleUserDTO(3L, 0, 0, TimeUnit.MINUTES, projectRoleUserId),
            buildProjectRoleUserDTO(4L, 4, 1, TimeUnit.NATURAL_DAYS, projectRoleUserId),
            buildProjectRoleUserDTO(5L, 5, 2, TimeUnit.DAYS, projectRoleUserId),
        )

        assertEquals(expectedProjectRoles, projectRoleByProjectIdUseCase.get(PROJECT_ID, year, requestedUserId))
    }

    private fun buildProjectRoleUserDTO(
        id: Long,
        maxAllowed: Int = 0,
        remaining: Int = 0,
        timeUnit: TimeUnit,
        userId: Long,
    ): ProjectRoleUserDTO =
        ProjectRoleUserDTO(
            id,
            "Role ID $id",
            1L,
            1L,
            RequireEvidence.WEEKLY,
            false,
            userId,
            TimeInfoDTO(MaxTimeAllowedDTO(maxAllowed, 0), timeUnit, remaining)
        )

    private companion object {
        private const val USER_LOGGED_ID = 1L
        private const val OTHER_USER_ID = 2L
        private const val PROJECT_ID = 1L

        private val ORGANIZATION = Organization(1L, "Nuestra empresa", 1, listOf())
        private val PROJECT =
            Project(1L, "Dummy project", true, false, LocalDate.now(), null, null, ORGANIZATION, listOf(),"NO_BILLABLE")

        private val authentication =
            ClientAuthentication(USER_LOGGED_ID.toString(), mapOf("roles" to listOf("admin")))
    }
}
