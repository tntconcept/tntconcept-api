package com.autentia.tnt.binnacle.repositories

import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.*

class ProjectRoleRepositorySecuredTest {
    private val projectRoleDao = mock<ProjectRoleDao>()
    private val securityService = mock<SecurityService>()
    private val projectRoleRepositorySecured = ProjectRoleRepositorySecured(projectRoleDao, securityService)

    @Test
    fun `find by id`() {
        projectRoleRepositorySecured.findById(projectRoleId)

        verify(projectRoleDao).findById(projectRoleId)
    }

    @Test
    fun `find between dates`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            projectRoleRepositorySecured.findDistinctRolesBetweenDate(startDate, endDate)
        }
    }

    @Test
    fun `find between dates when authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticatedUser))

        projectRoleRepositorySecured.findDistinctRolesBetweenDate(startDate, endDate)

        verify(projectRoleDao).findDistinctRolesBetweenDate(startDate, endDate, userId)
    }

    @Test
    fun `get all by project id`() {
        projectRoleRepositorySecured.getAllByProjectId(projectId)

        verify(projectRoleDao).getAllByProjectId(projectId)
    }

    @Test
    fun `get all by project id in`() {
        val projectIdList = listOf(projectId)
        projectRoleRepositorySecured.getAllByProjectIdIn(projectIdList)

        verify(projectRoleDao).getAllByProjectIdIn(projectIdList)
    }

    @Test
    fun `get all by id in`() {
        val projectIdList = listOf(projectRoleId)
        projectRoleRepositorySecured.getAllByIdIn(projectIdList)

        verify(projectRoleDao).getAllByIdIn(projectIdList)

    }

    private companion object {
        private const val projectId = 1L
        private const val projectRoleId = 1L
        private val startDate = LocalDateTime.of(2023, 4, 5, 0, 0)
        private val endDate = LocalDateTime.of(2023, 4, 7, 0, 0)
        private const val userId = 1L
        private val authenticatedUser =
            ClientAuthentication(
                userId.toString(),
                mapOf("roles" to listOf("user"))
            )
    }
}