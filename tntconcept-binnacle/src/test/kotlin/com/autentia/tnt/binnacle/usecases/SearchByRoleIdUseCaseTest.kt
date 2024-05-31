package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.*
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.security.application.id
import io.archimedesfw.commons.time.test.ClockTestUtils
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.Month
import java.util.*


val mockNow = LocalDateTime.of(2023, Month.MARCH, 1, 0, 0, 0)

internal class SearchByRoleIdUseCaseTest {

    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityRepository = mock<ActivityRepository>()
    private val holidayRepository = mock<HolidayRepository>()
    private val calendarFactory = CalendarFactory(holidayRepository)
    private val activitiesCalendarFactory = ActivitiesCalendarFactory(calendarFactory)
    private val activityCalendarService = ActivityCalendarService(calendarFactory, activitiesCalendarFactory)
    private val projectResponseConverter = ProjectResponseConverter()
    private val organizationResponseConverter = OrganizationResponseConverter()
    private val projectRoleResponseConverter = ProjectRoleResponseConverter()
    private val projectRoleConverter = ProjectRoleConverter()
    private val searchConverter =
        SearchConverter(
            projectResponseConverter,
            organizationResponseConverter,
            projectRoleResponseConverter
        )
    private val securityService = mock<SecurityService>()
    private val searchByRoleIdUseCase = SearchByRoleIdUseCase(
        projectRoleRepository,
        activityRepository,
        securityService,
        activityCalendarService,
        projectRoleConverter,
        searchConverter
    )

