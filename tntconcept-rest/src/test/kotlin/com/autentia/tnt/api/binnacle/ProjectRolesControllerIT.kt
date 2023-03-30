package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.converters.ProjectRoleRecentConverter
import com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent
import com.autentia.tnt.binnacle.core.domain.ProjectRolesRecent
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleRecentDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.usecases.LatestProjectRolesForAuthenticatedUserUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByUserIdsUseCase
import io.micronaut.http.HttpRequest.GET
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.NOT_FOUND
import io.micronaut.http.HttpStatus.OK
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Deprecated("Use ProjectRoleControllerIT instead")
@MicronautTest
@TestInstance(PER_CLASS)
internal class ProjectRolesControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(ProjectRoleByIdUseCase::class)
    internal val projectRoleByIdUseCase = mock<ProjectRoleByIdUseCase>()

    @get:MockBean(LatestProjectRolesForAuthenticatedUserUseCase::class)
    internal val latestProjectRolesForAuthenticatedUserUseCase = mock<LatestProjectRolesForAuthenticatedUserUseCase>()

    @get:MockBean(ProjectRoleRecentConverter::class)
    internal val projectRoleRecentConverter = mock<ProjectRoleRecentConverter>()


    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get the project roles`() {

        val projectId = 1

        val projectRole = ProjectRoleDTO(
            1L,
            "Asistente",
            1L,
            1L,
            10,
            TimeUnit.MINUTES,
            RequireEvidence.WEEKLY,
            true
        )

        doReturn(projectRole).whenever(projectRoleByIdUseCase).get(projectRole.id)


        val response = client.exchangeObject<ProjectRoleDTO>(GET("/api/project-roles/$projectId"))

        assertEquals(OK, response.status)
        assertEquals(projectRole, response.body.get())

    }

    @ParameterizedTest
    @MethodSource("getFailProvider")
    fun `return 404 if the project role does not exist`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String
    ) {
        val projectId = 1L

        doThrow(exception).whenever(projectRoleByIdUseCase).get(projectId)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(GET("/api/project-roles/$projectId"))
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)

    }

    @Test
    fun `get the recent project roles`() {

        val projectRoleRecent = ProjectRolesRecent(
            1L,
            "desarrollador",
            "Project",
            "Organization",
            true,
            true,
            LocalDateTime.now(),
            RequireEvidence.WEEKLY,
        )

        val projectRoleRecentDTO = ProjectRoleRecentDTO(
            1L,
            "desarrollador",
            "Project",
            "Organization",
            true,
            true,
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
            true,
        )

        doReturn(listOf(projectRoleRecent)).whenever(latestProjectRolesForAuthenticatedUserUseCase).getProjectRolesRecent()
        doReturn(projectRoleRecentDTO).whenever(projectRoleRecentConverter).toProjectRoleRecentDTO(projectRoleRecent)

        val response = client.exchangeList<ProjectRoleRecentDTO>(GET("/api/project-roles/recents"))

        assertEquals(OK, response.status)
        assertEquals(listOf(projectRoleRecentDTO), response.body.get())

    }

    private fun getFailProvider() = arrayOf(
        arrayOf(ProjectRoleNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
    )

}
