package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.converters.*
import com.autentia.tnt.binnacle.core.domain.*
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.binnacle.services.ProjectRoleService
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

    private val projectRoleService = mock<ProjectRoleService>()
    private val activityService = mock<ActivityService>()
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
        projectRoleService,
        activityService,
        securityService,
        activityCalendarService,
        projectRoleConverter,
        searchConverter
    )

    @Test
    fun `return empty list when roleid is not found for current year`() {

        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))

        doReturn(emptyList<ProjectRole>())
            .whenever(projectRoleService).getAllByIds(listOf(UNKONW_ROLE_ID))

        val roles = searchByRoleIdUseCase.getDescriptions(listOf(UNKONW_ROLE_ID), null)

        assertEquals(0, roles.organizations.size)
        assertEquals(0, roles.projects.size)
        assertEquals(0, roles.projectRoles.size)
    }

    @Test
    fun `return an unique element for Organization, Project and Role when search only for one projectRole and current year`() {
        val rolesForSearch = listOf(INTERNAL_STUDENT.id)
        val activity = createDomainActivity().copy(projectRole = INTERNAL_STUDENT)

        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
        whenever(projectRoleService.getAllByIds(rolesForSearch)).thenReturn(listOf(INTERNAL_STUDENT))
        whenever(
            activityService.getActivitiesByProjectRoleIds(
                TimeInterval.ofYear( LocalDate.now().year),
                rolesForSearch,
                authenticatedUser.id()
            )
        ).thenReturn(listOf(activity))
        val roles = searchByRoleIdUseCase.getDescriptions(rolesForSearch, null)

        assertEquals(1, roles.organizations.size)
        assertEquals(1, roles.projects.size)
        assertEquals(1, roles.projectRoles.size)

        assertEquals(organizationResponseConverter.toOrganizationResponseDTO(AUTENTIA), roles.organizations[0])
        assertEquals(projectResponseConverter.toProjectResponseDTO(INTERNAL_TRAINING), roles.projects[0])
        assertEquals(
            projectRoleResponseConverter.toProjectRoleUserDTO(
                projectRoleConverter.toProjectRoleUser(INTERNAL_STUDENT, 1380, 1L)
            ), roles.projectRoles[0]
        )
    }


    @Test
    fun `return an unique element for Organization, Project and Role when search only for one projectRole filtering by year`() {

        val rolesForSearch = listOf(INTERNAL_STUDENT.id)
        val activity = createDomainActivity().copy(projectRole = INTERNAL_STUDENT)

        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))
        whenever(projectRoleService.getAllByIds(rolesForSearch)).thenReturn(listOf(INTERNAL_STUDENT))
        whenever(
                activityService.getActivitiesByProjectRoleIds(
                        TimeInterval.ofYear(2023),
                        rolesForSearch,
                        authenticatedUser.id()
                )
        ).thenReturn(listOf(activity))
        val roles = searchByRoleIdUseCase.getDescriptions(rolesForSearch, 2023)

        assertEquals(1, roles.organizations.size)
        assertEquals(1, roles.projects.size)
        assertEquals(1, roles.projectRoles.size)

        assertEquals(organizationResponseConverter.toOrganizationResponseDTO(AUTENTIA), roles.organizations[0])
        assertEquals(projectResponseConverter.toProjectResponseDTO(INTERNAL_TRAINING), roles.projects[0])
        assertEquals(
                projectRoleResponseConverter.toProjectRoleUserDTO(
                        projectRoleConverter.toProjectRoleUser(INTERNAL_STUDENT, 1380, 1L)
                ), roles.projectRoles[0]
        )
    }

    @Test
    fun `return unique elements for shared project and companies`() {

        val activity = createDomainActivity()
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
        whenever(projectRoleService.getAllByIds(rolesForSearch)).thenReturn(rolesToReturn)
        whenever(
            activityService.getActivitiesByProjectRoleIds(
                TimeInterval.ofYear(LocalDate.now().year),
                rolesForSearch,
                authenticatedUser.id()
            )
        ).thenReturn(listOf(internalStudentActivity, internalTeacherActivity))
        val roles = searchByRoleIdUseCase.getDescriptions(rolesForSearch, null)

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
                projectRoleConverter.toProjectRoleUser(INTERNAL_STUDENT, 1380, 1L)
            )
        })
        assertNotNull(roles.projectRoles.find {
            it == projectRoleResponseConverter.toProjectRoleUserDTO(
                projectRoleConverter.toProjectRoleUser(INTERNAL_TEACHER, 2820, 1L)
            )
        })
        assertNotNull(roles.projectRoles.find {
            it == projectRoleResponseConverter.toProjectRoleUserDTO(
                projectRoleConverter.toProjectRoleUser(EXTERNAL_STUDENT, 0, 1L)
            )
        })
        assertNotNull(roles.projectRoles.find {
            it == projectRoleResponseConverter.toProjectRoleUserDTO(
                projectRoleConverter.toProjectRoleUser(EXTERNAL_TEACHER, 0, 1L)
            )
        })
    }

    private companion object {
        private val UNKONW_ROLE_ID = -1L

        private val AUTENTIA = Organization(1, "Autentia")
        private val INTERNAL_TRAINING = Project(1, "Internal training", true, true, AUTENTIA)
        private val INTERNAL_STUDENT =
            ProjectRole(1, "Student", RequireEvidence.WEEKLY, INTERNAL_TRAINING, 1440, TimeUnit.MINUTES, true, false)
        private val INTERNAL_TEACHER =
            ProjectRole(
                2,
                "Internal Teacher",
                RequireEvidence.WEEKLY,
                INTERNAL_TRAINING,
                2880,
                TimeUnit.MINUTES,
                true,
                false
            )

        private val OTHER_COMPANY = Organization(2, "Other S.A.")
        private val EXTERNAL_TRAINING = Project(2, "External training", true, true, OTHER_COMPANY)
        private val EXTERNAL_STUDENT =
            ProjectRole(
                3,
                "External student",
                RequireEvidence.WEEKLY,
                EXTERNAL_TRAINING,
                0,

                TimeUnit.MINUTES,
                true,
                false
            )
        private val EXTERNAL_TEACHER =
            ProjectRole(
                4,
                "External teacher",
                RequireEvidence.WEEKLY,
                EXTERNAL_TRAINING,
                0,
                TimeUnit.MINUTES,
                true,
                false
            )
        private val authenticatedUser =
            ClientAuthentication(
                "1",
                mapOf("roles" to listOf("user"))
            )
    }
}