    @Test
    fun `return empty list when roleId is not found for current year`() {

        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))

        doReturn(emptyList<ProjectRole>())
            .whenever(projectRoleRepository).getAllByIdIn(listOf(UNKONW_ROLE_ID))

        val roles =
            ClockTestUtils.runWithFixed(mockNow) {
                searchByRoleIdUseCase.get(listOf(UNKONW_ROLE_ID), null)
            }

        assertEquals(0, roles.organizations.size)
        assertEquals(0, roles.projects.size)
        assertEquals(0, roles.projectRoles.size)
    }

    @Test
    fun `return an unique element for Organization, Project and Role when search only for one projectRole and current year`() {
        val rolesForSearch = listOf(INTERNAL_STUDENT.id)
        val activity = createActivity().copy(projectRole = INTERNAL_STUDENT)

        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
        whenever(projectRoleRepository.getAllByIdIn(rolesForSearch)).thenReturn(listOf(INTERNAL_STUDENT))
        whenever(
            activityRepository.findByProjectRoleIds(
                TimeInterval.ofYear(mockNow.year).start,
                TimeInterval.ofYear(mockNow.year).end,
                rolesForSearch,
                authenticatedUser.id()
            )
        ).thenReturn(listOf(activity))
        val roles =
            ClockTestUtils.runWithFixed(mockNow) {
                searchByRoleIdUseCase.get(rolesForSearch, null)
            }

        assertEquals(1, roles.organizations.size)
        assertEquals(1, roles.projects.size)
        assertEquals(1, roles.projectRoles.size)

        assertEquals(organizationResponseConverter.toOrganizationResponseDTO(AUTENTIA), roles.organizations[0])
        assertEquals(projectResponseConverter.toProjectResponseDTO(INTERNAL_TRAINING), roles.projects[0])
        assertEquals(
            projectRoleResponseConverter.toProjectRoleUserDTO(
                projectRoleConverter.toProjectRoleUser(INTERNAL_STUDENT.toDomain(), 1380, 1L)
            ), roles.projectRoles[0]
        )
    }


    @Test
    fun `return an unique element for Organization, Project and Role when search only for one projectRole filtering by year`() {

        val rolesForSearch = listOf(INTERNAL_STUDENT.id)
        val activity = createActivity().copy(projectRole = INTERNAL_STUDENT)

        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
        whenever(projectRoleRepository.getAllByIdIn(rolesForSearch)).thenReturn(listOf(INTERNAL_STUDENT))
        whenever(
            activityRepository.findByProjectRoleIds(
                TimeInterval.ofYear(2023).start,
                TimeInterval.ofYear(2023).end,
                rolesForSearch,
                authenticatedUser.id()
            )
        ).thenReturn(listOf(activity))

        val roles =
            ClockTestUtils.runWithFixed(mockNow) {
                searchByRoleIdUseCase.get(rolesForSearch, 2023)
            }

        assertEquals(1, roles.organizations.size)
        assertEquals(1, roles.projects.size)
        assertEquals(1, roles.projectRoles.size)

        assertEquals(organizationResponseConverter.toOrganizationResponseDTO(AUTENTIA), roles.organizations[0])
        assertEquals(projectResponseConverter.toProjectResponseDTO(INTERNAL_TRAINING), roles.projects[0])
        assertEquals(
            projectRoleResponseConverter.toProjectRoleUserDTO(
                projectRoleConverter.toProjectRoleUser(INTERNAL_STUDENT.toDomain(), 1380, 1L)
            ), roles.projectRoles[0]
        )
    }

    @Test
    fun `return unique elements for shared project and companies`() {

        val activity = createActivity()
        val internalStudentActivity = activity.copy(projectRole = INTERNAL_STUDENT)
        val internalTeacherActivity = activity.copy(projectRole = INTERNAL_TEACHER)
        val rolesForSearch = listOf(
            INTERNAL_STUDENT.id,
            INTERNAL_TEACHER.id,
            EXTERNAL_STUDENT.id,
            EXTERNAL_TEACHER.id
        )
        val rolesToReturn = listOf(
            INTERNAL_STUDENT,
            INTERNAL_TEACHER,
            EXTERNAL_STUDENT,
            EXTERNAL_TEACHER
        )
        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
        whenever(projectRoleRepository.getAllByIdIn(rolesForSearch)).thenReturn(rolesToReturn)
        whenever(
            activityRepository.findByProjectRoleIds(
                TimeInterval.ofYear(mockNow.year).start,
                TimeInterval.ofYear(mockNow.year).end,
                rolesForSearch,
                authenticatedUser.id()
            )
        ).thenReturn(listOf(internalStudentActivity, internalTeacherActivity))
        val roles =
            ClockTestUtils.runWithFixed(mockNow) {
                searchByRoleIdUseCase.get(rolesForSearch, null)
            }

        assertEquals(2, roles.organizations.size)
        assertEquals(2, roles.projects.size)
        assertEquals(4, roles.projectRoles.size)


        assertNotNull(roles.organizations.find { it == organizationResponseConverter.toOrganizationResponseDTO(AUTENTIA) })
        assertNotNull(roles.organizations.find {
            it == organizationResponseConverter.toOrganizationResponseDTO(OTHER_COMPANY)
        })
        assertNotNull(roles.projects.find { it == projectResponseConverter.toProjectResponseDTO(INTERNAL_TRAINING) })
        assertNotNull(roles.projects.find { it == projectResponseConverter.toProjectResponseDTO(EXTERNAL_TRAINING) })
        assertNotNull(roles.projectRoles.find {
            it == projectRoleResponseConverter.toProjectRoleUserDTO(
                projectRoleConverter.toProjectRoleUser(INTERNAL_STUDENT.toDomain(), 1380, 1L)
            )
        })
        assertNotNull(roles.projectRoles.find {
            it == projectRoleResponseConverter.toProjectRoleUserDTO(
                projectRoleConverter.toProjectRoleUser(INTERNAL_TEACHER.toDomain(), 2820, 1L)
            )
        })
        assertNotNull(roles.projectRoles.find {
            it == projectRoleResponseConverter.toProjectRoleUserDTO(
                projectRoleConverter.toProjectRoleUser(EXTERNAL_STUDENT.toDomain(), 0, 1L)
            )
        })
        assertNotNull(roles.projectRoles.find {
            it == projectRoleResponseConverter.toProjectRoleUserDTO(
                projectRoleConverter.toProjectRoleUser(EXTERNAL_TEACHER.toDomain(), 0, 1L)
            )
        })
    }

    @Nested
    inner class SearchRemainingCalculation {

        @Test
        fun `is correct when exist activities with change of year`() {

            val expectedRemainingDays = 1

            val activities = listOf(
                Activity.of(
                    createDomainActivity(
                        start = LocalDateTime.of(2023, 1, 15, 0, 0, 0),
                        end = LocalDateTime.of(2023, 1, 15, 23, 59, 59),
                        duration = 960,
                        projectRole = projectRoleLimitedInDays.toDomain()
                    ), projectRoleLimitedInDays
                ),
                Activity.of(
                    createDomainActivity(
                        start = LocalDateTime.of(2023, 12, 31, 0, 0, 0),
                        end = LocalDateTime.of(2024, 1, 1, 23, 59, 59),
                        duration = 960,
                        projectRole = projectRoleLimitedInDays.toDomain()
                    ), projectRoleLimitedInDays
                )
            )

            val projectRoleList = listOf(projectRoleLimitedInDays)
            val timeInterval = TimeInterval.ofYear(mockNow.year)
            val roleIds = listOf(1L)

            whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))

            doReturn(projectRoleList)
                .whenever(projectRoleRepository)
                .getAllByIdIn(
                    roleIds
                )

            doReturn(activities)
                .whenever(activityRepository)
                .findByProjectRoleIds(
                    timeInterval.start,
                    timeInterval.end,
                    roleIds,
                    createUser().id
                )

            val obtainedRoleWithoutYearParameter =
                ClockTestUtils.runWithFixed(mockNow) {
                    searchByRoleIdUseCase.get(roleIds, null).projectRoles.get(0)
                }
            val obtainedRoleWithYearParameter =
                ClockTestUtils.runWithFixed(mockNow) {
                    searchByRoleIdUseCase.get(roleIds, 2023).projectRoles.get(0)
                }

            assertEquals(expectedRemainingDays, obtainedRoleWithoutYearParameter.timeInfo.userRemainingTime)
            assertEquals(expectedRemainingDays, obtainedRoleWithYearParameter.timeInfo.userRemainingTime)
        }

        @Test
        fun `is correct when exist activities in the same year`() {

            val expectedRemainingTime = 135

            val activities = listOf(
                Activity.of(
                    createDomainActivity(
                        start = LocalDateTime.of(2023, 1, 15, 9, 0, 0),
                        end = LocalDateTime.of(2023, 1, 15, 9, 45, 0),
                        duration = 45,
                        projectRole = projectRoleLimited.toDomain()
                    ), projectRoleLimited
                )
            )

            val projectRoleList = listOf(projectRoleLimited)
            val timeInterval = TimeInterval.ofYear(mockNow.year)
            val roleIds = listOf(1L)

            whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))

            doReturn(projectRoleList)
                .whenever(projectRoleRepository)
                .getAllByIdIn(
                    roleIds
                )

            doReturn(activities)
                .whenever(activityRepository)
                .findByProjectRoleIds(
                    timeInterval.start,
                    timeInterval.end,
                    roleIds,
                    createUser().id
                )

            val obtainedRoleWithoutYearParameter =
                ClockTestUtils.runWithFixed(mockNow) {
                    searchByRoleIdUseCase.get(roleIds, null).projectRoles.get(0)
                }

            val obtainedRoleWithYearParameter =
                ClockTestUtils.runWithFixed(mockNow) {
                    searchByRoleIdUseCase.get(roleIds, 2023).projectRoles.get(0)
                }

            assertEquals(expectedRemainingTime, obtainedRoleWithoutYearParameter.timeInfo.userRemainingTime)
            assertEquals(expectedRemainingTime, obtainedRoleWithYearParameter.timeInfo.userRemainingTime)
        }
    }


    private companion object {
        private val UNKONW_ROLE_ID = -1L

        private val AUTENTIA = com.autentia.tnt.binnacle.entities.Organization(1L, "Autentia", 1, listOf())
        private val INTERNAL_TRAINING =
            com.autentia.tnt.binnacle.entities.Project(
                1,
                "Internal training",
                true,
                mockNow.toLocalDate(),
                null,
                null,
                AUTENTIA,
                listOf(),
                "CLOSED_PRICE"
            )
        private val INTERNAL_STUDENT =
            ProjectRole(
                1,
                "Student",
                RequireEvidence.WEEKLY,
                INTERNAL_TRAINING,
                1440,
                0,
                false,
                true,
                TimeUnit.MINUTES
            )
        private val INTERNAL_TEACHER =
            ProjectRole(
                2,
                "Internal Teacher",
                RequireEvidence.WEEKLY,
                INTERNAL_TRAINING,
                2880,
                0,
                false,
                true,
                TimeUnit.MINUTES
            )

        private val OTHER_COMPANY = com.autentia.tnt.binnacle.entities.Organization(2L, "Other S.A.", 1, listOf())
        private val EXTERNAL_TRAINING =
            com.autentia.tnt.binnacle.entities.Project(
                2,
                "External training",
                true,
                mockNow.toLocalDate(),
                null,
                null,
                OTHER_COMPANY,
                listOf(),
                "CLOSED_PRICE"
            )
        private val EXTERNAL_STUDENT =
            ProjectRole(
                3,
                "External student",
                RequireEvidence.WEEKLY,
                EXTERNAL_TRAINING,
                0,
                0,
                false,
                true,
                TimeUnit.MINUTES
            )
        private val EXTERNAL_TEACHER =
            ProjectRole(
                4,
                "External teacher",
                RequireEvidence.WEEKLY,
                EXTERNAL_TRAINING,
                0,
                0,
                false,
                true,
                TimeUnit.MINUTES
            )

        private val projectRoleLimitedInDays =
            ProjectRole(
                1L,
                "My Project role",
                RequireEvidence.NO,
                INTERNAL_TRAINING,
                1920,
                0,
                true,
                false,
                TimeUnit.NATURAL_DAYS
            )

        private val projectRoleLimited =
            ProjectRole(
                1L,
                "My Project role",
                RequireEvidence.NO,
                INTERNAL_TRAINING,
                180,
                0,
                true,
                false,
                TimeUnit.MINUTES
            )

        private val authenticatedUser =
            ClientAuthentication(
                "1",
                mapOf("roles" to listOf("user"))
            )
    }
}
