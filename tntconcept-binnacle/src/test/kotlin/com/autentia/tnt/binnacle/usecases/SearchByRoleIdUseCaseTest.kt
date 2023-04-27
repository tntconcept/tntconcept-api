package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.OrganizationResponseConverter
import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.converters.SearchConverter
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ProjectRoleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

internal class SearchByRoleIdUseCaseTest {

    private val projectRoleService = mock<ProjectRoleService>()
    private val activityService = mock<ActivityService>()
    private val projectResponseConverter = ProjectResponseConverter()
    private val organizationResponseConverter = OrganizationResponseConverter()
    private val projectRoleResponseConverter = ProjectRoleResponseConverter(activityService)
    private val searchConverter =
        SearchConverter(
            projectResponseConverter,
            organizationResponseConverter,
            projectRoleResponseConverter
        )
    private val searchByRoleIdUseCase = SearchByRoleIdUseCase(projectRoleService, searchConverter)

    @Test
    fun `return empty list when roleid is not found`() {

        doReturn(emptyList<ProjectRole>())
            .whenever(projectRoleService).getAllByIds(listOf(UNKONW_ROLE_ID.toInt()))

        val roles = searchByRoleIdUseCase.getDescriptions(listOf(UNKONW_ROLE_ID))

        assertEquals(0, roles.organizations.size)
        assertEquals(0, roles.projects.size)
        assertEquals(0, roles.projectRoles.size)
    }

    @Test
    fun `return an unique element for Organization, Project and Role when search only for one projectRole`() {
        val rolesForSearch = listOf(INTERNAL_STUDENT.id)
        doReturn(listOf(INTERNAL_STUDENT)).whenever(projectRoleService).getAllByIds(rolesForSearch.map(Long::toInt))

        val roles = searchByRoleIdUseCase.getDescriptions(rolesForSearch)

        assertEquals(1, roles.organizations.size)
        assertEquals(1, roles.projects.size)
        assertEquals(1, roles.projectRoles.size)

        assertEquals(organizationResponseConverter.toOrganizationResponseDTO(AUTENTIA), roles.organizations[0])
        assertEquals(projectResponseConverter.toProjectResponseDTO(INTERNAL_TRAINING), roles.projects[0])
        assertEquals(projectRoleResponseConverter.toProjectRoleDTO(INTERNAL_STUDENT), roles.projectRoles[0])
    }

    @Test
    fun `return unique elements for shared project and companies`() {
        val rolesForSearch = listOf(
            INTERNAL_STUDENT.id,
            INTERNAL_TEACHER.id,
            EXTERNAL_STUDENT.id,
            EXTERNAL_TEACHER.id
        )
        doReturn(
            listOf(
                INTERNAL_STUDENT,
                INTERNAL_TEACHER,
                EXTERNAL_STUDENT,
                EXTERNAL_TEACHER
            )
        ).whenever(projectRoleService).getAllByIds(rolesForSearch.map(Long::toInt))

        val roles = searchByRoleIdUseCase.getDescriptions(rolesForSearch)

        assertEquals(2, roles.organizations.size)
        assertEquals(2, roles.projects.size)
        assertEquals(4, roles.projectRoles.size)

        assertNotNull(roles.organizations.find { it == organizationResponseConverter.toOrganizationResponseDTO(AUTENTIA) })
        assertNotNull(roles.organizations.find {
            it == organizationResponseConverter.toOrganizationResponseDTO(
                OTHER_COMPANY
            )
        })
        assertNotNull(roles.projects.find { it == projectResponseConverter.toProjectResponseDTO(INTERNAL_TRAINING) })
        assertNotNull(roles.projects.find { it == projectResponseConverter.toProjectResponseDTO(EXTERNAL_TRAINING) })
    }

    private companion object {
        private const val UNKONW_ROLE_ID = -1L

        private val AUTENTIA = Organization(1, "Autentia", emptyList())
        private val INTERNAL_TRAINING = Project(1, "Internal training", true, true, AUTENTIA, emptyList())
        private val INTERNAL_STUDENT =
            ProjectRole(1, "Student", RequireEvidence.WEEKLY, INTERNAL_TRAINING, 0, true, false, TimeUnit.MINUTES)
        private val INTERNAL_TEACHER =
            ProjectRole(2, "Internal Teacher", RequireEvidence.WEEKLY, INTERNAL_TRAINING, 0, true, false, TimeUnit.MINUTES)

        private val OTHER_COMPANY = Organization(2, "Other S.A.", emptyList())
        private val EXTERNAL_TRAINING = Project(2, "External training", true, true, OTHER_COMPANY, emptyList())
        private val EXTERNAL_STUDENT =
            ProjectRole(3, "External student", RequireEvidence.WEEKLY, EXTERNAL_TRAINING, 0, true, false, TimeUnit.MINUTES)
        private val EXTERNAL_TEACHER =
            ProjectRole(4, "External teacher", RequireEvidence.WEEKLY, EXTERNAL_TRAINING, 0, true, false, TimeUnit.MINUTES)
    }
}
