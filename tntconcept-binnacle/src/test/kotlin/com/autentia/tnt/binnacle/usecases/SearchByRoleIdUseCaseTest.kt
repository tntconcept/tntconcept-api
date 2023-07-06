package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.converters.*
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.security.application.id
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.*

internal class SearchByRoleIdUseCaseTest {

    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityRepository = mock<ActivityRepository>()
    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
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
    fun `return empty list when roleid is not found for current year`() {

        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))

        doReturn(emptyList<com.autentia.tnt.binnacle.entities.ProjectRole>())
            .whenever(projectRoleRepository).getAllByIdIn(listOf(UNKONW_ROLE_ID))

        val roles = searchByRoleIdUseCase.get(listOf(UNKONW_ROLE_ID), null)

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
                TimeInterval.ofYear( LocalDate.now().year).start,
                TimeInterval.ofYear( LocalDate.now().year).end,
                rolesForSearch,
                authenticatedUser.id()
            )
        ).thenReturn(listOf(activity))
        val roles = searchByRoleIdUseCase.get(rolesForSearch, null)

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
        val roles = searchByRoleIdUseCase.get(rolesForSearch, 2023)

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
                TimeInterval.ofYear(LocalDate.now().year).start,
                TimeInterval.ofYear(LocalDate.now().year).end,
                rolesForSearch,
                authenticatedUser.id()
            )
        ).thenReturn(listOf(internalStudentActivity, internalTeacherActivity))
        val roles = searchByRoleIdUseCase.get(rolesForSearch, null)

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

    private companion object {
        private val UNKONW_ROLE_ID = -1L

        private val AUTENTIA = com.autentia.tnt.binnacle.entities.Organization(1L, "Autentia", listOf())
        private val INTERNAL_TRAINING =
            com.autentia.tnt.binnacle.entities.Project(1, "Internal training", true, true, LocalDate.now(), null, null, AUTENTIA, listOf())
        private val INTERNAL_STUDENT =
            com.autentia.tnt.binnacle.entities.ProjectRole(1, "Student", RequireEvidence.WEEKLY, INTERNAL_TRAINING, 1440, false , true, TimeUnit.MINUTES)
        private val INTERNAL_TEACHER =
            com.autentia.tnt.binnacle.entities.ProjectRole(
                2,
                "Internal Teacher",
                RequireEvidence.WEEKLY,
                INTERNAL_TRAINING,
                2880,
                false,
                true,
                TimeUnit.MINUTES
            )

        private val OTHER_COMPANY = com.autentia.tnt.binnacle.entities.Organization(2L, "Other S.A.", listOf())
        private val EXTERNAL_TRAINING =
            com.autentia.tnt.binnacle.entities.Project(2, "External training", true, true, LocalDate.now(), null, null, OTHER_COMPANY, listOf())
        private val EXTERNAL_STUDENT =
            com.autentia.tnt.binnacle.entities.ProjectRole(
                3,
                "External student",
                RequireEvidence.WEEKLY,
                EXTERNAL_TRAINING,
                0,
                false,
                true,
                TimeUnit.MINUTES
            )
        private val EXTERNAL_TEACHER =
            com.autentia.tnt.binnacle.entities.ProjectRole(
                4,
                "External teacher",
                RequireEvidence.WEEKLY,
                EXTERNAL_TRAINING,
                0,
                false,
                true,
                TimeUnit.MINUTES
            )
        private val authenticatedUser =
            ClientAuthentication(
                "1",
                mapOf("roles" to listOf("user"))
            )
    }
}
